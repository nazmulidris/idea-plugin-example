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
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.ServiceManager.getService
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.layout.panel
import javax.swing.JComponent
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

//
// Implement the action here.
//

class ShowKotlinUIDSLSampleAction : AnAction() {
  override fun actionPerformed(e: AnActionEvent) {
    val response = MyDialogWrapper().showAndGet()
    consoleLog(ConsoleColors.ANSI_PURPLE, "MyDialogWrapper", "Response selected:${if (response) "Yes" else "No"}")
  }
}

//
// Create the UI here using the Kotlin DSL UI.
//

/**
 * Look at
 * [PasswordSafeConfigurable.kt](https://github.com/JetBrains/intellij-community/blob/master/platform/credential-store/src/PasswordSafeConfigurable.kt)
 * for more information on how to use `apply()` and `reset()` in the [DialogPanel]. These relate to "Configurables".
 */
private fun createDialogPanel(): DialogPanel = panel {
  noteRow("This is a row with a note")
  row {
    checkBox("Boolean MyState::myFlag", KotlinDSLUISampleService.instance.state::myFlag)
  }
  row {
    label("Label for text field")
    textField(KotlinDSLUISampleService.instance.state::myString)
  }
}

//
// Simply wrap the UI in a DialogWrapper.
//

private class MyDialogWrapper : DialogWrapper(true) {
  init {
    init()
    title = "Sample Dialog with Kotlin UI DSL"
  }

  override fun createCenterPanel(): JComponent = createDialogPanel()
}

//
// PersistentStateComponent & LightService to persist the state across IDE restarts.
//

@Service
@State(name = "KotlinDSLUISampleData", storages = [Storage("kotlinDSLUISampleData.xml")])
object KotlinDSLUISampleService : PersistentStateComponent<KotlinDSLUISampleService.State> {
  val instance: KotlinDSLUISampleService
    get() = getService(KotlinDSLUISampleService::class.java)

  //
  // PersistentStateComponent methods.
  //
  private var state = State()

  override fun getState(): State {
    consoleLog(ConsoleColors.ANSI_PURPLE, "KotlinDSLUISampleData.getState", "state: $state")
    return state
  }

  override fun loadState(stateLoadedFromPersistence: State) {
    consoleLog(ConsoleColors.ANSI_PURPLE,
               "KotlinDSLUISampleData.loadState",
               "stateLoadedFromPersistence: $stateLoadedFromPersistence")
    state = stateLoadedFromPersistence
  }

  //
  // Properties in this class are bound to the Kotlin DSL UI.
  //

  class State {
    var myFlag: Boolean by object : LoggingProperty<State, Boolean>(false) {}
    var myString: String by object : LoggingProperty<State, String>("") {}

    override fun toString(): String = "State{ myFlag: '$myFlag', myString: '$myString' }"

    open class LoggingProperty<R, T>(initValue: T) : ReadWriteProperty<R, T> {
      var backingField: T = initValue

      override fun getValue(thisRef: R, property: KProperty<*>): T {
        consoleLog(ConsoleColors.ANSI_BLUE, "MyState::${property.name}.getValue()", "value: '$backingField'")
        return backingField
      }

      override fun setValue(thisRef: R, property: KProperty<*>, value: T) {
        backingField = value
        consoleLog(ConsoleColors.ANSI_BLUE, "MyState::${property.name}.setValue()", "value: '$backingField'")
      }
    }
  }

}





