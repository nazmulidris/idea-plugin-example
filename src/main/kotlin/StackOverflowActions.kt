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

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.ui.Messages
import java.net.URLEncoder

class AskQuestionOnStackOverflowAction : AnAction() {
  override fun actionPerformed(event: AnActionEvent) {
    BrowserUtil.browse("https://stackoverflow.com/questions/ask")
  }

}

class SearchOnStackOverflowAction : AnAction() {
  override fun update(event: AnActionEvent) {
    with(event.getRequiredData(CommonDataKeys.EDITOR)) {
      val condition = caretModel.currentCaret.hasSelection()
      event.presentation.isEnabledAndVisible = condition
    }
  }

  override fun actionPerformed(event: AnActionEvent) {
    val langTag = with(event.getData(CommonDataKeys.PSI_FILE)) {
      this?.run {
        "+[${language.displayName.toLowerCase()}+]"
      }
    } ?: ""

    val selectedText = with(event.getRequiredData(CommonDataKeys.EDITOR)) {
      caretModel.currentCaret.selectedText
    }

    when (selectedText) {
      null -> {
        Messages.showMessageDialog(
            event.project,
            "Please select something before running this action",
            "Search on Stack Overflow",
            Messages.getWarningIcon())
      }
      else -> {
        val query = URLEncoder.encode(selectedText, "UTF-8") + langTag
        BrowserUtil.browse("https://stackoverflow.com/search?q=$query")
      }
    }
  }

}