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

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  id("org.jetbrains.intellij") version "0.4.10"
  kotlin("jvm") version "1.3.50"
}

group = "com.developerlife.example"
version = "1.0"

repositories {
  mavenCentral()
}

dependencies {
  implementation(kotlin("stdlib-jdk8"))
  implementation(kotlin("reflect"))
}

// See https://github.com/JetBrains/gradle-intellij-plugin/
intellij {
  // Unfortunately, this version does not support light services.
  version = "2019.2.3"

  // Disable this because tests fail w/ the latest snapshot.
  //version = "LATEST-EAP-SNAPSHOT"

  // Declare a dependency on the markdown plugin to be able to access the
  // MarkdownRecursiveElementVisitor.kt file. More info:
  // https://www.jetbrains.org/intellij/sdk/docs/basics/plugin_structure/plugin_dependencies.html
  // https://plugins.jetbrains.com/plugin/7793-markdown/versions
  setPlugins("java", "org.intellij.plugins.markdown:192.5728.98")
}
tasks.getByName<org.jetbrains.intellij.tasks.PatchPluginXmlTask>("patchPluginXml") {
  changeNotes("""
    Changelog:
    <ol>
      <li>1.0 - Empty plugin</li>
      <li>2.0 - Added features</li>
    </ol>""")
}
tasks.withType<KotlinCompile> {
  kotlinOptions.jvmTarget = "1.8"
}

// Testing
dependencies {
  testImplementation("org.assertj:assertj-core:3.11.1")
}