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
package ui

import com.intellij.notification.*
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys

/**
   * [Docs](https://www.jetbrains.org/intellij/sdk/docs/user_interface_components/notifications.html).
 */
class ShowNotificationSampleAction : AnAction() {
  private val GROUP_DISPAY_ID = "UI Samples"
  private val messageTitle = "Title of notification"
  private val messageDetails = "Details of notification"

  override fun actionPerformed(e: AnActionEvent) {
    aNotification()
    anotherNotification(e)
  }

  /**
   * One way to show notifications.
   * 1) This notification won't be logged to "Event Log" tool window.
   * 2) And it is project specific.
   */
  private fun anotherNotification(e: AnActionEvent) {
    val project = e.getRequiredData(CommonDataKeys.PROJECT)
    val notificationGroup = NotificationGroup(GROUP_DISPAY_ID, NotificationDisplayType.BALLOON, false)
    val notification = notificationGroup
        .createNotification("2. $messageTitle",
                            "2. $messageDetails",
                            NotificationType.INFORMATION,
                            null)
    notification.notify(project)
  }

  /**
   * Another way to show notifications.
   * 1) This will be logged to "Event Log" and is not tied to a specific project.
   */
  private fun aNotification() {
    val notification = Notification(GROUP_DISPAY_ID,
                                    "1 .$messageTitle",
                                    "1 .$messageDetails",
                                    NotificationType.INFORMATION)
    Notifications.Bus.notify(notification)
  }
}