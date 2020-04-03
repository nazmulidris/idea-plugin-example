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
import actions.EditorReplaceLink.RunningState.*
import com.google.common.annotations.VisibleForTesting
import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import notify
import org.intellij.plugins.markdown.ui.actions.MarkdownActionUtil
import printDebugHeader
import printlnAndLog
import psi.CheckCancelled
import psi.findLink
import psi.findParentElement
import psi.replaceExistingLinkWith
import urlshortenservice.ShortenUrlService
import urlshortenservice.TinyUrl
import whichThread
import java.awt.datatransfer.StringSelection

/**
 * Note: You can find a slightly different implementation of [findParentElement] using more utility classes from the
 * platform in the following places:
 * - [MarkdownActionUtil]
 * - [MarkdownIntroduceLinkReferenceAction.java](https://tinyurl.com/ufw3kll)
 */
class EditorReplaceLink(val shortenUrlService: ShortenUrlService = TinyUrl()) : AnAction() {
  /**
   * For some tests this is not initialized, but accessed when running [doWorkInBackground]. Make sure to call
   * `checkCancelled?.invoke()`.
   */
  private var checkCancelled: CheckCancelled? = null

  @VisibleForTesting
  private var myIndicator: ProgressIndicator? = null

  override fun actionPerformed(e: AnActionEvent) {
    printDebugHeader()

    val editor = e.getRequiredData(CommonDataKeys.EDITOR)
    val psiFile = e.getRequiredData(CommonDataKeys.PSI_FILE)
    val project = e.getRequiredData(CommonDataKeys.PROJECT)
    val progressTitle = "Doing heavy PSI mutation"

    object : Task.Backgroundable(project, progressTitle) {
      var result: Boolean = false

      override fun run(indicator: ProgressIndicator) {
        if (PluginManagerCore.isUnitTestMode) {
          ANSI_RED("🔥 Is in unit testing mode 🔥️").printlnAndLog()
          // Save a reference to this indicator for testing.
          myIndicator = indicator
        }
        checkCancelled = CheckCancelled(indicator, project)
        result = doWorkInBackground(editor, psiFile, project)
      }

      override fun onFinished() {
        Pair("Background task completed", if (result) "Link shortened & in clipboard" else "Nothing to do").notify()
      }
    }.queue()
  }

  enum class RunningState {
    NOT_STARTED, IS_RUNNING, HAS_STOPPED, IS_CANCELLED
  }

  @VisibleForTesting
  fun isRunning(): RunningState {
    if (myIndicator == null) {
      return NOT_STARTED
    }
    else {
      return when {
        myIndicator!!.isCanceled -> IS_CANCELLED
        myIndicator!!.isRunning  -> IS_RUNNING
        else                     -> HAS_STOPPED
      }
    }
  }

  @VisibleForTesting
  fun isCanceled(): Boolean = myIndicator?.isCanceled ?: false

  /**
   * This function returns true when it executes successfully. If there is no work for this function to do then it
   * returns false. However, if the task is cancelled (when wrapped w/ a [Task.Backgroundable], then it will throw
   * an exception (and aborts) when `checkCancelled.invoke()` is called.
   */
  @VisibleForTesting
  fun doWorkInBackground(editor: Editor, psiFile: PsiFile, project: Project): Boolean {
    printDebugHeader()
    ANSI_RED(whichThread()).printlnAndLog()

    // Acquire a read lock in order to find the link information.
    val linkNode = runReadAction {
      val offset = editor.caretModel.offset
      val elementAtCaret: PsiElement? = psiFile.findElementAt(offset)
      return@runReadAction findLink(elementAtCaret, psiFile, checkCancelled)
    }

    checkCancelled?.invoke()

    // Actually shorten the link in this background thread (ok to block here).
    if (linkNode == null) return false
    linkNode.linkDestination = shortenUrlService.shorten(linkNode.linkDestination) // Blocking call, does network IO.

    CopyPasteManager.getInstance().setContents(StringSelection(linkNode.linkDestination))

    checkCancelled?.invoke()

    // Mutate the PSI in this write command action.
    // - The write command action enables undo.
    // - The lambda inside of this call runs in the EDT.
    WriteCommandAction.runWriteCommandAction(project) {
      if (!psiFile.isValid) return@runWriteCommandAction
      ANSI_RED(whichThread()).printlnAndLog()
      replaceExistingLinkWith(project, linkNode, checkCancelled)
    }

    checkCancelled?.invoke()

    return true
  }

  override fun update(e: AnActionEvent) = mustHaveProjectAndEditor(e)
}