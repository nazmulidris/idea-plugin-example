/*
 * Copyright 2020 Nazmul Idris. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package actions

import Colors.ANSI_RED
import Colors.ANSI_YELLOW
import com.intellij.lang.Language
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task.Backgroundable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil
import org.intellij.plugins.markdown.lang.psi.MarkdownRecursiveElementVisitor
import org.intellij.plugins.markdown.lang.psi.impl.MarkdownHeaderImpl
import org.intellij.plugins.markdown.lang.psi.impl.MarkdownParagraphImpl
import printDebugHeader
import printlnAndLog
import whichThread


internal class EditorShowPSIInfo : AnAction() {
  val backgroundThreadSleepDuration: Long = 100

  /** [kotlin anonymous objects](https://medium.com/@agrawalsuneet/object-expression-in-kotlin-e75735f19f5d) */
  private val count = object {
    var paragraph: Int = 0
    var header: Int = 0
  }

  override fun actionPerformed(e: AnActionEvent) {
    printDebugHeader()

    val psiFile = e.getRequiredData(CommonDataKeys.PSI_FILE)
    val psiFileViewProvider = psiFile.viewProvider
    val project = e.getRequiredData(CommonDataKeys.PROJECT)
    val editor = e.getRequiredData(CommonDataKeys.EDITOR)
    val progressTitle = "Doing heavy PSI computation"

    val task = object : Backgroundable(project, progressTitle) {
      override fun run(indicator: ProgressIndicator) {
        doWorkInBackground(
            project, psiFile, psiFileViewProvider, indicator, editor)
      }
    }

    task.queue()

    // No need for the code below if you use `task.queue()`.
    // ProgressManager
    //    .getInstance()
    //    .runProcessWithProgressAsynchronously(
    //        task, BackgroundableProcessIndicator(task))

  }

  private fun doWorkInBackground(project: Project,
                                 psiFile: PsiFile,
                                 psiFileViewProvider: FileViewProvider,
                                 indicator: ProgressIndicator,
                                 editor: Editor
  ) {
    printDebugHeader()
    ANSI_YELLOW(whichThread()).printlnAndLog()

    indicator.isIndeterminate = true

    val languages = psiFileViewProvider.languages

    buildString {

      when {
        languages.contains("Markdown") -> runReadAction { navigateMarkdownTree(psiFile, indicator, project) }
        languages.contains("Java")     -> runReadAction { navigateJavaTree(psiFile, indicator, project, editor) }
        else                           -> append(ANSI_RED("No supported languages found"))
      }

      append("languages: $languages\n")
      append("count.header: ${count.header}\n")
      append("count.paragraph: ${count.paragraph}\n")

      checkCancelled(indicator, project)

    }.printlnAndLog()
  }

  private fun navigateJavaTree(psiFile: PsiFile,
                               indicator: ProgressIndicator,
                               project: Project,
                               editor: Editor
  ) {
    val offset = editor.caretModel.offset
    val element = psiFile.findElementAt(offset)

    val javaPsiInfo = buildString {

      element?.apply {
        append("Element at caret: $element\n")
        val containingMethod = PsiTreeUtil.getParentOfType(element, PsiMethod::class.java)

        append("Containing method: ")
        append(containingMethod?.name ?: "none")
        append("\n")

        containingMethod?.apply {
          val containingClass = containingMethod.containingClass
          append("Containing class: ")
          append(if (containingClass != null) containingClass.name else "none")
          append("\n")

          append("Local variables:\n")
          containingMethod.accept(object : JavaRecursiveElementVisitor() {
            override fun visitLocalVariable(variable: PsiLocalVariable) {
              super.visitLocalVariable(variable)
              append(variable.name).append("\n")
            }
          })
        }

      }

    }

    ApplicationManager.getApplication().invokeLater {
      Messages.showMessageDialog(
          project,
          if (javaPsiInfo == "") "No element at caret" else javaPsiInfo,
          "PSI Java Info",
          null)
    }

  }

  private fun navigateMarkdownTree(psiFile: PsiFile,
                                   indicator: ProgressIndicator,
                                   project: Project
  ) {
    psiFile.accept(object : MarkdownRecursiveElementVisitor() {
      override fun visitParagraph(paragraph: MarkdownParagraphImpl) {
        printDebugHeader()
        ANSI_YELLOW(whichThread()).printlnAndLog()

        this@EditorShowPSIInfo.count.paragraph++
        Thread.sleep(backgroundThreadSleepDuration)
        checkCancelled(indicator, project)

        // The following line ensures that ProgressManager.checkCancelled()
        // is called.
        super.visitParagraph(paragraph)
      }

      override fun visitHeader(header: MarkdownHeaderImpl) {
        printDebugHeader()
        ANSI_YELLOW(whichThread()).printlnAndLog()

        this@EditorShowPSIInfo.count.header++
        Thread.sleep(backgroundThreadSleepDuration)
        checkCancelled(indicator, project)

        // The following line ensures that ProgressManager.checkCancelled()
        // is called.
        super.visitHeader(header)
      }
    })

  }

  private fun Set<Language>.contains(language: String): Boolean =
      this.any { language.equals(it.id, ignoreCase = true) }

  private fun checkCancelled(indicator: ProgressIndicator,
                             project: Project
  ) {
    printDebugHeader()
    ANSI_YELLOW(whichThread()).printlnAndLog()

    if (indicator.isCanceled) {
      ANSI_RED("Task was cancelled").printlnAndLog()
      ApplicationManager
          .getApplication()
          .invokeLater {
            Messages.showWarningDialog(
                project, "Task was cancelled", "Cancelled")
          }
    }
  }

  override fun update(e: AnActionEvent) =
      EditorBaseAction.mustHaveProjectAndEditor(e)
}