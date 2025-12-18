This folder stores saved chat transcripts produced by `scripts/save_chat_from_clipboard.ps1`.

How to save a chat:
1. Select and copy the chat text in VS Code (or your browser) to the clipboard.
2. From the repository root run:

```powershell
.\scripts\save_chat_from_clipboard.ps1
```

3. A timestamped `chat_YYYYMMDD_HHMMSS.md` will be created here.

Keep `chat_logs` private (don't commit sensitive messages).
