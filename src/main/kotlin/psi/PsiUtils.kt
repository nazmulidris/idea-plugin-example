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

package psi

import Colors
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiRecursiveElementWalkingVisitor
import com.intellij.psi.search.PsiElementProcessor
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.util.PsiTreeUtil
import org.intellij.plugins.markdown.lang.MarkdownTokenTypeSets
import org.intellij.plugins.markdown.lang.MarkdownTokenTypes
import org.intellij.plugins.markdown.lang.psi.MarkdownPsiElementFactory
import printDebugHeader
import printlnAndLog
import whichThread

/**
 * Both parameters are marked Nullable for testing. In unit tests, a class of this object is not created.
 */
class CheckCancelled(private val indicator: ProgressIndicator?, private val project: Project?) {
  operator fun invoke() {
    printDebugHeader()

    if (indicator == null || project == null) return

    Colors.ANSI_RED(whichThread()).printlnAndLog()
    Colors.ANSI_YELLOW("Checking for cancellation").printlnAndLog()

    if (indicator.isCanceled) {
      Colors.ANSI_RED("Task was cancelled").printlnAndLog()
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

fun callCheckCancelled(checkCancelled: CheckCancelled?) {
  checkCancelled?.invoke()
}

data class LinkInfo(var parentLinkElement: PsiElement, var linkText: String, var linkDestination: String) {
  override fun toString(): String {
    return "\nLinkInfo{ $linkText,  $linkDestination }"
  }
}

fun findParentElement(element: PsiElement?, tokenSet: TokenSet, checkCancelled: CheckCancelled?): PsiElement? {
  if (element == null) return null
  return PsiTreeUtil.findFirstParent(element, false) {
    callCheckCancelled(checkCancelled)
    val node = it.node
    node != null && tokenSet.contains(node.elementType)
  }
}

fun findChildElement(element: PsiElement?, tokenSet: TokenSet, checkCancelled: CheckCancelled?): PsiElement? {
  if (element == null) return null

  val processor: PsiElementProcessor.FindElement<PsiElement> =
      object : PsiElementProcessor.FindElement<PsiElement>() {
        // If found, returns false. Otherwise returns true.
        override fun execute(each: PsiElement): Boolean {
          callCheckCancelled(checkCancelled)
          if (tokenSet.contains(each.node.elementType)) return setFound(each)
          else return true
        }
      }

  element.accept(object : PsiRecursiveElementWalkingVisitor() {
    override fun visitElement(element: PsiElement) {
      callCheckCancelled(checkCancelled)
      val isFound = !processor.execute(element)
      if (isFound) stopWalking()
      else super.visitElement(element)
    }
  })

  return processor.foundElement
}

fun findChildElement(element: PsiElement?, token: IElementType?, checkCancelled: CheckCancelled?): PsiElement? {
  return findChildElement(element, TokenSet.create(token), checkCancelled)
}

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
fun findLink(element: PsiElement?, psiFile: PsiFile, checkCancelled: CheckCancelled?): LinkInfo? {
  printDebugHeader()

  // Find the first parent of the element at the caret, which is a link.
  val parentLinkElement = findParentElement(element, MarkdownTokenTypeSets.LINKS, checkCancelled)

  val linkTextElement =
      findChildElement(parentLinkElement, MarkdownTokenTypeSets.LINK_TEXT, checkCancelled)
  val textElement =
      findChildElement(linkTextElement, MarkdownTokenTypes.TEXT, checkCancelled)
  val linkDestinationElement =
      findChildElement(parentLinkElement, MarkdownTokenTypeSets.LINK_DESTINATION, checkCancelled)

  val linkText = textElement?.text
  val linkDestination = linkDestinationElement?.text

  if (linkText == null || linkDestination == null || parentLinkElement == null) return null

  Colors.ANSI_GREEN("Top level element of type contained in MarkdownTokenTypeSets.LINKS found! 🎉").printlnAndLog()
  Colors.ANSI_GREEN("linkText: $linkText, linkDest: $linkDestination").printlnAndLog()
  return LinkInfo(parentLinkElement, linkText, linkDestination)
}

fun replaceExistingLinkWith(project: Project, newLinkInfo: LinkInfo, checkCancelled: CheckCancelled?) {
  // Create a replacement link destination.
  val replacementLinkElement =
      createNewLinkElement(project, newLinkInfo.linkText, newLinkInfo.linkDestination, checkCancelled)

  // Replace the original link destination in the [parentLinkElement] w/ the new one.
  if (replacementLinkElement != null) newLinkInfo.parentLinkElement.replace(replacementLinkElement)
}

fun createNewLinkElement(project: Project,
                         linkText: String,
                         linkDestination: String,
                         checkCancelled: CheckCancelled?
): PsiElement? {
  val markdownText = "[$linkText]($linkDestination)"
  val newFile = MarkdownPsiElementFactory.createFile(project, markdownText)
  val newParentLinkElement = findChildElement(newFile, MarkdownTokenTypeSets.LINKS, checkCancelled)
  return newParentLinkElement
}
