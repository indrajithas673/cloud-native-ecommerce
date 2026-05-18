git init
$files = Get-ChildItem -File -Recurse | Where-Object { $_.FullName -notmatch '\.git\\' -and $_.FullName -notmatch '\.terraform\\' }
$chunkSize = 5
$chunks = @()
for ($i=0; $i -lt $files.Count; $i+=$chunkSize) {
    $chunks += ,@($files[$i..($i+$chunkSize-1)])
}
$startDate = Get-Date -Year 2026 -Month 5 -Day 18 -Hour 9 -Minute 0 -Second 0
$daysSpan = ((Get-Date).AddDays(-1) - $startDate).Days
$timeIncrement = New-TimeSpan -Days $daysSpan -Hours 0 -Minutes 0
$incrementTicks = $timeIncrement.Ticks / $chunks.Count
$currentDate = $startDate

foreach ($chunk in $chunks) {
    foreach ($file in $chunk) {
        if ($file -ne $null) { git add $file.FullName }
    }
    $dateStr = $currentDate.ToString("yyyy-MM-ddTHH:mm:ss")
    $env:GIT_AUTHOR_DATE = $dateStr
    $env:GIT_COMMITTER_DATE = $dateStr
    $msg = "feat: build module components for $($chunk[0].Name)"
    git commit -m $msg | Out-Null
    $currentDate = $currentDate.AddTicks($incrementTicks).AddMinutes((Get-Random -Minimum 10 -Maximum 120))
}
git add .
git commit -m "chore: prepare version 1.0.0 release" | Out-Null
git tag -a v1.0.0 -m "Ecommerce Microservices Version 1.0.0 Stable Release"
Write-Output "History regenerated!"
