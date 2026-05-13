function g_sacp {
    param(
        [Parameter(Mandatory=$true)]
        [string]$Message
    )

    git status

    $confirm = Read-Host "Alle geänderten Dateien committen und pushen? (j/n)"
    if ($confirm -ne "j") {
        Write-Host "Abgebrochen."
        return
    }

    git add -A
    git commit -m "$Message"
    git push
}