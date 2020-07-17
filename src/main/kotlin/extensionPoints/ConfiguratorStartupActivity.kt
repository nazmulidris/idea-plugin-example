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

package extensionPoints

import ColorConsoleContext.Companion.whichThread
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import logWithHistory
import notify

/**
 * Create an ExtensionPointName given the namespace of the plugin and the name of the extension point itself. Note that
 * the namespace is "com .intellij" if IntelliJ Platform core functionality is extended, otherwise, it is the namespace
 * of the plugin itself.
 */
class EP_NAME {
  private val nameSpace = "com.developerlife.example.idea-plugin-example"
  private val name = "configuratorRunnable"
  private val fullyQualifiedName = "$nameSpace.$name"
  fun create(): ExtensionPointName<Runnable> = ExtensionPointName.create<Runnable>(fullyQualifiedName)
}

/**
 * An [StartupActivity] that loads all the extensions that are registered to the extension point. Note that this class
 * does not have to implement any IntelliJ platform interfaces.
 */
class ConfiguratorStartupActivity : StartupActivity {
  private val className: String = this::class.simpleName ?: "ConfiguratorStartupActivity"

  init {
    Pair("$className init", "startup activity is created").notify()
  }

  init {
    dumpThreadInfo()
    "$className created".logWithHistory()
    Pair(className, "created").notify()
  }

  override fun runActivity(project: Project) {
    dumpThreadInfo()
    EP_NAME().create().extensionList.forEach { it.run() }
  }

  private fun dumpThreadInfo() {
    Pair("$className init", whichThread()).notify()
  }

}