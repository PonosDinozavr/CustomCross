param([string]$McVer)

$BaseDir = "C:\Users\user\IdeaProjects\CustomCross"
$BuildDir = "C:\Users\user\IdeaProjects\CustomCross\build\versions\$McVer"

Remove-Item -Recurse -Force $BuildDir -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Force -Path "$BuildDir\src" | Out-Null
Copy-Item "$BaseDir\src\*" "$BuildDir\src\" -Recurse -Force
Copy-Item "$BaseDir\build.gradle" "$BuildDir\"
Copy-Item "$BaseDir\settings.gradle" "$BuildDir\"
Copy-Item "$BaseDir\gradle.properties" "$BuildDir\"
Copy-Item "$BaseDir\gradle" "$BuildDir\gradle\" -Recurse -Force
Copy-Item "$BaseDir\gradlew.bat" "$BuildDir\"
Copy-Item "$BaseDir\gradlew" "$BuildDir\"
Copy-Item "$BaseDir\LICENSE.txt" "$BuildDir\"
Copy-Item "$BaseDir\.gitignore" "$BuildDir\"

$versions = @{
    "1.21.5" = @{ yarn = "1.21.5+build.1"; fabric = "0.120.0+1.21.5" }
    "1.21.6" = @{ yarn = "1.21.6+build.1"; fabric = "0.120.0+1.21.6" }
    "1.21.7" = @{ yarn = "1.21.7+build.8"; fabric = "0.128.0+1.21.7" }
    "1.21.8" = @{ yarn = "1.21.8+build.1"; fabric = "0.129.0+1.21.8" }
    "1.21.9" = @{ yarn = "1.21.9+build.1"; fabric = "0.130.0+1.21.9" }
    "1.21.10" = @{ yarn = "1.21.10+build.3"; fabric = "0.134.1+1.21.10" }
    "1.21.11" = @{ yarn = "1.21.11+build.6"; fabric = "0.135.1+1.21.11" }
}

$t = $versions[$McVer]
$verProps = @"
org.gradle.jvmargs=-Xmx2G
minecraft_version=$McVer
yarn_mappings=$($t.yarn)
loader_version=0.19.3
mod_version=1.0.0
maven_group=org.example.bindManager
archives_base_name=customcross
fabric_version=$($t.fabric)
"@
Set-Content -Path "$BuildDir\gradle.properties" -Value $verProps -NoNewline

pushd $BuildDir
.\gradlew.bat build --no-daemon 2>&1 | Tee-Object "$BuildDir\build.log" | Select-String -Pattern "BUILD|FAILED|error:" -CaseSensitive
$ok = $LASTEXITCODE -eq 0
popd

if ($ok) {
    Copy-Item "$BuildDir\build\libs\customcross-1.0.0.jar" "$BaseDir\build\libs\customcross-$McVer-1.0.0.jar" -Force
    Write-Host "SUCCESS: $McVer -> customcross-$McVer-1.0.0.jar"
} else {
    Write-Host "FAILED: $McVer"
}
