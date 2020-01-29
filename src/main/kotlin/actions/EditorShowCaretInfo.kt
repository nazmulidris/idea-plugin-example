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

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.*
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.ui.Messages
import printDebugHeader
import java.awt.datatransfer.StringSelection

class EditorShowCaretInfo : AnAction() {
  override fun actionPerformed(e: AnActionEvent) {
    printDebugHeader()
    val editor: Editor = e.getRequiredData(CommonDataKeys.EDITOR)
    val caretModel: CaretModel = editor.caretModel
    val primaryCaret: Caret = caretModel.primaryCaret

    val logicalPosition: LogicalPosition = primaryCaret.logicalPosition

    val visualPosition: VisualPosition = primaryCaret.visualPosition

    val caretOffset = primaryCaret.offset

    buildString {
      append(createLogicalCaretPositionMessage(logicalPosition))
      append("\n")
      append(createVisualCaretPositionMessage(visualPosition))
      append("\n")
      append("CaretOffset[${caretOffset}]")
      append("\n")
    }.apply {
      CopyPasteManager.getInstance().setContents(StringSelection(this))
      Messages.showInfoMessage(
          this,
          "Caret info inside Editor (copied to clipboard)")
    }
  }

  private fun createLogicalCaretPositionMessage(
      logicalPosition: LogicalPosition
  ): String {
    return buildString {
      // LogicalPosition.
      append("LogicalPosition[")
      append("line: ${logicalPosition.line}, ")
      append("column: ${logicalPosition.column}, ")
      append("leansForward: ${logicalPosition.leansForward}")
      append("]\n")
    }
  }

  private fun createVisualCaretPositionMessage(
      visualPosition: VisualPosition
  ): String {
    return buildString {
      // VisualPosition.
      append("VisualPosition[")
      append("line: ${visualPosition.line}, ")
      append("column: ${visualPosition.column}, ")
      append("leansRight: ${visualPosition.leansRight}")
      append("]\n")
    }
  }

  override fun update(e: AnActionEvent) =
      EditorBaseAction.mustHaveProjectAndEditor(e)
}