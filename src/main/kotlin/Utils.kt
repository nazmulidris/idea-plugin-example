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

import ColorConsoleContext.Companion.colorConsole
import Colors.*
import com.intellij.ide.plugins.PluginManager
import com.intellij.notification.Notification
import com.intellij.notification.NotificationListener.URL_OPENING_LISTENER
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ApplicationManager.getApplication
import services.LogService
import java.text.SimpleDateFormat
import java.util.*

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
                         URL_OPENING_LISTENER))

/**
 * DSL to print colorized console output. Here are examples of how to use this:
 * ```
 * fun main() {
 *   colorConsole {//this: ColorConsoleContext
 *     printLine {//this: MutableList<String>
 *       span(Purple, "word1")
 *       span("word2")
 *       span(Blue, "word3")
 *     }
 *     printLine {//this: MutableList<String>
 *       span(Green, "word1")
 *       span(Purple, "word2")
 *     }
 *     println(
 *         line {//this: MutableList<String>
 *           add(Green("word1"))
 *           add(Blue("word2"))
 *         })
 *   }
 * }
 * ```
 */
class ColorConsoleContext {
  companion object {
    fun colorConsole(block: ColorConsoleContext.() -> Unit) {
      ColorConsoleContext().apply(block)
    }

    /**
     * [ApplicationManager.getApplication#isDispatchThread] is equivalent to calling [java.awt.EventQueue.isDispatchThread].
     */
    fun whichThread() = buildString {
      append(
          when {
            getApplication().isDispatchThread -> Colors.Red("Running on EDT")
            else                              -> Colors.Green("Running on BGT")
          }
      )
      append(
          " - ${Thread.currentThread().name.take(50)}..."
      )
    }
  }

  fun printLine(block: MutableList<String>.() -> Unit) {
    if (isPluginInTestIDE() || isPluginInUnitTestMode()) {
      println(line {
        block(this)
      })
    }
  }

  fun line(block: MutableList<String>.() -> Unit): String {
    val messageFragments = mutableListOf<String>()
    block(messageFragments)
    val timestamp = SimpleDateFormat("hh:mm:sa").format(Date())
    return messageFragments.joinToString(separator = ", ", prefix = "$timestamp: ")
  }

  /**
   * Appends all arguments to the given [MutableList].
   */
  fun MutableList<String>.span(color: Colors, text: String): MutableList<String> {
    add(color.ansiCode + text + ANSI_RESET.ansiCode)
    return this
  }

  /**
   * Appends all arguments to the given [MutableList].
   */
  fun MutableList<String>.span(text: String): MutableList<String> {
    add(text + ANSI_RESET.ansiCode)
    return this
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

  fun printWhichThread() {
    colorConsole {
      printLine {
        span(whichThread())
      }
    }
  }
}

/** https://github.com/fusesource/jansi */
enum class Colors(val ansiCode: String) {
  ANSI_RESET("\u001B[0m"),
  Black("\u001B[30m"),
  Red("\u001B[31m"),
  Green("\u001B[32m"),
  Yellow("\u001B[33m"),
  Blue("\u001B[34m"),
  Purple("\u001B[35m"),
  Cyan("\u001B[36m"),
  White("\u001B[37m");

  operator fun invoke(content: String): String {
    return "${ansiCode}$content${ANSI_RESET.ansiCode}"
  }

  operator fun invoke(content: StringBuilder): StringBuilder {
    return StringBuilder("${ansiCode}$content${ANSI_RESET.ansiCode}")
  }
}

fun isPluginInTestIDE(): Boolean = System.getProperty("idea.is.internal")?.toBoolean() ?: false

fun isPluginInUnitTestMode(): Boolean = ApplicationManager.getApplication().isUnitTestMode