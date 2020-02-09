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
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import printDebugHeader
import printlnAndLog
import sleep

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
    // TODO 🔥 Is it possible to simply test the main function invoked by the action itself in the test. 🔥
  }

  @Test
  fun testEditorReplaceLink() {
    printDebugHeader()

    // Load test file w/ text selected.
    myFixture.configureByFile(TestFile.Input(getTestName(false)))

    val action = EditorReplaceLink(object : urlshortenservice.ShortenUrlService {
      override fun shorten(longUrl: String) = "http://shorturl.com"
    })

    val presentation = myFixture.testAction(action)

    // The following code only executes after the entire action has run to completion! Basically the following code
    // does not do anything useful.
    while (true) {
      ANSI_BLUE("isRunning: ${action.isRunning()}, isCancelled: ${action.isCanceled()}").printlnAndLog()
      if (action.isRunning()) continue
      if (action.isCanceled()) break
      if (!action.isRunning()) break
      sleep()
    }

    assertThat(presentation.isEnabledAndVisible).isTrue()

    myFixture.checkResultByFile(TestFile.Output(getTestName(false)))
  }
}