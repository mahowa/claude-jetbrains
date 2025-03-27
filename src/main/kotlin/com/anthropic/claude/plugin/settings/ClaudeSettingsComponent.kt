package com.anthropic.claude.plugin.settings

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import javax.swing.JComponent
import javax.swing.JPanel

class ClaudeSettingsComponent {
    private val apiKeyField = JBPasswordField()
    private val claudeCodePathField = TextFieldWithBrowseButton()
    private val showDiffCheckbox = JBCheckBox("Show diff for code changes")
    val panel: JPanel

    init {
        claudeCodePathField.addBrowseFolderListener(
            "Claude Code Path",
            "Select the path to the claude-code executable",
            null,
            FileChooserDescriptorFactory.createSingleFileDescriptor()
        )

        panel = FormBuilder.createFormBuilder()
            .addLabeledComponent(JBLabel("API Key:"), apiKeyField, true)
            .addLabeledComponent(JBLabel("Claude Code Path:"), claudeCodePathField, true)
            .addComponent(showDiffCheckbox, 1)
            .addComponentFillVertically(JPanel(), 0)
            .panel
    }

    var apiKey: String
        get() = String(apiKeyField.password)
        set(value) {
            apiKeyField.text = value
        }

    var claudeCodePath: String
        get() = claudeCodePathField.text
        set(value) {
            claudeCodePathField.text = value
        }
        
    var showDiffForChanges: Boolean
        get() = showDiffCheckbox.isSelected
        set(value) {
            showDiffCheckbox.isSelected = value
        }
}
