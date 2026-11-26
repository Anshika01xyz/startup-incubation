$ErrorActionPreference = 'Stop'
[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
$uri = 'https://fliarbi.com/datasets/files/india-startup-funding/india_startup_funding_sample.csv'
$destination = Join-Path $PSScriptRoot 'india_startup_funding_sample.csv'
Invoke-WebRequest -Uri $uri -OutFile $destination
$file = Get-Item $destination
$header = Get-Content -Path $destination -TotalCount 1
if ($file.Length -lt 1024 -or $header -notmatch 'round_id' -or $header -notmatch 'company_name') {
    Remove-Item $destination -ErrorAction SilentlyContinue
    throw 'The downloaded file does not look like the publisher CSV. Please retry from the publisher dataset page.'
}
Write-Host "Downloaded actual publisher CSV to: $destination"
Write-Host "File size: $($file.Length) bytes"
