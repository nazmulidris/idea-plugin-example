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

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

/**
 * Here's an example of writing plugin tests from jetbrains.org website that
 * shows how to use [BasePlatformTestCase] and its [CodeInsightTestFixture]
 * property [myFixture] (which is used to load files into the IDE and
 * interrogate it) -
 * [https://www.jetbrains.org/intellij/sdk/docs/tutorials/writing_tests_for_plugins/completion_test.html]
 */
class LogServiceTest : BasePlatformTestCase() {

  @Before
  public override fun setUp() {
    super.setUp()
  }

  @Test
  fun testGetInstance() {
    assertThat(LogService.instance).isNotNull
  }

  @Test
  fun testGetState() {
    assertThat(LogService.instance.state.messageList).size()
        .isGreaterThan(0)
  }

  @Test
  fun testLoadAndGetState() {
    val state = LogService.State().apply {
      messageList.add("testLoadAndGetState1")
      messageList.add("testLoadAndGetState2")
    }
    LogService.instance.loadState(state)
    assertThat(LogService.instance.state).isEqualTo(state)
  }

  @Test
  fun testAddMessage() {
    LogService.instance.addMessage("testAddMessage")
    assertThat(LogService.instance.state.messageList).contains("testAddMessage")
  }
}