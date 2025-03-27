package com.anthropic.claude.plugin.api

import com.anthropic.claude.plugin.settings.ClaudeSettings
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessHandlerFactory
import com.intellij.execution.process.ProcessListener
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import java.io.File

@Service(Service.Level.PROJECT)
class ClaudeCodeService(private val project: Project) {
    private val logger = Logger.getInstance(ClaudeCodeService::class.java)
    private val gson = Gson()

    fun executeClaudeCode(prompt: String, progressIndicator: ProgressIndicator?, listener: ProcessListener): ProcessHandler {
        val settings = ClaudeSettings.getInstance()
        
        val commandLine = GeneralCommandLine()
        commandLine.exePath = settings.claudeCodePath
        commandLine.setWorkDirectory(project.basePath)
        
        // Add environment variables
        if (settings.apiKey.isNotEmpty()) {
            commandLine.withEnvironment("ANTHROPIC_API_KEY", settings.apiKey)
        }
        
        // Add arguments
        commandLine.addParameter("--json") // JSON output for easier parsing
        commandLine.addParameter(prompt)
        
        // Create process handler
        val processHandler = ProcessHandlerFactory.getInstance().createProcessHandler(commandLine)
        processHandler.addProcessListener(listener)
        
        return processHandler
    }
    
    fun executeClaudeCodeWithChanges(prompt: String, filePath: String, progressIndicator: ProgressIndicator?, listener: ProcessListener): ProcessHandler {
        val commandLine = GeneralCommandLine()
        val settings = ClaudeSettings.getInstance()
        
        commandLine.exePath = settings.claudeCodePath
        commandLine.setWorkDirectory(project.basePath)
        
        // Add environment variables
        if (settings.apiKey.isNotEmpty()) {
            commandLine.withEnvironment("ANTHROPIC_API_KEY", settings.apiKey)
        }
        
        // Add arguments for file changes
        commandLine.addParameter("--json") // JSON output for easier parsing
        commandLine.addParameter("--file")
        commandLine.addParameter(filePath)
        commandLine.addParameter(prompt)
        
        // Create process handler
        val processHandler = ProcessHandlerFactory.getInstance().createProcessHandler(commandLine)
        processHandler.addProcessListener(listener)
        
        return processHandler
    }
    
    companion object {
        @JvmStatic
        fun getInstance(project: Project): ClaudeCodeService {
            return project.getService(ClaudeCodeService::class.java)
        }
    }
}
