# Contributing to Morrow

Thanks for taking an interest in Morrow.

## Before you start

- Read the README to understand the current architecture and feature status.
- For substantial changes, open an issue or discussion first so the scope is clear.
- Keep pull requests focused and explain why the change is needed.

## Development guidelines

- Preserve the existing Kotlin and Jetpack Compose architecture unless a change is agreed upon.
- Avoid unrelated UI, backend, and build-system rewrites in the same pull request.
- Do not change the deployed `/chat` contract without updating and testing its callers.
- Never commit API keys, OAuth client secrets, access tokens, signing keys, or personal data.
- Do not describe a feature as complete until it has been tested.

## Before opening a pull request

- Run the Android build or explain why you could not run it.
- Include relevant screenshots for visible UI changes.
- Describe changed files, test results, and any manual setup still required.
- Call out breaking changes and backend configuration changes clearly.

## Reporting bugs

Open a GitHub issue with the device/Android version, steps to reproduce, expected behavior, and actual behavior. Remove secrets and private data from logs and screenshots.
