---
name: project-update
description: Switched from Flutter to Native Android (Kotlin + Compose)
metadata:
  type: project
---

Project shifted to Native Android development in Kotlin using Jetpack Compose because of environment issues with Flutter CLI.
Remaining Goals:
1. PDF to EPUB conversion logic (use PDFBox-Android or similar).
2. EPUB reader implementation (use EPubLib or similar).
3. Preservation of structure and images.
4. Modern Material 3 UI.

**Why:** Native development is more stable in this environment.
**How to apply:** Implement core conversion and UI modules in `app/src/main/java/com/pdf/epub/`.
