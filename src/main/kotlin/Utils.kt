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

import color_console_log.ColorConsoleContext.Companion.colorConsole
import color_console_log.Colors.*
import com.intellij.ide.plugins.PluginManager
import com.intellij.notification.Notification
import com.intellij.notification.NotificationListener.URL_OPENING_LISTENER
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ApplicationManager.getApplication
import services.LogService

fun printWhichThread() {
  colorConsole {
    printLine {
      span(whichThread())
    }
  }
}

/**
 * [ApplicationManager.getApplication#isDispatchThread] is equivalent to calling [java.awt.EventQueue.isDispatchThread].
 */
fun whichThread() = buildString {
  append(
      when {
        getApplication().isDispatchThread -> Red("Running on EDT")
        else                              -> Green("Running on BGT")
      }
  )
  append(
      " - ${Thread.currentThread().name.take(50)}..."
  )
}

fun printDebugHeader() {
  val stackTrace = Thread.currentThread().stackTrace[2]
  colorConsole {
    printLine {
      span(Cyan, "[${stackTrace.className}]")
      span(Yellow, "${stackTrace.methodName}()")
    }
  }
}

fun sleep(durationMs: Long = 100) {
  val formattedDuration = "%.3f sec".format(durationMs / 1000f)

  colorConsole {
    printLine {
      printWhichThread()
      span(Blue, " sleeping for $formattedDuration 😴")
    }
  }

  Thread.sleep(durationMs)

  colorConsole {
    printLine {
      printWhichThread()
      span(Green, " awake 😳")
    }
  }
}

fun longSleep() {
  sleep(100 * 20)
}

fun shortSleep() {
  sleep(20)
}

fun String.printlnAndLog() {
  log()
  println(Cyan("MyPlugin: ") + this)
}

/**
 * Write this to the idea.log file, located in: $PROJECT_DIR/build/idea-sandbox/system/log
 */
fun String.log() {
  PluginManager.getLogger().info(Cyan("MyPlugin: ") + this)
}

/**
 * Write this to the idea.log file, located in: $PROJECT_DIR/build/idea-sandbox/system/log
 */
fun String.logWithHistory() {
  PluginManager.getLogger().info("MyPlugin: $this")
  LogService.instance.addMessage(this)
  Pair("LogService", "add('$this') called, ${LogService.instance}").notify()
}

fun String.logWithoutHistory() {
  PluginManager.getLogger().info("MyPlugin: $this")
  Pair("logWithoutHistory", this).notify()
}

const val GROUP_DISPAY_ID = "MyPlugin.Group"

/**
 * Generate a Notification in IDEA using the first (maps to title) and second (maps to content) properties of the Pair.
 */
fun Pair<String, String>.notify() = com.intellij.notification
    .Notifications.Bus
    .notify(Notification(GROUP_DISPAY_ID,
                         first,
                         second,
                         NotificationType.INFORMATION,
                         URL_OPENING_LISTENER))

fun isPluginInTestIDE(): Boolean = System.getProperty("idea.is.internal")?.toBoolean() ?: false

fun isPluginInUnitTestMode(): Boolean = ApplicationManager.getApplication().isUnitTestMode