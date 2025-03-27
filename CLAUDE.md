# Claude Code JetBrains Plugin Development Guidelines

## Build Commands
- Build: `./gradlew buildPlugin`
- Run in dev IDE: `./gradlew runIde`
- Run tests: `./gradlew test`
- Run single test: `./gradlew test --tests "com.anthropic.claude.plugin.TestClassName.testMethodName"`
- Package plugin: `./gradlew packagePlugin`

## Code Style Guidelines
- **Imports**: Order by package name, group standard library first, then third-party, then project
- **Naming**: Use camelCase for variables/functions, PascalCase for classes, SCREAMING_SNAKE_CASE for constants
- **Types**: Always include explicit return types for public functions
- **Error Handling**: Use nullable types and safe calls (`?.`) wherever possible; wrap external API calls in try/catch
- **Comments**: Minimal comments - only for complex logic (code should be self-documenting)
- **Formatting**: 4-space indentation, 120 max line length
- **Services**: Use IntelliJ's service framework with `@Service` annotations for singletons
- **UI Components**: Favor composition over inheritance for Swing components
- **Code Organization**: Group related functionality in the same package, separate business logic from UI

When making changes, ensure consistent style with existing code and maintain backward compatibility.