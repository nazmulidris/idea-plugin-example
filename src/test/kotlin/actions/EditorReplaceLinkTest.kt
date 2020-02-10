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

package actions

import Colors.ANSI_BLUE
import TestFile
import TestUtils.Companion.computeBasePath
import actions.EditorReplaceLink.RunningState.*
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import printDebugHeader
import printlnAndLog
import shortSleep
import urlshortenservice.ShortenUrlService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

val shortenUrlService = object : ShortenUrlService {
  override fun shorten(longUrl: String) = "http://shorturl.com"
}

class EditorReplaceLinkTest : BasePlatformTestCase() {

  @Before
  public override fun setUp() {
    super.setUp()
    assertThat(testDataPath).isNotNull()
  }

  override fun getTestDataPath(): String {
    return computeBasePath()
  }

  @Test
  fun testUnderlyingFunctionUsedByTestEditorReplaceLinkAction() {
    printDebugHeader()

    val psiFile = myFixture.configureByFile(TestFile.Input(getTestName(false)))
    val project = myFixture.project
    val editor = myFixture.editor

    val action = EditorReplaceLink(shortenUrlService)
    val result = action.doWorkInBackground(editor, psiFile, project)
    assertThat(result).isTrue()

    val presentation = myFixture.testAction(action)
    assertThat(presentation.isEnabledAndVisible).isTrue()

    myFixture.checkResultByFile(TestFile.Output(getTestName(false)))
  }

  /**
   * [Further reading on Future and Executor](https://www.callicoder.com/java-callable-and-future-tutorial/)
   */
  @Test
  fun testTheActionByConnectingWithTinyUrlServiceLive() {
    printDebugHeader()

    myFixture.configureByFile(TestFile.Input(getTestName(false)))

    val action = EditorReplaceLink()

    val executor = Executors.newSingleThreadExecutor()
    val future = executor.submit {
      while (true) {
        ANSI_BLUE("executor: isRunning: ${action.isRunning()}, isCancelled: ${action.isCanceled()}").printlnAndLog()
        if (action.isRunning() == NOT_STARTED) {
          shortSleep()
          continue
        }
        if (action.isRunning() == IS_CANCELLED || action.isRunning() == HAS_STOPPED) {
          executor.shutdown()
          break
        }
        else shortSleep()
      }
    }

    val presentation = myFixture.testAction(action)
    assertThat(presentation.isEnabledAndVisible).isTrue()

    myFixture.checkResultByFile(TestFile.Output(getTestName(false)))

    ANSI_BLUE("executor: future.isDone: ${future.isDone}").printlnAndLog()
    executor.awaitTermination(30, TimeUnit.SECONDS)
    ANSI_BLUE("executor: future.isDone: ${future.isDone}").printlnAndLog()
    executor.shutdown()
  }

  @Test
  fun testEditorReplaceLink() {
    printDebugHeader()

    myFixture.configureByFile(TestFile.Input(getTestName(false)))

    val action = EditorReplaceLink(shortenUrlService)

    val presentation = myFixture.testAction(action)
    assertThat(presentation.isEnabledAndVisible).isTrue()

    myFixture.checkResultByFile(TestFile.Output(getTestName(false)))
  }
}