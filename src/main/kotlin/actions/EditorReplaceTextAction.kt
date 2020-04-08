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

import ColorConsoleContext.Companion.colorConsole
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.CaretModel
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project

class EditorReplaceTextAction : AnAction() {
  /**
   * [Tutorial](https://www.jetbrains.org/intellij/sdk/docs/tutorials/editor_basics/working_with_text.html)
   */
  override fun actionPerformed(e: AnActionEvent) {
    colorConsole {
      printDebugHeader()
      printWhichThread()
    }

    // We are using `getRequiredData()` here because of the checks in the
    // update() method below. This action wouldn't be active if we didn't
    // have a project, an editor, and text selected in that editor.
    val editor: Editor = e.getRequiredData(CommonDataKeys.EDITOR)
    val project: Project = e.getRequiredData(CommonDataKeys.PROJECT)
    val document: Document = editor.document
    val caretModel: CaretModel = editor.caretModel
    val primaryCaret = caretModel.primaryCaret

    // start and end offsets of the selected text (based on the primaryCaret).
    val selection =
        Pair(primaryCaret.selectionStart, primaryCaret.selectionEnd)

    // Actual content that is selected at the caret.
    val selectedText: String = primaryCaret.selectedText!!

    // Change the document in a write action in a command (for undo).
    WriteCommandAction.runWriteCommandAction(project) {
      document.replaceString(selection.first, selection.second,
                             ">> $selectedText <<")
    }

    // Deselect the selection of the text that that was just replaced.
    primaryCaret.removeSelection()
  }

  override fun update(e: AnActionEvent) = mustHaveProjectAndEditorAndTextSelection(e)
}