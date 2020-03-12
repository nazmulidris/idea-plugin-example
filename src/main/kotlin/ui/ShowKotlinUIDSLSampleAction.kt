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

import ConsoleColors
import ConsoleColors.Companion.consoleLog
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.layout.panel
import javax.swing.JComponent

class ShowKotlinUIDSLSampleAction : AnAction() {
  override fun actionPerformed(e: AnActionEvent) {
    val response = MyDialogWrapper().showAndGet()
    consoleLog(ConsoleColors.ANSI_PURPLE, "MyDialogWrapper", "Response selected:${if (response) "Yes" else "No"}")
  }
}

private fun createUI(): DialogPanel = panel {
  noteRow("This is a row with a note")
}

private class MyDialogWrapper : DialogWrapper(true) {
  init {
    init()
    title = "Sample Dialog with Kotlin UI DSL"
  }

  override fun createCenterPanel(): JComponent = createUI()
}
