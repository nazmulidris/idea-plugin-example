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

package extensionPoints

import com.intellij.openapi.extensions.ExtensionPointName
import logWithHistory
import notify
import whichThread

/**
 * Create an ExtensionPointName given the namespace of the plugin and the
 * name of the extension point itself. Note that the namespace is "com
 * .intellij" if IntelliJ Platform core functionality is extended, otherwise,
 * it is the namespace of the plugin itself.
 */
object EP_NAME {
  private const val nameSpace = "com.developerlife.example.idea-plugin-example"
  private const val name = "configuratorRunnable"
  private const val fullyQualifiedName = "$nameSpace.$name"
  operator fun invoke(): ExtensionPointName<Runnable> =
      ExtensionPointName.create<Runnable>(fullyQualifiedName)
}

/**
 * An ApplicationComponent that loads all the extensions that are registered to
 * the extension point. Note that this class does not have to implement any
 * IntelliJ platform interfaces.
 */
class Configurator {

  init {
    dumpThreadInfo()
    val className = this::class.simpleName
    "$className created".logWithHistory()
    Pair("$className", "created").notify()
  }

  init {
    dumpThreadInfo()
    EP_NAME().extensionList.forEach { it.run() }
  }

  private fun dumpThreadInfo() {
    Pair("Configurator init", whichThread()).notify()
  }

}