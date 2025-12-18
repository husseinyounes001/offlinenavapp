Chat persistence

You can save chat transcripts into the repository using the provided PowerShell script.

Steps:

1. Copy the chat contents to your clipboard.
2. Run (from repo root):

```powershell
.\scripts\save_chat_from_clipboard.ps1
```

3. The saved file will be in `chat_logs/` as `chat_YYYYMMDD_HHMMSS.md`.

If you want automatic saving or integration with VS Code's chat panel, I can add a small VS Code extension or a more advanced script — tell me which you prefer.
