# Contributing to RepoStore

Thank you for your interest in contributing to RepoStore! 🎉 This guide will help you set up your development environment and understand how to contribute to the project.

## Table of Contents
1. [How to Contribute](#how-to-contribute)
2. [Contribution Workflow](#contribution-workflow)
3. [Localizing RepoStore](#localizing-repostore)
4. [Contributing to GitCore](#contributing-to-gitcore)
5. [Fastlane Metadata](#fastlane-metadata)
6. [Code Style & Best Practices](#code-style--best-practices)
7. [Commit Messages](#commit-messages)

---

## How to Contribute

### Reporting Bugs
1. Check the [Existing Issues](https://github.com/samyak2403/RepoStore/issues) for similar reports.
2. If the issue is new, create a bug report with:
    - Precise title and description.
    - Steps to reproduce the error.
    - Expected vs. Actual behavior.
    - System details (Android version, Device model).
    - Screenshots or screen recordings.

### Suggesting Features
1. Open a new issue with the `enhancement` label.
2. Clearly explain the need and usability of the proposed feature.

## Contribution Workflow

1. **Fork** the repository and **Clone** your fork.
   ```bash
   git clone https://github.com/samyak2403/RepoStore.git
   ```
2. **Create a branch** for your work.
   ```bash
   git checkout -b feature/your-awesome-feature
   # or
   git checkout -b fix/issue-name
   ```
3. **Make your changes** following our guidelines.
4. **Test** the app thoroughly on different screen sizes and OS versions.
5. **Commit** using the conventional format (see below).
6. **Push** to your fork and **Open a Pull Request** to the `main` branch.

## Localizing RepoStore

The project now supports extensive multi-language features via a **Searchable Language Dialog**.

### Adding a New Language
1. Create a new `values-xx` folder in `app/src/main/res/`.
2. Add your translated `strings.xml` file.
3. Ensure the language is picked up by `LanguageDialogHelper.kt`.
4. To add specific language support (like **Hinglish**), modify `LanguageDialogHelper.kt` to define the native and English display names correctly.

## Contributing to GitCore

The core Git operations are located in the `GitCore` library module. To contribute here:
- Keep the module platform-independent where possible.
- Ensure efficient data handling for Git-related operations (cloning, pull/push).

## Fastlane Metadata

We use **Fastlane** for managing app store listings and screenshots.
1. Metadata files are in `fastlane/metadata/android/`.
2. To update descriptions or changelogs, edit the text files under the relevant locale (e.g., `en-US`, `hi-IN`).
3. For screenshots, place them in the corresponding locale folder under `fastlane/metadata/android/images`.

## Code Style & Best Practices

- **Kotlin First**: Use Kotlin for all new logic.
- **Consistency**: Follow the existing indentation and naming conventions.
- **Modularity**: Keep the `app` and `GitCore` modules separated cleanly.
- **UI Gems**: When adding new UI components, use **Material 3** elements and maintain theme compatibility (Dark/Light modes).

## Commit Messages

We follow the [Conventional Commits](https://www.conventionalcommits.org/) specification:
- `feat:` for new features
- `fix:` for bug fixes
- `docs:` for documentation updates
- `style:` for UI/formatting changes
- `refactor:` for code restructuring
- `chore:` for maintenance (Gradle, dependencies)

---

Thank you for making RepoStore better! ❤️
