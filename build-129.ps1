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

$patchDir = "$BaseDir\patches\129"

# Patch 1: drawBorder -> fill-based outline in CrosshairRenderer.java
$f = "$BuildDir\src\client\java\org\example\bindManager\customcross\client\render\CrosshairRenderer.java"
(Get-Content $f -Raw) -replace 'context\.drawBorder\(cx - \(int\) radius, cy - \(int\) radius, \(int\) radius \* 2, \(int\) radius \* 2, color\);', @'
int x = cx - (int) radius, y = cy - (int) radius, r2 = (int) radius * 2;
        context.fill(x, y, x + r2, y + 1, color);
        context.fill(x, y + r2 - 1, x + r2, y + r2, color);
        context.fill(x, y, x + 1, y + r2, color);
        context.fill(x + r2 - 1, y, x + r2, y + r2, color);
'@ | Set-Content $f -NoNewline

# Patch 2: drawBorder -> fill-based outline in SettingsScreen.java + ColorPickerWidget.java + ModernWidgets.java
$files = @(
    "$BuildDir\src\client\java\org\example\bindManager\customcross\client\gui\SettingsScreen.java",
    "$BuildDir\src\client\java\org\example\bindManager\customcross\client\gui\ColorPickerWidget.java",
    "$BuildDir\src\client\java\org\example\bindManager\customcross\client\gui\ModernWidgets.java"
)
foreach ($f in $files) {
    $content = Get-Content $f -Raw
    # Replace drawBorder(x, y, w, h, color) with inline fill calls
    $content = $content -replace 'context\.drawBorder\((\w+),\s*(\w+),\s*(\w+),\s*(\w+),\s*(\w+)\)', @'
drawBorder(context, $1, $2, $3, $4, $5)
'@
    Set-Content $f -Value $content -NoNewline
}
# Add drawBorder helper to each file
$borderHelper = @'

    private static void drawBorder(DrawContext ctx, int x, int y, int w, int h, int color) {
        ctx.fill(x, y, x + w, y + 1, color);
        ctx.fill(x, y + h - 1, x + w, y + h, color);
        ctx.fill(x, y, x + 1, y + h, color);
        ctx.fill(x + w - 1, y, x + w, y + h, color);
    }
'@

# SettingsScreen.java - add border helper before last }
$f = "$BuildDir\src\client\java\org\example\bindManager\customcross\client\gui\SettingsScreen.java"
$content = Get-Content $f -Raw
$content = $content.TrimEnd("`r`n}")
$content + $borderHelper + "`n}" | Set-Content $f -NoNewline

# ColorPickerWidget.java - add border helper before last }
$f = "$BuildDir\src\client\java\org\example\bindManager\customcross\client\gui\ColorPickerWidget.java"
$content = Get-Content $f -Raw
$content = $content.TrimEnd("`r`n}")
$content + $borderHelper + "`n}" | Set-Content $f -NoNewline

# ModernWidgets.java - add border helper before last }
$f = "$BuildDir\src\client\java\org\example\bindManager\customcross\client\gui\ModernWidgets.java"
$content = Get-Content $f -Raw
$content = $content.TrimEnd("`r`n}")
$content + $borderHelper + "`n}" | Set-Content $f -NoNewline

# Patch 3: KeyBinding.Category (already in source)

# Build
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
