package com.anthropic.claude.plugin.ui

import com.anthropic.claude.plugin.api.ClaudeCodeService
import com.anthropic.claude.plugin.settings.ClaudeSettings
import com.intellij.diff.DiffContentFactory
import com.intellij.diff.DiffManager
import com.intellij.diff.contents.DocumentContent
import com.intellij.diff.requests.SimpleDiffRequest
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessListener
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextArea
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.nio.charset.StandardCharsets
import java.util.regex.Pattern
import javax.swing.*
import org.apache.commons.lang.StringEscapeUtils

class ClaudeToolWindow(private val project: Project) : Disposable {
    val component: JPanel = JPanel(BorderLayout())
    private val promptField = JBTextField()
    private val responseArea = JBTextArea().apply {
        isEditable = false
        lineWrap = true
        wrapStyleWord = true
    }
    private val statusLabel = JLabel("Ready")
    private val sendButton = JButton("Ask Claude")
    
    init {
        // Set up UI components
        val topPanel = JPanel(BorderLayout())
        topPanel.add(JLabel("Enter your prompt:"), BorderLayout.WEST)
        topPanel.add(promptField, BorderLayout.CENTER)
        topPanel.add(sendButton, BorderLayout.EAST)
        component.add(topPanel, BorderLayout.NORTH)
        
        val scrollPane = JBScrollPane(responseArea)
        component.add(scrollPane, BorderLayout.CENTER)
        
        val statusPanel = JPanel(BorderLayout())
        statusPanel.add(statusLabel, BorderLayout.WEST)
        component.add(statusPanel, BorderLayout.SOUTH)
        
        // Set up action
        sendButton.addActionListener {
            askClaude()
        }
        
        promptField.addActionListener {
            askClaude()
        }
        
        // Beautify the UI
        component.border = JBUI.Borders.empty(5)
        topPanel.border = JBUI.Borders.empty(0, 0, 5, 0)
        statusPanel.border = JBUI.Borders.empty(5, 0, 0, 0)
    }
    
    private fun askClaude() {
        val prompt = promptField.text
        if (prompt.isBlank()) return
        
        statusLabel.text = "Processing..."
        responseArea.text = ""
        
        // Using ProgressManager to show a progress indicator
        ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Running Claude Code", true) {
            override fun run(indicator: ProgressIndicator) {
                indicator.text = "Running Claude Code"
                val claudeService = ClaudeCodeService.getInstance(project)
                
                val processListener = object : ProcessListener {
                    private val outputBuilder = StringBuilder()
                    
                    override fun startNotified(event: ProcessEvent) {
                        ApplicationManager.getApplication().invokeLater {
                            statusLabel.text = "Running..."
                        }
                    }
                    
                    override fun processTerminated(event: ProcessEvent) {
                        val output = outputBuilder.toString()
                        ApplicationManager.getApplication().invokeLater {
                            statusLabel.text = "Completed"
                            processClaudeOutput(output)
                        }
                    }
                    
                    override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {
                        outputBuilder.append(event.text)
                        ApplicationManager.getApplication().invokeLater {
                            responseArea.append(event.text)
                        }
                    }
                }
                
                val processHandler = claudeService.executeClaudeCode(prompt, indicator, processListener)
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
    
    private fun processClaudeOutput(output: String) {
        // This method would handle parsing and applying file changes
        // For example, looking for file change commands in the output
        val fileChangePattern = Pattern.compile("<file_change>(.*?)</file_change>", Pattern.DOTALL)
        val matcher = fileChangePattern.matcher(output)
        
        val settings = ClaudeSettings.getInstance()
        
        while (matcher.find()) {
            val fileChangeJson = matcher.group(1)
            try {
                // Parse the file change
                val filePath = extractFilePath(fileChangeJson)
                val newContent = extractNewContent(fileChangeJson)
                
                if (filePath != null && newContent != null) {
                    if (settings.showDiffForChanges) {
                        showDiffAndApplyChange(filePath, newContent)
                    } else {
                        // Apply directly without showing diff
                        applyFileChange(filePath, newContent)
                    }
                }
            } catch (e: Exception) {
                // Handle parsing errors
                responseArea.append("\nError processing file change: ${e.message}")
            }
        }
    }
    
    private fun extractFilePath(json: String): String? {
        // Simple extraction for demonstration - in a real implementation, use proper JSON parsing
        val pathPattern = Pattern.compile("\"file_path\":\s*\"(.*?)\"")
        val matcher = pathPattern.matcher(json)
        return if (matcher.find()) {
            StringEscapeUtils.unescapeJava(matcher.group(1))
        } else null
    }
    
    private fun extractNewContent(json: String): String? {
        // Simple extraction for demonstration - in a real implementation, use proper JSON parsing
        val contentPattern = Pattern.compile("\"new_content\":\s*\"(.*?)\"")
        val matcher = contentPattern.matcher(json)
        return if (matcher.find()) {
            StringEscapeUtils.unescapeJava(matcher.group(1))
        } else null
    }
    
    private fun showDiffAndApplyChange(filePath: String, newContent: String) {
        ApplicationManager.getApplication().invokeLater {
            val virtualFile = LocalFileSystem.getInstance().findFileByPath(filePath)
            if (virtualFile != null) {
                // Create diff contents
                val contentFactory = DiffContentFactory.getInstance()
                val originalContent: DocumentContent
                
                // Get original content
                val originalDocument = EditorFactory.getInstance().createDocument(virtualFile.contentsToByteArray().toString(StandardCharsets.UTF_8))
                originalContent = contentFactory.create(originalDocument)
                
                // Create new content document
                val newDocument = EditorFactory.getInstance().createDocument(newContent)
                val newContentDoc = contentFactory.create(newDocument)
                
                // Show diff
                val diffRequest = SimpleDiffRequest(
                    "Changes for ${virtualFile.name}",
                    originalContent,
                    newContentDoc,
                    "Original",
                    "Modified by Claude Code"
                )
                
                DiffManager.getInstance().showDiff(project, diffRequest) { result ->
                    if (result) {
                        applyFileChange(filePath, newContent)
                    }
                }
            }
        }
    }
    
    private fun applyFileChange(filePath: String, newContent: String) {
        ApplicationManager.getApplication().runWriteAction {
            try {
                val virtualFile = LocalFileSystem.getInstance().findFileByPath(filePath)
                    ?: LocalFileSystem.getInstance().refreshAndFindFileByPath(filePath)
                    ?: throw Exception("Cannot find file: $filePath")
                
                virtualFile.setBinaryContent(newContent.toByteArray(StandardCharsets.UTF_8))
                responseArea.append("\nApplied changes to $filePath")
            } catch (e: Exception) {
                responseArea.append("\nError applying changes to $filePath: ${e.message}")
            }
        }
    }

    override fun dispose() {
        // Clean up resources if needed
    }
}
