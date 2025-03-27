package com.anthropic.claude.plugin.settings

import com.intellij.openapi.options.Configurable
import javax.swing.JComponent

class ClaudeSettingsConfigurable : Configurable {
    private var settingsComponent: ClaudeSettingsComponent? = null

    override fun createComponent(): JComponent {
        settingsComponent = ClaudeSettingsComponent()
        return settingsComponent!!.panel
    }

    override fun isModified(): Boolean {
        val settings = ClaudeSettings.getInstance()
        return settingsComponent?.apiKey != settings.apiKey ||
               settingsComponent?.claudeCodePath != settings.claudeCodePath ||
               settingsComponent?.showDiffForChanges != settings.showDiffForChanges
    }

    override fun apply() {
        val settings = ClaudeSettings.getInstance()
        settings.apiKey = settingsComponent?.apiKey ?: ""
        settings.claudeCodePath = settingsComponent?.claudeCodePath ?: ""
        settings.showDiffForChanges = settingsComponent?.showDiffForChanges ?: true
    }

    override fun reset() {
        val settings = ClaudeSettings.getInstance()
        settingsComponent?.apiKey = settings.apiKey
        settingsComponent?.claudeCodePath = settings.claudeCodePath
        settingsComponent?.showDiffForChanges = settings.showDiffForChanges
    }

    override fun disposeUIResources() {
        settingsComponent = null
    }

    override fun getDisplayName(): String = "Claude Code"
}
