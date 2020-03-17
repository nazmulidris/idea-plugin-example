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
import com.intellij.ide.BrowserUtil
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.ServiceManager.getService
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.CollectionComboBoxModel
import com.intellij.ui.layout.panel
import ui.KotlinDSLUISampleService.instance
import javax.swing.JComponent
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

//
// Implement the action here.
//

class ShowKotlinUIDSLSampleInDialogAction : AnAction() {
  override fun actionPerformed(e: AnActionEvent) {
    val response = MyDialogWrapper().showAndGet()
    consoleLog(ConsoleColors.ANSI_PURPLE, "MyDialogWrapper", "Response selected:${if (response) "Yes" else "No"}")
  }
}

//
// Create the UI here using the Kotlin DSL UI.
//

/**
 * What UI elements are available?
 * 1. To see what objects can be added to the [LayoutBuilder], check out [RowBuilder].
 * 2. To see what objects can be added inside each row, check out [CellBuilder].
 *
 * Configurables
 * Look at [PasswordSafeConfigurable.kt](https://tinyurl.com/vqk6o3g) for more information on how to use `apply()`
 * and `reset()` in the [DialogPanel]. These relate to "Configurables".
 */
fun createDialogPanel(): DialogPanel {
  // Restore the selection state of the combo box.
  val comboBoxChoices = listOf("choice1", "choice2", "choice3")
  val comboBoxModel = CollectionComboBoxModel(
      comboBoxChoices,
      if (instance.state.myStringChoice == "") null else instance.state.myStringChoice)

  return panel {
    noteRow("This is a row with a note")

    row("[Boolean]") {
      row {
        cell {
          checkBox("", instance.state::myFlag)
          label("Boolean state::myFlag")
        }
      }
    }

    row("[String]") {
      row {
        label("String state::myString")
        textField(instance.state::myString)
      }
    }

    row("[Int]") {
      row {
        label("Int state::myInt")
        spinner(instance.state::myInt, minValue = 0, maxValue = 50, step = 5)
      }
    }

    row("ComboBox / Drop down list") {
      comboBox(comboBoxModel, instance.state::myStringChoice)
    }

    noteRow("""Note with a link. <a href="http://github.com">Open source</a>""") {
      consoleLog(ConsoleColors.ANSI_PURPLE, "link url: '$it' clicked")
      BrowserUtil.browse(it)
    }
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
  private var myState = State()

  override fun getState(): State {
    consoleLog(ConsoleColors.ANSI_PURPLE, "KotlinDSLUISampleData.getState", "state: $myState")
    return myState
  }

  override fun loadState(stateLoadedFromPersistence: State) {
    consoleLog(ConsoleColors.ANSI_PURPLE,
               "KotlinDSLUISampleData.loadState",
               "stateLoadedFromPersistence: $stateLoadedFromPersistence")
    myState = stateLoadedFromPersistence
  }

  //
  // Properties in this class are bound to the Kotlin DSL UI.
  //

  class State {
    var myFlag: Boolean by object : LoggingProperty<State, Boolean>(false) {}
    var myString: String by object : LoggingProperty<State, String>("") {}
    var myInt: Int by object : LoggingProperty<State, Int>(0) {}
    var myStringChoice: String by object : LoggingProperty<State, String>("") {}

    override fun toString(): String =
        "State{ myFlag: '$myFlag', myString: '$myString', myInt: '$myInt', myStringChoice: '$myStringChoice' }"

    /** Factory class to generate synthetic properties, that log every access and mutation to each property. */
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




