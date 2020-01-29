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

import com.intellij.ide.plugins.PluginManager
import com.intellij.notification.Notification
import com.intellij.notification.NotificationListener.UrlOpeningListener
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import services.LogService

fun printDebugHeader() {
  val stackTrace = Thread.currentThread().stackTrace[2]
  Colors.ANSI_PURPLE(
      "MyPlugin: ${stackTrace.className}.${stackTrace.methodName}()")
      .printlnAndLog()
}

fun String.printlnAndLog() {
  log()
  println(this)
}

/**
 * Write this to the idea.log file, located in:
 * $PROJECT_DIR/build/idea-sandbox/system/log
 */
fun String.log() {
  PluginManager.getLogger().info("MyPlugin: $this")
}

/**
 * Write this to the idea.log file, located in:
 * $PROJECT_DIR/build/idea-sandbox/system/log
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
 * Generate a Notification in IDEA using the first (maps to title) and second
 * (maps to content) properties of the Pair.
 */
fun Pair<String, String>.notify() = com.intellij.notification
    .Notifications.Bus
    .notify(Notification(GROUP_DISPAY_ID,
                         first,
                         second,
                         NotificationType.INFORMATION,
                         UrlOpeningListener(true)))

/**
 * [ApplicationManager.getApplication#isDispatchThread] is equivalent to calling
 * [java.awt.EventQueue.isDispatchThread].
 */
fun whichThread(): String =
    (if (ApplicationManager.getApplication().isDispatchThread) "Running on EDT"
    else "Running BGT") + " - ${Thread.currentThread().name.take(25)}..."

/**
 * https://github.com/fusesource/jansi
 */
enum class Colors(val ansiCode: String) {
  ANSI_RESET("\u001B[0m"),
  ANSI_BLACK("\u001B[30m"),
  ANSI_RED("\u001B[31m"),
  ANSI_GREEN("\u001B[32m"),
  ANSI_YELLOW("\u001B[33m"),
  ANSI_BLUE("\u001B[34m"),
  ANSI_PURPLE("\u001B[35m"),
  ANSI_CYAN("\u001B[36m"),
  ANSI_WHITE("\u001B[37m");

  operator fun invoke(content: String): String {
    return "${ansiCode}$content${ANSI_RESET.ansiCode}"
  }

  operator fun invoke(content: StringBuilder): StringBuilder {
    return StringBuilder("${ansiCode}$content${ANSI_RESET.ansiCode}")
  }
}