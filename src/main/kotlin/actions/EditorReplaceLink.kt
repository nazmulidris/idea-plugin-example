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

import actions.EditorBaseAction.mustHaveProjectAndEditor
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import printDebugHeader
import printlnAndLog

class EditorReplaceLink : AnAction() {
  override fun actionPerformed(e: AnActionEvent) {
    printDebugHeader()

    val editor = e.getRequiredData(CommonDataKeys.EDITOR)
    val psiFile = e.getRequiredData(CommonDataKeys.PSI_FILE)
    val project = e.getRequiredData(CommonDataKeys.PROJECT)
    val progressTitle = "Doing heavy PSI mutation"

    object : Task.Backgroundable(project, progressTitle) {
      override fun run(indicator: ProgressIndicator) = doWorkInBackground(editor, psiFile, project, indicator)
    }.queue()
  }

  private fun doWorkInBackground(editor: Editor, psiFile: PsiFile, project: Project, indicator: ProgressIndicator) {
    printDebugHeader()

    // The write command action enables undo.
    WriteCommandAction.runWriteCommandAction(project) {
      if (!psiFile.isValid) return@runWriteCommandAction
      val document = editor.document
      replaceLink(editor, psiFile)
      PsiDocumentManager.getInstance(project).commitDocument(document)
    }
  }

  private fun replaceLink(editor: Editor, psiFile: PsiFile) {
    printDebugHeader()

    val offset = editor.caretModel.offset
    val element: PsiElement? = psiFile.findElementAt(offset)

    element?.apply {

      element.toString().printlnAndLog()

      //PsiUtilCore.getElementType(link) == INLINE_LINK

    }

  }

  override fun update(e: AnActionEvent) = mustHaveProjectAndEditor(e)
}