package com.anthropic.claude.plugin.actions

import com.anthropic.claude.plugin.api.ClaudeCodeService
import com.anthropic.claude.plugin.settings.ClaudeSettings
import com.intellij.diff.DiffContentFactory
import com.intellij.diff.DiffManager
import com.intellij.diff.contents.DocumentContent
import com.intellij.diff.requests.SimpleDiffRequest
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessListener
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.wm.ToolWindowManager
import java.nio.charset.StandardCharsets
import javax.swing.JPanel
import javax.swing.JTextArea
import javax.swing.JTextField

class AskClaudeAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val selectedText = editor.selectionModel.selectedText
        
        if (selectedText.isNullOrBlank()) {
            Messages.showWarningDialog(project, "Please select some code to send to Claude", "No Selection")
            return
        }
        
        // Show dialog to get prompt
        val prompt = Messages.showInputDialog(
            project,
            "What would you like to ask Claude about this code?",
            "Ask Claude Code",
            null
        ) ?: return
        
        // Get virtual file
        val virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return
        val filePath = virtualFile.path
        
        // Run Claude Code
        ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Running Claude Code", true) {
            override fun run(indicator: ProgressIndicator) {
                indicator.text = "Running Claude Code"
                val claudeService = ClaudeCodeService.getInstance(project)
                
                val processListener = object : ProcessListener {
                    private val outputBuilder = StringBuilder()
                    
                    override fun startNotified(event: ProcessEvent) {
                        // Process started
                    }
                    
                    override fun processTerminated(event: ProcessEvent) {
                        val output = outputBuilder.toString()
                        
                        // Open the Claude tool window to show the response
                        ApplicationManager.getApplication().invokeLater {
                            val toolWindow = ToolWindowManager.getInstance(project).getToolWindow("Claude Code")
                            toolWindow?.show {}
                            
                            // Parse output for file changes
                            // This is simplified - in reality you would parse JSON or structured output
                            if (output.contains("file_change")) {
                                // Extract changed content
                                val newContent = extractNewContent(output)
                                if (newContent != null) {
                                    showDiffAndApplyChange(filePath, newContent, virtualFile)
                                }
                            }
                        }
                    }
                    
                    override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {
                        outputBuilder.append(event.text)
                    }
                }
                
                // Combine selected text with prompt
                val fullPrompt = "User selected this code:\n\n```\n$selectedText\n```\n\nUser prompt: $prompt"
                
                val processHandler = claudeService.executeClaudeCodeWithChanges(fullPrompt, filePath, indicator, processListener)
                processHandler.startNotify()
                
                while (!processHandler.isProcessTerminated) {
                    try {
                        Thread.sleep(100)
                    } catch (e: InterruptedException) {
                        break
                    }
                }
            }
        })
    }
    
    private fun extractNewContent(output: String): String? {
        // This is a simplified extraction - in reality, use proper JSON parsing
        val startMarker = "<new_content>"
        val endMarker = "</new_content>"
        val startIndex = output.indexOf(startMarker)
        val endIndex = output.indexOf(endMarker)
        
        return if (startIndex >= 0 && endIndex > startIndex) {
            output.substring(startIndex + startMarker.length, endIndex)
        } else null
    }
    
    private fun showDiffAndApplyChange(filePath: String, newContent: String, virtualFile: com.intellij.openapi.vfs.VirtualFile) {
        ApplicationManager.getApplication().invokeLater {
            // Create diff contents
            val contentFactory = DiffContentFactory.getInstance()
            
            // Get original content
            val originalContent: DocumentContent = contentFactory.create(virtualFile.contentsToByteArray().toString(StandardCharsets.UTF_8))
            
            // Create new content
            val newContentDoc = contentFactory.create(newContent)
            
            // Show diff
            val diffRequest = SimpleDiffRequest(
                "Changes for ${virtualFile.name}",
                originalContent,
                newContentDoc,
                "Original",
                "Modified by Claude Code"
            )
            
            DiffManager.getInstance().showDiff(virtualFile.fileSystem.refreshAndFindFileByPath(filePath).project, diffRequest) { result ->
                if (result) {
                    ApplicationManager.getApplication().runWriteAction {
                        virtualFile.setBinaryContent(newContent.toByteArray(StandardCharsets.UTF_8))
                    }
                }
            }
        }
    }
    
    override fun update(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR)
        e.presentation.isEnabledAndVisible = editor != null && editor.selectionModel.hasSelection()
    }
}
