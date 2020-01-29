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

package onProjectOpen

import Colors.*
import com.intellij.AppTopics
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileDocumentManagerListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileContentChangeEvent
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.intellij.openapi.vfs.newvfs.events.VFilePropertyChangeEvent
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import printDebugHeader
import printlnAndLog
import whichThread

@Service
class FileManagerLightService(
    /** One instance of this service is created per project. */
    private val project: Project
) {

  companion object {
    /**
     * This is used by IDEA to get a reference to the single instance of this
     * service (used by [ServiceManager]).
     */
    fun getInstance(project: Project): FileManagerLightService {
      return ServiceManager
          .getService(project, FileManagerLightService::class.java)
    }
  }

  fun init() {
    "LightService.init() run w/ project: $project".printlnAndLog()
    logListOfProjectVFilesByExt()
    logListOfProjectVFilesByName()
    logListOfAllProjectVFiles()
    attachListenerForProjectVFileChanges()
    attachFileSaveListener()
  }

  /**
   * - [Tutorial](http://arhipov.blogspot.com/2011/04/code-snippet-intercepting-on-save.html)
   * - [FileDocumentManagerListener]
   * - [AppTopics]
   * - [`Document docs`](https://www.jetbrains.org/intellij/sdk/docs/basics/architectural_overview/documents.html#how-do-i-get-notified-when-documents-change)
   */
  private fun attachFileSaveListener() {
    printDebugHeader()

    val connection = project.messageBus.connect(/*parentDisposable=*/ project)
    connection.subscribe(
        AppTopics.FILE_DOCUMENT_SYNC,
        object : FileDocumentManagerListener {
          override fun beforeDocumentSaving(document: Document) {
            val vFile = FileDocumentManager.getInstance().getFile(document)
            ANSI_BLUE(buildString {
              append("A VirtualFile is about to be saved\n")
              append("\tvFile: $vFile\n")
              append("\tdocument: $document\n")
            }).printlnAndLog()
          }
        })
  }

  /**
   * VFS listeners are application level and will receive events for changes
   * happening in all the projects opened by the user. You may need to filter
   * out events that aren’t relevant to your task (e.g., via
   * `ProjectFileIndex#isInContent()`).
   *
   * A listener for VFS events, invoked inside write-action.
   *
   * To register this listener, use e.g.
   * ```
   * project
   *   .getMessageBus()
   *   .connect(disposable)
   *   .subscribe(VirtualFileManager.VFS_CHANGES, listener)
   * ```
   */
  private fun attachListenerForProjectVFileChanges(): Unit {
    printDebugHeader()

    fun handleEvent(event: VFileEvent) {
      when (event) {
        is VFilePropertyChangeEvent -> {
          ANSI_GREEN("VFile property change event: $event").printlnAndLog()
        }
        is VFileContentChangeEvent  -> {
          ANSI_GREEN("VFile content change event: $event").printlnAndLog()
        }
      }
    }

    fun doAfter(events: List<VFileEvent>) {
      ANSI_BLUE("VFS_CHANGES: #events: ${events.size}").printlnAndLog()
      val projectFileIndex = ProjectRootManager.getInstance(project).fileIndex
      events.withIndex().forEach { (index, event) ->
        ANSI_GREEN("$index. VFile event: $event").printlnAndLog()
        // Filter out file events that are not in the project's content.
        events
            .filter { it.file != null && projectFileIndex.isInContent(it.file!!) }
            .forEach { handleEvent(it) }
      }
    }

    val connection = project.messageBus.connect(/*parentDisposable=*/ project)
    connection.subscribe(
        VirtualFileManager.VFS_CHANGES,
        object : BulkFileListener {
          override fun after(events: List<VFileEvent>) = doAfter(events)
        })
  }

  private fun logListOfAllProjectVFiles() {
    printDebugHeader()

    ANSI_RED(whichThread()).printlnAndLog()
    buildString {
      getListOfAllProjectVFiles(project)
          .withIndex()
          .forEach { (index: Int, virtualFile: VirtualFile) ->
            append(convertVFileToString(index, virtualFile))
          }
    }.printlnAndLog()
  }

  private fun logListOfProjectVFilesByName() {
    printDebugHeader()

    ANSI_RED(whichThread()).printlnAndLog()
    buildString {
      getListOfProjectVFilesByName(project, fileName = "Lambdas.kt")
          .withIndex()
          .forEach { (index: Int, virtualFile: VirtualFile) ->
            append(convertVFileToString(index, virtualFile))
          }
    }.printlnAndLog()
  }

  private fun logListOfProjectVFilesByExt() {
    printDebugHeader()

    ANSI_RED(whichThread()).printlnAndLog()
    buildString {
      getListOfProjectVFilesByExt(project).withIndex()
          .forEach { (index: Int, virtualFile: VirtualFile) ->
            append(convertVFileToString(index, virtualFile))
          }
    }.printlnAndLog()
  }

  private fun convertVFileToString(index: Int,
                                   virtualFile: VirtualFile
  ): String {
    return "VirtualFile[$index]: " +
           ANSI_BLUE("\n name: '${virtualFile.name}'") +
           ANSI_GREEN("\n path: '${virtualFile.path}'") +
           ANSI_YELLOW("\n parent.path: '${virtualFile.parent.path}'") +
           "\n"
  }

  fun getListOfProjectVFilesByExt(project: Project,
                                  caseSensitivity: Boolean = true,
                                  extName: String = "kt"
  ): MutableCollection<VirtualFile> {
    val scope = GlobalSearchScope.projectScope(project)
    return FilenameIndex.getAllFilesByExt(project, extName, scope)
  }

  fun getListOfProjectVFilesByName(project: Project,
                                   caseSensitivity: Boolean = true,
                                   fileName: String
  ): MutableCollection<VirtualFile> {
    val scope = GlobalSearchScope.projectScope(project)
    return FilenameIndex.getVirtualFilesByName(
        project, fileName, caseSensitivity, scope)
  }

  fun getListOfAllProjectVFiles(project: Project): MutableCollection<VirtualFile> {
    val collection = mutableListOf<VirtualFile>()
    ProjectFileIndex.getInstance(project).iterateContent {
      collection += it
      // Return true to process all the files (no early escape).
      true
    }
    return collection
  }

}