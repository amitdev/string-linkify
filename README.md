# 🔗 String Linkify IntelliJ Plugin
An IntelliJ plugin that turns string literals in your code into clickable hyperlinks, using the same patterns you configure in IntelliJ's built-in **Issue Navigation**.

## The Problem

IntelliJ's Issue Navigation feature (Settings > Version Control > Issue Navigation) lets you define regex patterns that turn issue IDs into clickable links -- but it only works in **VCS commit messages and comments**. If your code references issues, feature flags, or other identifiers inside string literals, those remain plain, unlinked text.

## What This Plugin Does
This plugin extends Issue Navigation patterns to **any string literals in code**. The pattern you've already configured for commit messages will now also create clickable links inside strings, XML attributes, and comments across multiple languages.

It also supports **contextual matching** -- it considers the surrounding code (like a method or constructor name) when matching patterns, enabling more precise rules.

### Example

Given this Issue Navigation configuration:

| Pattern                          | Link URL                             |
|----------------------------------|--------------------------------------|
| `Feature\("([A-Za-z\-_.\d]+)"\)` | `https://featureflags.com/browse/$1` |

The string `"feature-1"` in the following code becomes a clickable link to `https://featureflags.com/browse/feature-1`:

```kotlin
val f = Feature("feature-1")
```

### Contextual Matching

When the plugin encounters a string literal, it builds two match candidates and tries them in order:

1. **Contextual** -- includes the enclosing call/constructor name, e.g. `Feature("PROJ-123")`
2. **Raw** -- just the string content, e.g. `PROJ-123`

This lets you write patterns that are scoped to specific call sites (matching only `Feature("...")` but not `Logger("...")`), or broad patterns that match any string containing an issue ID.

## Setup
1. Install the plugin.
2. Go to **File > Settings > Version Control > Issue Navigation** (or **Preferences** on macOS).
3. Add or edit patterns. The plugin reuses these same patterns -- no separate configuration needed.

### Pattern Examples

| Use Case | Issue Pattern | Link URL |
|---|---|---|
| Any JIRA-style ID in a string | `([A-Z]+-\d+)` | `https://jira.example.com/browse/$1` |
| Only inside `Feature(...)` calls | `Feature\("([A-Za-z\-_.\d]+)"\)` | `https://features.example.com/$1` |
| GitHub issue references | `#(\d+)` | `https://github.com/org/repo/issues/$1` |

## Building from Source

**Requirements:** JDK 21, Gradle 8.12+

```bash
./gradlew buildPlugin
```

To run a sandboxed IDE instance with the plugin loaded:

```bash
./gradlew runIde
```

