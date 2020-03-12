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
  // Information on IJ versions https://www.jetbrains.org/intellij/sdk/docs/reference_guide/intellij_artifacts.html
  // You can use release build numbers or snapshot name for the version.
  // 1) IJ Release Repository w/ build numbers https://www.jetbrains.com/intellij-repository/releases/
  // 2) IJ Snapshots Repository w/ snapshot names https://www.jetbrains.com/intellij-repository/snapshots/
  version = "193.6494.35" // You can also use LATEST-EAP-SNAPSHOT here.

  // Declare a dependency on the markdown plugin to be able to access the
  // MarkdownRecursiveElementVisitor.kt file. More info:
  // https://www.jetbrains.org/intellij/sdk/docs/basics/plugin_structure/plugin_dependencies.html
  // https://plugins.jetbrains.com/plugin/7793-markdown/versions
  setPlugins("java", "org.intellij.plugins.markdown:193.5233.63")
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