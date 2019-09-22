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

import com.intellij.openapi.components.ServiceManager
import notify
import java.util.concurrent.CopyOnWriteArrayList

object LogService {
  /**
   * This is used by IDEA to get a reference to the single instance of this
   * service (used by [ServiceManager]).
   */
  val instance: LogService
    get() = ServiceManager.getService(LogService::class.java)

  private val messageList = CopyOnWriteArrayList<String>()

  fun add(message: String) {
    messageList.add(message)
    Pair("LogService",
         "add('$message') called, messageList.size= ${messageList.size}")
        .notify()
  }

}
