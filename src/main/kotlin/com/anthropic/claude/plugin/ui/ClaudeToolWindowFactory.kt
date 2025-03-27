package com.anthropic.claude.plugin.ui

import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory

class ClaudeToolWindowFactory : ToolWindowFactory, DumbAware {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val claudeToolWindow = ClaudeToolWindow(project)
        val contentFactory = ContentFactory.getInstance()
        val content = contentFactory.createContent(claudeToolWindow.component, "", false)
        toolWindow.contentManager.addContent(content)
    }
}
