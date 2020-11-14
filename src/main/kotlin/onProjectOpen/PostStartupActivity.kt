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

package onProjectOpen

import color_console_log.ColorConsoleContext.Companion.colorConsole
import color_console_log.Colors.Cyan
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity

internal class PostStartupActivity : StartupActivity {

  /**
   * This runs every time a project is opened in IDEA.
   * [More info](https://tinyurl.com/ufd64mk)
   */
  override fun runActivity(project: Project) {
    colorConsole {
      printLine {
        span(Cyan, "onProjectOpen.PostStartupActivity runActivity()")
      }
    }
    FileManagerLightService.getInstance(project).init()
  }
}