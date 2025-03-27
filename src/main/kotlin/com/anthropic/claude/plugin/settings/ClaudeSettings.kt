package com.anthropic.claude.plugin.settings

import com.intellij.openapi.components.*
import com.intellij.util.xmlb.XmlSerializerUtil

@State(
    name = "com.anthropic.claude.plugin.settings.ClaudeSettings",
    storages = [Storage("ClaudeCodeSettings.xml")]
)
class ClaudeSettings : PersistentStateComponent<ClaudeSettings> {
    var apiKey: String = ""
    var claudeCodePath: String = ""
    var showDiffForChanges: Boolean = true
    
    override fun getState(): ClaudeSettings = this

    override fun loadState(state: ClaudeSettings) {
        XmlSerializerUtil.copyBean(state, this)
    }

    companion object {
        @JvmStatic
        fun getInstance(): ClaudeSettings {
            return service<ClaudeSettings>()
        }
    }
}
