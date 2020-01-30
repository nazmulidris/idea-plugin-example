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
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task.Backgroundable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiFile
import org.intellij.plugins.markdown.lang.psi.MarkdownRecursiveElementVisitor
import org.intellij.plugins.markdown.lang.psi.impl.MarkdownHeaderImpl
import org.intellij.plugins.markdown.lang.psi.impl.MarkdownParagraphImpl
import printDebugHeader
import printlnAndLog
import whichThread

internal class EditorShowPSIInfo : AnAction() {
  override fun actionPerformed(e: AnActionEvent) {
    printDebugHeader()

    val psiFile = e.getRequiredData(CommonDataKeys.PSI_FILE)
    val psiFileViewProvider = psiFile.viewProvider
    val project = e.getRequiredData(CommonDataKeys.PROJECT)
    val progressTitle = "Doing heavy PSI computation"

    val task = object : Backgroundable(project, progressTitle) {
      override fun run(indicator: ProgressIndicator) {
        doWorkInBackground(project, psiFile, psiFileViewProvider, indicator)
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
                                 indicator: ProgressIndicator
  ) {
    printDebugHeader()
    ANSI_YELLOW(whichThread()).printlnAndLog()

    indicator.isIndeterminate = true

    buildString {
      val count = object {
        var paragraph: Int = 0
        var header: Int = 0
      }

      psiFile.accept(object : MarkdownRecursiveElementVisitor() {
        override fun visitParagraph(paragraph: MarkdownParagraphImpl) {
          printDebugHeader()
          ANSI_YELLOW(whichThread()).printlnAndLog()

          count.paragraph++
          Thread.sleep(2000)
          checkCancelled(indicator, project)

          // The following line ensures that ProgressManager.checkCancelled()
          // is called.
          super.visitParagraph(paragraph)
        }

        override fun visitHeader(header: MarkdownHeaderImpl) {
          printDebugHeader()
          ANSI_YELLOW(whichThread()).printlnAndLog()

          count.header++
          Thread.sleep(2000)
          checkCancelled(indicator, project)

          // The following line ensures that ProgressManager.checkCancelled()
          // is called.
          super.visitHeader(header)
        }
      })

      append("languages: ${psiFileViewProvider.languages}\n")
      append("count.header: ${count.header}\n")
      append("count.paragraph: ${count.paragraph}\n")

      checkCancelled(indicator, project)

    }.printlnAndLog()
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