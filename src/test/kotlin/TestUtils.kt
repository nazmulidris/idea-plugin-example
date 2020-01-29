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

@file:JvmName("TestUtils")

import TestUtils.Companion.computeBasePath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import java.io.File

/**
 * Files needed to be loaded from the plugin project's `testdata` directory.
 *
 * By default, IntelliJ Platform [BasePlatformTestCase] provides a location that
 * is invalid for use by 3rd party plugins (provided by
 * [BasePlatformTestCase.myFixture#basePath]). This assumes that the files
 * are in the classpath of the IntelliJ IDEA codebase itself.
 *
 * The [computeBasePath] function uses the classpath of this class in order
 * to locate where on disk, this class is loaded from. And then walks up the
 * path to locate the `testdata` folder. Also, note that this class uses an
 * annotation (`@file:JvmName()`) in order to explicitly set its own classname
 * and not use the computed `TestUtilsKt.class` (which would be the default
 * w/out using this annotation).
 */
class TestUtils {

  companion object {
    val testDataFolder = "testdata"
    /**
     * @throws [IllegalStateException] if the [testDataFolder] folder
     * can't be found somewhere on the classpath.
     */
    fun computeBasePath(): String {
      val urlFromClassloader =
          TestUtils::class.java.classLoader.getResource("TestUtils.class")
      checkNotNull(urlFromClassloader) { "Could not find $testDataFolder" }

      var path: File? = File(urlFromClassloader.toURI())
      while (path != null &&
             path.exists() &&
             !File(path, testDataFolder).isDirectory
      ) {
        path = path.parentFile
      }
      checkNotNull(path) { "Could not find $testDataFolder" }
      return File(path, testDataFolder).absolutePath
    }

  }
}

object TestFile {
  val Input = "input-file.md"
  val Output = "output-file.md"
}