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

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.DialogWrapper
import notify
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel

/**
 * [Docs](https://www.jetbrains.org/intellij/sdk/docs/user_interface_components/dialog_wrapper.html).
 */
class ShowDialogSampleAction : AnAction() {
  override fun actionPerformed(e: AnActionEvent) {
    val response = SampleDialogWrapper().showAndGet()
    Pair("Dialog", "Response selected:${if (response) "Yes" else "No"}").notify()
  }
}

class SampleDialogWrapper : DialogWrapper(true) {
  init {
    init()
    title = "Sample Dialog"
  }

  override fun createCenterPanel(): JComponent {
    val panel = JPanel(BorderLayout())
    val label = JLabel("Press OK or Cancel")
    label.preferredSize = Dimension(100, 100)
    panel.add(label, BorderLayout.CENTER)
    return panel
  }
}