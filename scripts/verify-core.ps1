
$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
$build = Join-Path $root 'target\core-verification'

New-Item -ItemType Directory -Force -Path $build | Out-Null

$sources = Get-ChildItem (Join-Path $root 'src\main\java\com\startup\platform') -Filter '*.java' -Recurse |
    Where-Object { $_.Name -ne 'Main.java' } |
    ForEach-Object { '"' + $_.FullName.Replace('\', '/') + '"' }

$sources += '"' + (Join-Path $root 'src\test\java\com\startup\platform\CoreVerification.java').Replace('\', '/') + '"'

$listFile = Join-Path $build 'sources.txt'
Set-Content -Path $listFile -Value $sources -Encoding ascii

& javac -Xlint:all -d $build "@$listFile"

if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}

& java -cp "$build;$(Join-Path $root 'src\main\resources')" com.startup.platform.CoreVerification

exit $LASTEXITCODE
