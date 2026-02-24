---
name: Commit Message Assistant
description: Use this skill when the user asks for a commit message or to summarize staged changes.
---

# Commit Message Assistant

This skill generates concise, conventional commit messages based on staged changes.

## Format

Return a single-line conventional commit message:

```
<type>: <summary>
```

Where `<type>` is one of: feat, fix, docs, refactor, test, chore.
