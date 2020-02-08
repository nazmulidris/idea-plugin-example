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

import TestFile
import TestUtils.Companion.computeBasePath
import com.intellij.testFramework.LightPlatformCodeInsightTestCase
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import java.io.File

/**
 * This test extends [LightPlatformCodeInsightTestCase] and not [BasePlatformTestCase]. Check out this example
 * [MarkdownToggleBoldTest.java](https://tinyurl.com/vrw8q4e). Couple of differences to note:
 * 1. The [computeBasePath] doesn't work for this fixture and needs a separator postfix.
 * 2. There is no need to check the action's presentation to see whether it has been enabled, since this is done for
 * you by [executeAction].
 */
class EditorReplaceTextActionTest : LightPlatformCodeInsightTestCase() {

  @Before
  public override fun setUp() {
    super.setUp()
    assertThat(testDataPath).isNotNull()
  }

  override fun getTestDataPath(): String {
    return computeBasePath() + File.separator
  }

  @Test
  fun testSelectedTextIsReplaced() {
    // Load test file w/ text selected.
    configureByFile(TestFile.Input)

    // Try and perform the action.
    executeAction("MyPlugin.EditorReplaceText")

    // Assert that the changes are what we expect by comparing it to output file.
    checkResultByFile(TestFile.Output)
  }
}