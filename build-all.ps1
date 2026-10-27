param(
    [string[]]$Versions = @("1.21.4","1.21.5","1.21.6","1.21.7","1.21.8"),
    [string]$BaseDir = "C:\Users\user\IdeaProjects\CustomCross",
    [string]$OutputDir = "C:\Users\user\Desktop\Frog Studio\000 mod Custom Cross"
)

Set-Location $BaseDir

# Store original gradle.properties
$origProps = Get-Content "$BaseDir\gradle.properties" -Raw

# Map: mc_version -> yarn_mappings, fabric_version
$targets = @{
    "1.21.4"  = @{ yarn = "1.21.4+build.8"; fabric = "0.119.4+1.21.4" }
    "1.21.5"  = @{ yarn = "1.21.5+build.1"; fabric = "0.120.0+1.21.5" }
    "1.21.6"  = @{ yarn = "1.21.6+build.1"; fabric = "0.120.0+1.21.6" }
    "1.21.7"  = @{ yarn = "1.21.7+build.8"; fabric = "0.128.0+1.21.7" }
    "1.21.8"  = @{ yarn = "1.21.8+build.1"; fabric = "0.129.0+1.21.8" }
}

$results = @{}

foreach ($ver in $Versions) {
    Write-Host "=== Building for $ver ==="
    $t = $targets[$ver]
    if (-not $t) {
        Write-Host "  -> No target config for $ver"
        continue
    }
    $props = @"
org.gradle.jvmargs=-Xmx1G
minecraft_version=$ver
yarn_mappings=$($t.yarn)
loader_version=0.19.3
mod_version=1.0.0
maven_group=org.example.bindManager
archives_base_name=customcross
fabric_version=$($t.fabric)
"@
    Set-Content -Path "$BaseDir\gradle.properties" -Value $props -NoNewline
    & "$BaseDir\gradlew.bat" clean build --no-daemon 2>&1 | Out-Null
    $ok = $LASTEXITCODE -eq 0
    $results[$ver] = $ok
    if ($ok) {
        $jarPath = "$BaseDir\build\libs\customcross-1.0.0.jar"
        $sourcesPath = "$BaseDir\build\libs\customcross-1.0.0-sources.jar"
        $destJar = "$BaseDir\build\libs\customcross-$ver-1.0.0.jar"
        $destSources = "$BaseDir\build\libs\customcross-$ver-1.0.0-sources.jar"

        if (Test-Path $jarPath) {
            Copy-Item $jarPath $destJar -Force
            if (Test-Path $OutputDir) {
                Copy-Item $jarPath "$OutputDir\customcross-$ver-1.0.0.jar" -Force
            }

            # Also update ModrinthApp profiles if found
            $modrinthProfiles = "$env:APPDATA\ModrinthApp\profiles"
            if (Test-Path $modrinthProfiles) {
                Get-ChildItem -Directory $modrinthProfiles | ForEach-Object {
                    $modsDir = Join-Path $_.FullName "mods"
                    if (Test-Path $modsDir) {
                        Get-ChildItem -Path $modsDir -Filter "customcross-*" | ForEach-Object {
                            if ($_.Name -like "*$ver*") {
                                Copy-Item $jarPath $_.FullName -Force
                                Write-Host "  -> Updated Modrinth mod in: $($_.FullName)"
                            }
                        }
                    }
                }
            }
        }
        if (Test-Path $sourcesPath) {
            Copy-Item $sourcesPath $destSources -Force
            if (Test-Path $OutputDir) {
                Copy-Item $sourcesPath "$OutputDir\customcross-$ver-1.0.0-sources.jar" -Force
            }
        }
        Write-Host "  -> SUCCESS ($ver)"
    } else {
        Write-Host "  -> FAILED ($ver)"
    }
}

# Restore original
Set-Content -Path "$BaseDir\gradle.properties" -Value $origProps -NoNewline

Write-Host "`n=== Results ==="
$results.Keys | Sort-Object | ForEach-Object { "$_ : $(if ($results[$_]) {'OK'} else {'FAIL'})" }
