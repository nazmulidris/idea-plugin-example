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

package services

import Colors.ANSI_BLUE
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import logWithoutHistory
import printDebugHeader
import printlnAndLog
import whichThread
import java.util.concurrent.CopyOnWriteArrayList

@State(name = "LogServiceData", storages = [Storage("logServiceData.xml")])
object LogService : PersistentStateComponent<LogService.State> {
  /**
   * This is used by IDEA to get a reference to the single instance of this
   * service (used by [ServiceManager]).
   */
  val instance: LogService
    get() = ServiceManager.getService(LogService::class.java)

  fun addMessage(message: String) {
    with(state.messageList) {
      add(message)
      add("LogService: ${whichThread()}")
    }
  }

  override fun toString(): String {
    return with(state.messageList) {
      "messageList.size=$size" + "\n${joinToString(separator = "\n")}"
    }
  }

  /*
   * PersistentStateComponent implementation.
   *
   * The loadState() method is called by IDEA after the component has been created (only if there is some non-default
   * state persisted for the component), and after the XML file with the persisted state is changed externally (for
   * example, if the project file was updated from the version control system). In the latter case, the component is
   * responsible for updating the UI and other related components according to the changed state.
   *
   * The getState() method is called by IDEA every time the settings are saved (for example, on frame deactivation or
   * when closing the IDE). If the state returned from getState() is equal to the default state (obtained by creating
   * the state class with a default constructor), nothing is persisted in the XML. Otherwise, the returned state is
   * serialized in XML and stored.
   */

  private var state = State()

  data class State(
      var messageList: MutableList<String> =
          CopyOnWriteArrayList()
  )

  /**
   * Called by IDEA to get the current state of this service, so that it can
   * be saved to persistence.
   */
  override fun getState(): State {
    "IDEA called getState()".logWithoutHistory()
    return state
  }

  /**
   * Called by IDEA when new component state is loaded. This state object should
   * be used directly, defensive copying is not required.
   */
  override fun loadState(stateLoadedFromPersistence: State) {
    "IDEA called loadState(stateLoadedFromPersistence)".logWithoutHistory()
    val logMessages = stateLoadedFromPersistence
        .messageList
        .joinToString(separator = ",", prefix = "{", postfix = "}")
    printDebugHeader()
    ANSI_BLUE(logMessages).printlnAndLog()
    state = stateLoadedFromPersistence
  }

}
