param(
    [string[]]$Versions = @("1.21.5","1.21.6","1.21.7","1.21.8","1.21.9","1.21.10","1.21.11"),
    [string]$BaseDir = "C:\Users\user\IdeaProjects\CustomCross"
)

pushd $BaseDir

# Store original gradle.properties
$origProps = Get-Content "gradle.properties" -Raw

# Map: mc_version -> yarn_mappings, fabric_version
$targets = @{
    "1.21.5" = @{ yarn = "1.21.5+build.1"; fabric = "0.120.0+1.21.5" }
    "1.21.6" = @{ yarn = "1.21.6+build.1"; fabric = "0.120.0+1.21.6" }
    "1.21.7" = @{ yarn = "1.21.7+build.8"; fabric = "0.128.0+1.21.7" }
    "1.21.8" = @{ yarn = "1.21.8+build.1"; fabric = "0.129.0+1.21.8" }
    "1.21.9" = @{ yarn = "1.21.9+build.1"; fabric = "0.130.0+1.21.9" }
    "1.21.10" = @{ yarn = "1.21.10+build.3"; fabric = "0.134.1+1.21.10" }
    "1.21.11" = @{ yarn = "1.21.11+build.6"; fabric = "0.135.1+1.21.11" }
}

$results = @{}

foreach ($ver in $Versions) {
    Write-Host "=== Building for $ver ==="
    $t = $targets[$ver]
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
    Set-Content -Path "gradle.properties" -Value $props -NoNewline
    .\gradlew.bat build --no-daemon 2>&1 | Out-Null
    $ok = $LASTEXITCODE -eq 0
    $results[$ver] = $ok
    if ($ok) {
        # Rename JAR to version-specific
        if (Test-Path "build/libs/customcross-1.0.0.jar") {
            Copy-Item "build/libs/customcross-1.0.0.jar" "build/libs/customcross-$ver-1.0.0.jar" -Force
        }
        if (Test-Path "build/libs/customcross-1.0.0-sources.jar") {
            Copy-Item "build/libs/customcross-1.0.0-sources.jar" "build/libs/customcross-$ver-1.0.0-sources.jar" -Force
        }
        Write-Host "  -> SUCCESS"
    } else {
        Write-Host "  -> FAILED"
    }
}

# Restore original
Set-Content -Path "gradle.properties" -Value $origProps -NoNewline

Write-Host "=== Results ==="
$results.Keys | Sort-Object | ForEach-Object { "$_ : $(if ($results[$_]) {'OK'} else {'FAIL'})" }
