/*
 * Copyright 2019 Nazmul Idris. All rights reserved.
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

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import logWithoutHistory
import whichThread
import java.util.concurrent.CopyOnWriteArrayList

@State(name = "LogServiceData", storages = [Storage("logServiceData.xml")])
object LogService : PersistentStateComponent<LogService.LogServiceState> {
  /**
   * This is used by IDEA to get a reference to the single instance of this
   * service (used by [ServiceManager]).
   */
  val instance: LogService
    get() = ServiceManager.getService(LogService::class.java)

  fun add(message: String) {
    with(ourState.messageList) {
      add(message)
      add("LogService: ${whichThread()}")
    }
  }

  override fun toString(): String {
    return with(ourState.messageList) {
      "messageList.size=$size" + "\n${joinToString(separator = "\n")}"
    }
  }

  // PersistentStateComponent implementation.
  //
  // The loadState() method is called by IDEA after the component has been
  // created (only if there is some non-default state persisted for the
  // component), and after the XML file with the persisted state is changed
  // externally (for example, if the project file was updated from the version
  // control system). In the latter case, the component is responsible for
  // updating the UI and other related components according to the changed
  // state.
  //
  // The getState() method is called by IDEA every time the settings are saved
  // (for example, on frame deactivation or when closing the IDE). If the state
  // returned from getState() is equal to the default state (obtained by
  // creating the state class with a default constructor), nothing is persisted
  // in the XML. Otherwise, the returned state is serialized in XML and stored.

  private var ourState = LogServiceState()

  /**
   * [More info](http://www.jetbrains.org/intellij/sdk/docs/basics/persisting_state_of_components.html)
   */
  data class LogServiceState(val messageList: CopyOnWriteArrayList<String> =
                                 CopyOnWriteArrayList()
  )

  /**
   * Called by IDEA to get the current state of this service, so that it can
   * be saved to persistence.
   */
  override fun getState(): LogServiceState {
    "IDEA called getState()".logWithoutHistory()
    return ourState
  }

  /**
   * Called by IDEA when new component state is loaded. The
   * [stateLoadedFromPersistence] object should be used directly, defensive
   * copying is not required.
   */
  override fun loadState(stateLoadedFromPersistence: LogServiceState) {
    "IDEA called loadState(stateLoadedFromPersistence)".logWithoutHistory()
    stateLoadedFromPersistence.messageList
        .joinToString(separator = ",", prefix = "{", postfix = "}")
        .logWithoutHistory()
    ourState = stateLoadedFromPersistence
  }

}
