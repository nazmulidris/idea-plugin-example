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
package ui

import color_console_log.ColorConsoleContext.Companion.colorConsole
import color_console_log.Colors.Green
import color_console_log.Colors.Purple
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.PopupStep
import com.intellij.openapi.ui.popup.util.BaseListPopupStep

/**
 * [Docs](https://www.jetbrains.org/intellij/sdk/docs/user_interface_components/popups.html).
 */
class ShowPopupSampleAction : AnAction() {
  lateinit var editor: Editor

  override fun actionPerformed(e: AnActionEvent) {
    editor = e.getRequiredData(CommonDataKeys.EDITOR)

    // One way to show a list.
    JBPopupFactory
        .getInstance()
        .createListPopup(MyList {
          // Another way to show a list, using the builder.
          JBPopupFactory
              .getInstance()
              .createPopupChooserBuilder(mutableListOf("one", "two", "three"))
              .setTitle("PopupChooserBuilder")
              .setItemChosenCallback {
                colorConsole {
                  printLine {
                    span(Purple, "PopupChooserBuilder.onChosen")
                    span(Green, it.toString())
                  }
                }
              }
              .createPopup()
              .showInBestPositionFor(editor)
        })
        .showInBestPositionFor(editor)
  }
}

class MyList(val onChosenHandler: (String) -> Unit) : BaseListPopupStep<String>() {
  init {
    init("Popup title", mutableListOf("Choice 1", "Choice 2", "Choice 3"), null)
  }

  override fun getTextFor(value: String): String {
    return "TEXT: $value"
  }

  override fun onChosen(selectedValue: String, finalChoice: Boolean): PopupStep<*>? {
    colorConsole {
      printLine {
        span(Purple, "MyList.onChosen")
        span(Green, selectedValue.toString())
      }
    }
    onChosenHandler(selectedValue)
    return PopupStep.FINAL_CHOICE
  }
}