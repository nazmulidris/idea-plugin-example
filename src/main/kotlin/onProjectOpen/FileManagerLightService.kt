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

import ColorConsoleContext.Companion.colorConsole
import Colors.*
import com.intellij.AppTopics
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.ServiceManager.getService
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileDocumentManagerListener
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileContentChangeEvent
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.intellij.openapi.vfs.newvfs.events.VFilePropertyChangeEvent
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import org.intellij.plugins.markdown.lang.MarkdownTokenTypeSets
import psi.*
import ui.KotlinDSLUISampleService
import urlshortenservice.ShortenUrlService
import urlshortenservice.TinyUrl

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
    fun getInstance(project: Project) = getService(project, FileManagerLightService::class.java)
  }

  fun init(shortenUrlService: ShortenUrlService = TinyUrl()) {
    colorConsole {
      printLine {
        span(Cyan, "LightService.init()")
        span(Green, "run w/ project: $project")
      }
    }
    logListOfProjectVFilesByExt()
    logListOfProjectVFilesByName()
    logListOfAllProjectVFiles()
    attachListenerForProjectVFileChanges()
    attachFileSaveListener(shortenUrlService)
  }

  /**
   * - [Tutorial](http://arhipov.blogspot.com/2011/04/code-snippet-intercepting-on-save.html)
   * - [FileDocumentManagerListener]
   * - [AppTopics]
   * - [`Document docs`](https://www.jetbrains.org/intellij/sdk/docs/basics/architectural_overview/documents.html#how-do-i-get-notified-when-documents-change)
   */
  private fun attachFileSaveListener(shortenUrlService: ShortenUrlService) {
    colorConsole {
      printDebugHeader()
    }

    class ReplaceLongLinksInMarkdownFileOnSave(val shortenUrlService: ShortenUrlService) {
      fun execute(document: Document) {
        val vFile = FileDocumentManager.getInstance().getFile(document)

        colorConsole {
          printDebugHeader()
          printWhichThread()
          printLine {
            span(Red, "project: $project")
          }
          printLine {
            span(Blue, buildString {
              append("A VirtualFile is about to be saved\n")
              append("\tvFile: $vFile\n")
              append("\tdocument: $document\n")
            })
          }
        }

        if (KotlinDSLUISampleService.instance.myState.myFlag) {
          object : Task.Backgroundable(project, "🔥 Run background task on save Markdown file 🔥") {
            override fun run(indicator: ProgressIndicator) = doWorkInBackground(document, indicator, project)
          }.queue()
        }
        else {
          colorConsole {
            printLine {
              span(Red, "⚠️ myFlag is false -> do nothing ⚠️")
            }
          }
        }
      }

      private fun doWorkInBackground(document: Document, indicator: ProgressIndicator, project: Project) {
        colorConsole {
          printDebugHeader()
          printWhichThread()
        }

        val checkCancelled = CheckCancelled(indicator, project)
        val markdownPsiFile = runReadAction { getMarkdownPsiFile(document) }

        markdownPsiFile?.apply {
          val linkNodes = runReadAction { getAllLongLinks(markdownPsiFile, checkCancelled) }

          // Do this in background thread: make blocking calls that perform network IO.
          run {
            colorConsole {
              printLine {
                span(Red, "️⚠️ Shorten links ⚠️")
                span(Green, "size: ${linkNodes.size}")
                span(Blue, linkNodes.toString())
              }
            }
            linkNodes.forEach { linkNode: LinkNode ->
              linkNode.linkDestination = shortenUrlService.shorten(linkNode.linkDestination)
              checkCancelled.invoke()
            }
          }

          // Mutate the PSI in this write command action.
          // - The write command action enables undo.
          // - The lambda inside of this call runs in the EDT.
          WriteCommandAction.runWriteCommandAction(project) {
            if (!markdownPsiFile.isValid) return@runWriteCommandAction
            colorConsole {
              printWhichThread()
              printLine {
                span(Green, "🔥 Running write action to replace links 🔥")
              }
            }
            linkNodes.forEach { replaceExistingLinkWith(project, it, checkCancelled) }
          }
        }
      }

      private fun getMarkdownPsiFile(document: Document): PsiFile? {
        colorConsole {
          printWhichThread()
        }
        val psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document)
        psiFile?.apply {
          val viewProvider = psiFile.viewProvider
          val langs = viewProvider.languages
          if (langSetContains(langs, "Markdown")) return psiFile
        }
        return null
      }

      private fun getAllLongLinks(psiFile: PsiFile, checkCancelled: CheckCancelled): List<LinkNode> {
        val links = mutableListOf<LinkNode>()

        colorConsole {
          printLine {
            span(Red, "🔥 Process Markdown file 🔥")
          }
        }

        val linkElements = findAllChildElements(psiFile, MarkdownTokenTypeSets.LINKS, checkCancelled)
        // The following line does the same thing as above:
        // val collectedLinks = PsiTreeUtil.collectElementsOfType(psiFile, MarkdownLinkDestinationImpl::class.java)

        colorConsole {
          printLine {
            span(Purple, "size of collected link elements: ")
            span(Blue, linkElements.size.toString())
          }
        }

        linkElements.forEach { element ->
          val linkNode = findLink(element, psiFile, checkCancelled)
          colorConsole {
            printLine {
              span(Purple, "linkNode")
              span(Blue, "${linkNode ?: "null"}")
            }
          }
          if (shouldAccept(linkNode)) links.add(linkNode!!)
        }

        return links
      }

      private fun shouldAccept(linkNode: LinkNode?): Boolean = when {
        linkNode == null                                           -> false
        linkNode.linkDestination.startsWith("https://tinyurl.com") -> false
        linkNode.linkDestination.startsWith("http")                -> true
        else                                                       -> false
      }
    }

    val replaceLongLinksInMarkdownFileOnSave = ReplaceLongLinksInMarkdownFileOnSave(shortenUrlService)
    val connection = project.messageBus.connect(/*parentDisposable=*/ project)
    connection.subscribe(AppTopics.FILE_DOCUMENT_SYNC, object : FileDocumentManagerListener {
      override fun beforeDocumentSaving(document: Document) = replaceLongLinksInMarkdownFileOnSave.execute(document)
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
    colorConsole {
      printDebugHeader()
    }

    fun handleEvent(event: VFileEvent) {
      when (event) {
        is VFilePropertyChangeEvent -> {
          colorConsole {
            printLine {
              span(Green, "VFile property change event: $event")
            }
          }
        }
        is VFileContentChangeEvent  -> {
          colorConsole {
            printLine {
              span(Blue, "VFile content change event: $event")
            }
          }
        }
      }
    }

    fun doAfter(events: List<VFileEvent>) {
      colorConsole {
        printLine {
          span(Blue, "VFS_CHANGES: #events: ${events.size}")
        }
      }
      val projectFileIndex = ProjectRootManager.getInstance(project).fileIndex
      events.withIndex().forEach { (index, event) ->
        colorConsole {
          printLine {
            span(Green, "$index. VFile event: $event")
          }
        }
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
    colorConsole {
      printDebugHeader()
      printWhichThread()
      printLine {
        span(Yellow, "logListOfAllProjectVFiles: ")
        span(Blue, buildString {
          getListOfAllProjectVFiles(project)
              .withIndex()
              .forEach { (index: Int, virtualFile: VirtualFile) ->
                append(convertVFileToString(index, virtualFile))
              }
        })
      }
    }
  }

  private fun logListOfProjectVFilesByName() {
    colorConsole {
      printDebugHeader()
      printWhichThread()
      printLine {
        span(Yellow, "logListOfProjectVFilesByName: ")
        span(Blue, buildString {
          getListOfProjectVFilesByName(project, fileName = "Lambdas.kt")
              .withIndex()
              .forEach { (index: Int, virtualFile: VirtualFile) ->
                append(convertVFileToString(index, virtualFile))
              }
        })
      }
    }
  }

  private fun logListOfProjectVFilesByExt() {
    colorConsole {
      printDebugHeader()
      printWhichThread()
      printLine {
        span(Yellow, "logListOfProjectVFilesByExt: ")
        span(Blue, buildString {
          getListOfProjectVFilesByExt(project).withIndex()
              .forEach { (index: Int, virtualFile: VirtualFile) ->
                append(convertVFileToString(index, virtualFile))
              }
        })
      }
    }
  }

  private fun convertVFileToString(index: Int,
                                   virtualFile: VirtualFile
  ): String {
    return "VirtualFile[$index]: " +
           Blue("\n name: '${virtualFile.name}'") +
           Green("\n path: '${virtualFile.path}'") +
           Yellow("\n parent.path: '${virtualFile.parent.path}'") +
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
