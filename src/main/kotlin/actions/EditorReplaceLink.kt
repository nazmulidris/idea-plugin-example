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

import Colors.*
import actions.EditorBaseAction.mustHaveProjectAndEditor
import actions.EditorReplaceLink.RunningState.*
import com.google.common.annotations.VisibleForTesting
import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiRecursiveElementWalkingVisitor
import com.intellij.psi.search.PsiElementProcessor.FindElement
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.util.PsiTreeUtil
import notify
import org.intellij.plugins.markdown.lang.MarkdownTokenTypeSets
import org.intellij.plugins.markdown.lang.MarkdownTokenTypes
import org.intellij.plugins.markdown.lang.psi.MarkdownPsiElementFactory
import org.intellij.plugins.markdown.ui.actions.MarkdownActionUtil
import printDebugHeader
import printlnAndLog
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
   * For some tests this is not initialized, but accessed when running [doWorkInBackground]. Use [callCheckCancelled]
   * instead of a direct call to `CheckCancelled.invoke()`.
   */
  private lateinit var checkCancelled: CheckCancelled
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
   * an exception (and aborts) when [callCheckCancelled] is called.
   */
  @VisibleForTesting
  fun doWorkInBackground(editor: Editor, psiFile: PsiFile, project: Project): Boolean {
    printDebugHeader()
    ANSI_RED(whichThread()).printlnAndLog()

    // Acquire a read lock in order to find the link information.
    val linkInfo = runReadAction { findLink(editor, project, psiFile) }

    callCheckCancelled()

    // Actually shorten the link in this background thread (ok to block here).
    if (linkInfo == null) return false
    linkInfo.linkDestination = shortenUrlService.shorten(linkInfo.linkDestination) // Blocking call, does network IO.

    CopyPasteManager.getInstance().setContents(StringSelection(linkInfo.linkDestination))

    callCheckCancelled()

    // Mutate the PSI in this write command action.
    // - The write command action enables undo.
    // - The lambda inside of this call runs in the EDT.
    WriteCommandAction.runWriteCommandAction(project) {
      if (!psiFile.isValid) return@runWriteCommandAction
      ANSI_RED(whichThread()).printlnAndLog()
      replaceExistingLinkWith(project, linkInfo)
    }

    callCheckCancelled()

    return true
  }

  data class LinkInfo(var parentLinkElement: PsiElement, var linkText: String, var linkDestination: String)

  /**
   * This function tries to find the first element which is a link, by walking up the tree starting w/ the element that
   * is currently under the caret.
   *
   * To simplify, something like `PsiUtilCore.getElementType(element) == INLINE_LINK` is evaluated for each element
   * starting from the element under the caret, then visiting its parents, and their parents, etc, until a node of type
   * `INLINE_LINK` is found, actually, a type contained in [MarkdownTokenTypeSets.LINKS].
   *
   * The tree might look something like the following, which is a snippet of this
   * [README.md](https://tinyurl.com/rdowe6q) file).
   *
   * ```
   * MarkdownParagraphImpl(Markdown:PARAGRAPH)(1201,1498)
   *   PsiElement(Markdown:Markdown:TEXT)('The main goal of this plugin is to show')(1201,1240)
   *   PsiElement(Markdown:WHITE_SPACE)(' ')(1240,1241)
   *   ASTWrapperPsiElement(Markdown:Markdown:INLINE_LINK)(1241,1274)  <============[🔥 WE WANT THIS PARENT 🔥]=========
   *     ASTWrapperPsiElement(Markdown:Markdown:LINK_TEXT)(1241,1252)
   *       PsiElement(Markdown:Markdown:[)('[')(1241,1242)
   *       PsiElement(Markdown:Markdown:TEXT)('SonarQube')(1242,1251)  <============[🔥 EDITOR CARET IS HERE 🔥]========
   *       PsiElement(Markdown:Markdown:])(']')(1251,1252)
   *     PsiElement(Markdown:Markdown:()('(')(1252,1253)
   *     MarkdownLinkDestinationImpl(Markdown:Markdown:LINK_DESTINATION)(1253,1273)
   *       PsiElement(Markdown:Markdown:GFM_AUTOLINK)('http://sonarqube.org')(1253,1273)
   *     PsiElement(Markdown:Markdown:))(')')(1273,1274)
   *   PsiElement(Markdown:WHITE_SPACE)(' ')(1274,1275)
   *   PsiElement(Markdown:Markdown:TEXT)('issues directly within your IntelliJ IDE.')(1275,1316)
   *   PsiElement(Markdown:Markdown:EOL)('\n')(1316,1317)
   *   PsiElement(Markdown:Markdown:TEXT)('Currently the plugin is build to work in IntelliJ IDEA,')(1317,1372)
   *   PsiElement(Markdown:WHITE_SPACE)(' ')(1372,1373)
   *   PsiElement(Markdown:Markdown:TEXT)('RubyMine,')(1373,1382)
   *   PsiElement(Markdown:WHITE_SPACE)(' ')(1382,1383)
   *   PsiElement(Markdown:Markdown:TEXT)('WebStorm,')(1383,1392)
   *   PsiElement(Markdown:WHITE_SPACE)(' ')(1392,1393)
   *   PsiElement(Markdown:Markdown:TEXT)('PhpStorm,')(1393,1402)
   *   PsiElement(Markdown:WHITE_SPACE)(' ')(1402,1403)
   *   PsiElement(Markdown:Markdown:TEXT)('PyCharm,')(1403,1411)
   *   PsiElement(Markdown:WHITE_SPACE)(' ')(1411,1412)
   *   PsiElement(Markdown:Markdown:TEXT)('AppCode and Android Studio with any programming ... SonarQube.')(1412,1498)
   * PsiElement(Markdown:Markdown:EOL)('\n')(1498,1499)
   * ```
   */
  private fun findLink(editor: Editor, project: Project, psiFile: PsiFile): LinkInfo? {
    printDebugHeader()

    val offset = editor.caretModel.offset
    val elementAtCaret: PsiElement? = psiFile.findElementAt(offset)

    // Find the first parent of the element at the caret, which is a link.
    val parentLinkElement = findParentElement(elementAtCaret, MarkdownTokenTypeSets.LINKS)

    val linkTextElement = findChildElement(parentLinkElement, MarkdownTokenTypeSets.LINK_TEXT)
    val textElement = findChildElement(linkTextElement, MarkdownTokenTypes.TEXT)
    val linkDestinationElement = findChildElement(parentLinkElement, MarkdownTokenTypeSets.LINK_DESTINATION)

    val linkText = textElement?.text
    val linkDestination = linkDestinationElement?.text

    if (linkText == null || linkDestination == null || parentLinkElement == null) return null

    ANSI_GREEN("Top level element of type contained in MarkdownTokenTypeSets.LINKS found! 🎉").printlnAndLog()
    ANSI_GREEN("linkText: $linkText, linkDest: $linkDestination").printlnAndLog()
    return LinkInfo(parentLinkElement, linkText, linkDestination)
  }

  private fun replaceExistingLinkWith(project: Project, newLinkInfo: LinkInfo) {
    // Create a replacement link destination.
    val replacementLinkElement = createNewLinkElement(project, newLinkInfo.linkText, newLinkInfo.linkDestination)

    // Replace the original link destination in the [parentLinkElement] w/ the new one.
    if (replacementLinkElement != null) newLinkInfo.parentLinkElement.replace(replacementLinkElement)
  }

  private fun createNewLinkElement(project: Project, linkText: String, linkDestination: String): PsiElement? {
    val markdownText = "[$linkText]($linkDestination)"
    val newFile = MarkdownPsiElementFactory.createFile(project, markdownText)
    val newParentLinkElement = findChildElement(newFile, MarkdownTokenTypeSets.LINKS)
    return newParentLinkElement
  }

  private fun findChildElement(element: PsiElement?, token: IElementType?): PsiElement? {
    return findChildElement(element, TokenSet.create(token))
  }

  private fun findChildElement(element: PsiElement?, tokenSet: TokenSet): PsiElement? {
    if (element == null) return null

    val processor: FindElement<PsiElement> =
        object : FindElement<PsiElement>() {
          // If found, returns false. Otherwise returns true.
          override fun execute(each: PsiElement): Boolean {
            callCheckCancelled()
            if (tokenSet.contains(each.node.elementType)) return setFound(each)
            else return true
          }
        }

    element.accept(object : PsiRecursiveElementWalkingVisitor() {
      override fun visitElement(element: PsiElement) {
        callCheckCancelled()
        val isFound = !processor.execute(element)
        if (isFound) stopWalking()
        else super.visitElement(element)
      }
    })

    return processor.foundElement
  }

  private fun findParentElement(element: PsiElement?, tokenSet: TokenSet): PsiElement? {
    if (element == null) return null
    return PsiTreeUtil.findFirstParent(element, false) {
      callCheckCancelled()
      val node = it.node
      node != null && tokenSet.contains(node.elementType)
    }
  }

  fun callCheckCancelled() {
    try {
      checkCancelled.invoke()
    }
    catch (e: UninitializedPropertyAccessException) {
      // For some tests [checkCancelled] is not initialized. And accessing a lateinit var will throw an exception.
    }
  }

  /**
   * Both parameters are marked Nullable for testing. In unit tests, a class of this object is not created.
   */
  class CheckCancelled(private val indicator: ProgressIndicator?, private val project: Project?) {
    operator fun invoke() {
      printDebugHeader()

      if (indicator == null || project == null) return

      ANSI_RED(whichThread()).printlnAndLog()
      ANSI_YELLOW("Checking for cancellation").printlnAndLog()

      if (indicator.isCanceled) {
        ANSI_RED("Task was cancelled").printlnAndLog()
        ApplicationManager
            .getApplication()
            .invokeLater {
              Messages.showWarningDialog(
                  project, "Task was cancelled", "Cancelled")
            }
      }

      indicator.checkCanceled()
      // Can use ProgressManager.checkCancelled() as well, if we don't want to pass the indicator around.
    }
  }

  override fun update(e: AnActionEvent) = mustHaveProjectAndEditor(e)
}