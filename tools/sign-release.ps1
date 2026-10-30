param(
    [Parameter(Mandatory = $true)]
    [ValidatePattern('^\d+\.\d+\.\d+$')]
    [string]$Version,
    [string]$AndroidSdk = (Join-Path $env:LOCALAPPDATA 'Android/Sdk'),
    [string]$BuildToolsVersion = '36.0.0',
    [string]$SigningDirectory = (Join-Path (Split-Path (Split-Path $PSScriptRoot -Parent) -Parent) 'release-signing')
)

$ErrorActionPreference = 'Stop'
$repoRoot = Split-Path $PSScriptRoot -Parent
$unsignedApk = Join-Path $repoRoot 'app/build/outputs/apk/release/app-release-unsigned.apk'
$keystore = Join-Path $SigningDirectory 'haulauf-release.p12'
$passwordFile = Join-Path $SigningDirectory 'password.dpapi.txt'
$buildTools = Join-Path $AndroidSdk "build-tools/$BuildToolsVersion"
$outputDir = Join-Path (Split-Path $repoRoot -Parent) 'release-artifacts'
$alignedApk = Join-Path $outputDir "haulauf-trainer-$Version-aligned.apk"
$signedApk = Join-Path $outputDir "haulauf-trainer-$Version.apk"

foreach ($required in @($unsignedApk, $keystore, $passwordFile, (Join-Path $buildTools 'zipalign.exe'), (Join-Path $buildTools 'apksigner.bat'), (Join-Path $buildTools 'aapt2.exe'))) {
    if (-not (Test-Path $required)) { throw "Missing input: $required" }
}
if (Test-Path $signedApk) { throw 'Signed APK already exists; refusing to overwrite it.' }
$badging = & (Join-Path $buildTools 'aapt2.exe') dump badging $unsignedApk
if ($LASTEXITCODE -ne 0) { throw 'Cannot read APK metadata.' }
$packageLine = $badging | Where-Object { $_ -match '^package:' } | Select-Object -First 1
if ($packageLine -notmatch "name='com\.example\.myapplication'" -or
    $packageLine -notmatch "versionName='$([regex]::Escape($Version))'" -or
    $packageLine -notmatch "versionCode='(\d+)'") {
    throw 'APK package/version does not match the requested release. Build the regular release variant first.'
}
if ([long]$Matches[1] -le 2) { throw 'New releases must use a versionCode greater than the published 1.1.0 (2).' }
[IO.Directory]::CreateDirectory($outputDir) | Out-Null
& (Join-Path $buildTools 'zipalign.exe') -p 4 $unsignedApk $alignedApk
if ($LASTEXITCODE -ne 0) { throw 'zipalign failed.' }

$securePassword = Get-Content -LiteralPath $passwordFile -Raw | ConvertTo-SecureString
$passwordPointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($securePassword)
try { $env:HAULAUF_KEYSTORE_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($passwordPointer) }
finally { [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($passwordPointer) }
try {
    & (Join-Path $buildTools 'apksigner.bat') sign --ks $keystore --ks-type PKCS12 --ks-key-alias haulauf-release --ks-pass env:HAULAUF_KEYSTORE_PASSWORD --key-pass env:HAULAUF_KEYSTORE_PASSWORD --out $signedApk $alignedApk
    if ($LASTEXITCODE -ne 0) { throw 'APK signing failed.' }
    & (Join-Path $buildTools 'apksigner.bat') verify --verbose --print-certs $signedApk
    if ($LASTEXITCODE -ne 0) { throw 'APK signature verification failed.' }
    $certificate = & (Join-Path $buildTools 'apksigner.bat') verify --print-certs $signedApk
    if ($LASTEXITCODE -ne 0 -or ($certificate -join "`n") -notmatch '(?i)certificate SHA-256 digest: 28632343111138fc6f4e964e880a882c803bc014082190d541c6926d28ca9780') {
        throw 'Signing certificate differs from release 1.1.0. Do not distribute this APK.'
    }
} finally {
    Remove-Item Env:HAULAUF_KEYSTORE_PASSWORD -ErrorAction SilentlyContinue
    Remove-Item -LiteralPath $alignedApk -ErrorAction SilentlyContinue
}
Write-Output "Signed APK: $signedApk"
Get-FileHash -LiteralPath $signedApk -Algorithm SHA256 | Select-Object Path, Hash
