#!/usr/bin/env pwsh
<#
Saves the current Windows clipboard contents into `chat_logs/` with a timestamped filename.
Usage: from repository root run:
  .\scripts\save_chat_from_clipboard.ps1
#>

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Definition
$repoRoot = Resolve-Path (Join-Path $scriptDir "..")
$logDir = Join-Path $repoRoot 'chat_logs'
if (-not (Test-Path $logDir)) { New-Item -ItemType Directory -Path $logDir | Out-Null }

$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$file = Join-Path $logDir ("chat_" + $timestamp + ".md")

try {
    $content = Get-Clipboard -Raw
} catch {
    Write-Error "Unable to read clipboard. Copy the chat text first, then run this script."
    exit 1
}

"# Chat saved on $timestamp`n" | Out-File -FilePath $file -Encoding utf8
$content | Out-File -FilePath $file -Append -Encoding utf8

Write-Output "Saved clipboard chat to: $file"