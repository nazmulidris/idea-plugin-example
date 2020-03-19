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
import longSleep
import org.intellij.plugins.markdown.lang.psi.MarkdownRecursiveElementVisitor
import org.intellij.plugins.markdown.lang.psi.impl.MarkdownHeaderImpl
import org.intellij.plugins.markdown.lang.psi.impl.MarkdownLinkDestinationImpl
import org.intellij.plugins.markdown.lang.psi.impl.MarkdownParagraphImpl
import printDebugHeader
import printlnAndLog
import psi.langSetContains
import sleep
import whichThread


internal class EditorShowPSIInfo : AnAction() {
  private data class Count(var paragraph: Int = 0, var links: Int = 0, var header: Int = 0)

  private val count = Count()

  override fun actionPerformed(e: AnActionEvent) {
    printDebugHeader()

    val psiFile = e.getRequiredData(CommonDataKeys.PSI_FILE)
    val psiFileViewProvider = psiFile.viewProvider
    val project = e.getRequiredData(CommonDataKeys.PROJECT)
    val editor = e.getRequiredData(CommonDataKeys.EDITOR)
    val progressTitle = "Doing heavy PSI access"

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

    val langs = psiFileViewProvider.languages

    val message = buildString {
      append(langs.joinToString(prefix = "\n[", postfix = "]\n"))
      when {
        langSetContains(langs, "Markdown") -> runReadAction {
          append(navigateMarkdownTree(psiFile,
                                      indicator,
                                      project))
        }
        langSetContains(langs, "Java")     -> runReadAction {
          append(navigateJavaTree(psiFile,
                                  indicator,
                                  project,
                                  editor))
        }
        else                               -> append(ANSI_RED("No supported languages found"))
      }
      checkCancelled(indicator, project)
    }

    message.printlnAndLog()
    ApplicationManager.getApplication().invokeLater {
      Messages.showMessageDialog(project, message, "PSI Info", null)
    }

  }

  private fun navigateJavaTree(psiFile: PsiFile,
                               indicator: ProgressIndicator,
                               project: Project,
                               editor: Editor
  ): String {
    printDebugHeader()

    val offset = editor.caretModel.offset
    val element: PsiElement? = psiFile.findElementAt(offset)

    val javaPsiInfo = buildString {

      longSleep()
      checkCancelled(indicator, project)

      element?.apply {
        append("Element at caret: $element\n")
        val containingMethod: PsiMethod? = PsiTreeUtil.getParentOfType(element, PsiMethod::class.java)

        containingMethod?.apply {
          append("Containing method: ${containingMethod.name}\n")

          containingMethod.containingClass?.apply {
            append("Containing class: ${this.name} \n")
          }

          val list = mutableListOf<PsiLocalVariable>()
          containingMethod.accept(object : JavaRecursiveElementVisitor() {
            override fun visitLocalVariable(variable: PsiLocalVariable) {
              list.add(variable)

              // The following line ensures that ProgressManager.checkCancelled()
              // is called.
              super.visitLocalVariable(variable)
            }
          })
          if (list.isNotEmpty())
            append(list.joinToString(prefix = "Local variables:\n", separator = "\n") { it -> "- ${it.name}" })

        }

      }

      checkCancelled(indicator, project)

    }

    return if (javaPsiInfo == "") "No PsiElement at caret!" else javaPsiInfo

  }

  private fun navigateMarkdownTree(psiFile: PsiFile,
                                   indicator: ProgressIndicator,
                                   project: Project
  ): String {
    psiFile.accept(object : MarkdownRecursiveElementVisitor() {
      override fun visitLinkDestination(linkDestination: MarkdownLinkDestinationImpl) {
        printDebugHeader()
        ANSI_YELLOW(whichThread()).printlnAndLog()

        count.links++
        sleep()
        checkCancelled(indicator, project)

        // The following line ensures that ProgressManager.checkCancelled()
        // is called.
        super.visitLinkDestination(linkDestination)
      }

      override fun visitParagraph(paragraph: MarkdownParagraphImpl) {
        printDebugHeader()
        ANSI_YELLOW(whichThread()).printlnAndLog()

        count.paragraph++
        sleep()
        checkCancelled(indicator, project)

        // The following line ensures that ProgressManager.checkCancelled()
        // is called.
        super.visitParagraph(paragraph)
      }

      override fun visitHeader(header: MarkdownHeaderImpl) {
        printDebugHeader()
        ANSI_YELLOW(whichThread()).printlnAndLog()

        count.header++
        sleep()
        checkCancelled(indicator, project)

        // The following line ensures that ProgressManager.checkCancelled()
        // is called.
        super.visitHeader(header)
      }
    })

    return count.toString()

  }

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