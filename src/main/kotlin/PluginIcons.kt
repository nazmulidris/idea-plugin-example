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

import com.intellij.openapi.util.IconLoader
import javax.swing.Icon

/**
Provides programmatic access to the SVG icons, in the `resources`
folder, eg: `/icons/ic_check_circle.svg`
 */
interface PluginIcons {
  val HELLO_ACTION: Icon
    get() = IconLoader.getIcon("/icons/ic_check_circle.svg")
}