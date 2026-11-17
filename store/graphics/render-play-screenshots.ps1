# Renders the Google Play phone screenshots from the raw emulator captures in
# ..\screenshots\phone-en, framed by play-screenshot.html.
# Output: ..\screenshots\play-phone-en, 1080x1920 (9:16) 24-bit PNG without alpha,
# which is what Play Console accepts and what large-format promotion needs.
# Usage (Windows PowerShell): .\render-play-screenshots.ps1 [-Chrome <path to chrome.exe>]
param([string]$Chrome)

Add-Type -AssemblyName System.Drawing

$width = 1080
$height = 1920
$renderBudgetMs = 8000
$template = Join-Path $PSScriptRoot 'play-screenshot.html'
$outDir = Join-Path (Split-Path $PSScriptRoot -Parent) 'screenshots\play-phone-en'
$userData = Join-Path $env:TEMP 'listo-render-profile'

$shots = @(
    @{ shot = '01-onboarding';  title = 'Your shopping, sorted.';     sub = 'A free shopping list that keeps itself tidy' },
    @{ shot = '02-my-lists';    title = 'All your lists at a glance'; sub = 'Progress, dates and reminders in one view' },
    @{ shot = '03-list-detail'; title = "Type it, don't organise it"; sub = 'Every item lands in the right aisle by itself' },
    @{ shot = '04-categories';  title = 'Aisles your way';            sub = 'Add your own categories and colours' },
    @{ shot = '05-calendar';    title = 'Plan the week ahead';        sub = 'Give a list a date and a reminder' },
    @{ shot = '06-settings';    title = 'Make it yours';              sub = 'Light or dark, 12 accent colours, 3 languages' }
)

if (-not $Chrome) {
    $Chrome = @(
        "$env:ProgramFiles\Google\Chrome\Application\chrome.exe",
        "${env:ProgramFiles(x86)}\Google\Chrome\Application\chrome.exe",
        "${env:ProgramFiles(x86)}\Microsoft\Edge\Application\msedge.exe"
    ) | Where-Object { Test-Path $_ } | Select-Object -First 1
}
if (-not $Chrome) { throw 'Chrome or Edge not found; pass -Chrome <path>.' }

New-Item -ItemType Directory -Force $outDir | Out-Null

foreach ($s in $shots) {
    $query = 'shot=' + [uri]::EscapeDataString($s.shot) +
        '&title=' + [uri]::EscapeDataString($s.title) +
        '&sub=' + [uri]::EscapeDataString($s.sub)
    $url = ([uri]$template).AbsoluteUri + '?' + $query
    $raw = Join-Path $env:TEMP "$($s.shot)-raw.png"
    if (Test-Path $raw) { Remove-Item $raw }

    # Chrome logs to stderr; that noise is not an error.
    & $Chrome --headless=new --disable-gpu --hide-scrollbars --force-device-scale-factor=1 `
        "--window-size=$width,$height" "--virtual-time-budget=$renderBudgetMs" `
        "--user-data-dir=$userData" "--screenshot=$raw" $url 2>&1 | Out-Null
    if (-not (Test-Path $raw)) { throw "$($s.shot): Chrome did not write a screenshot." }

    # Re-encode as 24-bit RGB: Play rejects PNGs with an alpha channel.
    $src = [System.Drawing.Image]::FromFile($raw)
    try {
        if ($src.Width -ne $width -or $src.Height -ne $height) {
            throw "$($s.shot): rendered $($src.Width)x$($src.Height), expected ${width}x$height."
        }
        $bmp = New-Object System.Drawing.Bitmap $width, $height, ([System.Drawing.Imaging.PixelFormat]::Format24bppRgb)
        $g = [System.Drawing.Graphics]::FromImage($bmp)
        $g.DrawImage($src, 0, 0, $width, $height)
        $g.Dispose()
        $out = Join-Path $outDir "$($s.shot).png"
        $bmp.Save($out, [System.Drawing.Imaging.ImageFormat]::Png)
        $bmp.Dispose()
        "wrote $out"
    } finally {
        $src.Dispose()
    }
    Remove-Item $raw
}
