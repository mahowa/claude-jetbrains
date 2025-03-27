# Claude Code Plugin for JetBrains IDEs

> **DISCLAIMER:** This project is currently under development and may not be fully stable.

A JetBrains IDE plugin that integrates Claude Code, allowing you to run Claude Code AI assistant directly within your IDE.

## Features

- Run Claude Code directly within your JetBrains IDE
- Ask questions about selected code
- View and apply code changes using the IDE's built-in diff tool
- Configurable API key and Claude Code path

## Installation

1. Download the plugin from the JetBrains Marketplace (link coming soon)
2. Install it in your JetBrains IDE via Preferences/Settings → Plugins → Install from disk
3. Configure your Anthropic API key and Claude Code path in Preferences/Settings → Tools → Claude Code

## Requirements

- JetBrains IDE (IntelliJ IDEA, PyCharm, WebStorm, etc.) 2022.3 or newer
- Claude Code executable installed on your system
- Valid Anthropic API key

## Usage

### Tool Window

1. Open the Claude Code tool window (View → Tool Windows → Claude Code)
2. Enter your prompt and press Enter or click "Ask Claude"
3. View the response in the tool window

### Context Menu

1. Select code in the editor
2. Right-click and select "Ask Claude Code" from the context menu
3. Enter your prompt in the dialog
4. If Claude suggests code changes, they will be displayed in a diff viewer for you to review and apply

## Development

### Building the Plugin

```bash
./gradlew buildPlugin
```

This will create a plugin zip file in `build/distributions`.

### Running the Plugin in a Development IDE

```bash
./gradlew runIde
```

## Contributors

Contributions to this project are welcome! However, all code will be reviewed and accepted only by the repository owner. When your contribution is accepted, your name and image will be displayed in this section.

### How to Contribute

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## About Claude Code

This plugin was developed with assistance from [Claude Code](https://docs.anthropic.com/en/docs/agents-and-tools/claude-code/overview), Anthropic's command-line interface for Claude. Claude Code provides AI assistance for software development tasks directly in your terminal.

## License

This project is licensed under the MIT License - see the LICENSE file for details.
