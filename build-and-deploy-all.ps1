param()

$BaseDir = "C:\Users\user\IdeaProjects\CustomCross"
$OutputDir = "C:\Users\user\Desktop\Frog Studio\000 mod Custom Cross\mods"
$BackupDir = "C:\Users\user\Desktop\Frog Studio\000 mod Custom Cross\backups\mods"
$ModrinthProfiles = "$env:APPDATA\ModrinthApp\profiles"

Set-Location $BaseDir

Write-Host "==================================================" -ForegroundColor Cyan
Write-Host "CUSTOMCROSS COMPLETE RE-BUILD & VERIFY ALL VERSIONS" -ForegroundColor Cyan
Write-Host "==================================================" -ForegroundColor Cyan

# Common libraries
$tr = "C:\Users\user\.gradle\caches\modules-2\files-2.1\net.fabricmc\tiny-remapper\0.10.3\d52becfcf74e17b753a128c9db77eb98b0f15e0a\tiny-remapper-0.10.3.jar"
$asmJars = (Get-ChildItem -Path "C:\Users\user\.gradle\caches\modules-2\files-2.1\org.ow2.asm" -Recurse -Filter "*.jar" | Where-Object { $_.FullName -like "*9.6*" -and $_.FullName -notmatch "sources" } | Select-Object -ExpandProperty FullName)
$mappingIo = "C:\Users\user\.gradle\caches\modules-2\files-2.1\net.fabricmc\mapping-io\0.8.0\6e572f573c1fcd4916a5a3f457f2a8a950eb31b9\mapping-io-0.8.0.jar"
$cpRemapper = ($asmJars + @($tr, $mappingIo)) -join ";"

$gson = (Get-ChildItem -Path "C:\Users\user\.gradle\caches\modules-2\files-2.1\com.google.code.gson\gson" -Recurse -Filter "*.jar" | Where-Object { $_.Name -notmatch "sources" } | Select-Object -First 1).FullName
$slf4j = (Get-ChildItem -Path "C:\Users\user\.gradle\caches\modules-2\files-2.1\org.slf4j\slf4j-api" -Recurse -Filter "*.jar" | Where-Object { $_.Name -notmatch "sources" } | Select-Object -First 1).FullName
$brigadier = (Get-ChildItem -Path "C:\Users\user\.gradle\caches\modules-2\files-2.1\com.mojang\brigadier" -Recurse -Filter "*.jar" | Where-Object { $_.Name -notmatch "sources" } | Select-Object -First 1).FullName
$loader = (Get-ChildItem -Path "C:\Users\user\.gradle\caches\modules-2\files-2.1\net.fabricmc\fabric-loader" -Recurse -Filter "fabric-loader-*.jar" | Where-Object { $_.Name -notmatch "sources" } | Select-Object -First 1).FullName
$joml = (Get-ChildItem -Path "C:\Users\user\.gradle\caches\modules-2\files-2.1\org.joml\joml" -Recurse -Filter "*.jar" | Where-Object { $_.Name -notmatch "sources" } | Select-Object -First 1).FullName
$fastutil = (Get-ChildItem -Path "C:\Users\user\.gradle\caches\modules-2\files-2.1\it.unimi.dsi\fastutil" -Recurse -Filter "*.jar" | Where-Object { $_.Name -notmatch "sources" } | Select-Object -First 1).FullName
$jspecify = (Get-ChildItem -Path "C:\Users\user\.gradle\caches\modules-2\files-2.1\org.jspecify\jspecify" -Recurse -Filter "*.jar" | Where-Object { $_.Name -notmatch "sources" } | Select-Object -First 1).FullName
$dfu = (Get-ChildItem -Path "C:\Users\user\.gradle\caches\modules-2\files-2.1\com.mojang\datafixerupper" -Recurse -Filter "*.jar" | Where-Object { $_.Name -notmatch "sources" } | Select-Object -First 1).FullName
$mixin = (Get-ChildItem -Path "C:\Users\user\.gradle\caches\modules-2\files-2.1\net.fabricmc\sponge-mixin" -Recurse -Filter "*.jar" | Where-Object { $_.Name -notmatch "sources" } | Select-Object -First 1).FullName

# -------------------------------------------------------------
# Configuration for each version
# -------------------------------------------------------------
$sources120 = @(
    "$BaseDir\src_120\client\java\org\example\bindManager\customcross\client\gui\ModernWidgets.java",
    "$BaseDir\src_120\client\java\org\example\bindManager\customcross\client\gui\ColorPickerWidget.java",
    "$BaseDir\src_120\client\java\org\example\bindManager\customcross\client\gui\SettingsScreen.java",
    "$BaseDir\src_120\client\java\org\example\bindManager\customcross\client\render\CrosshairRenderer.java",
    "$BaseDir\src_120\client\java\org\example\bindManager\customcross\client\render\GifCrosshair.java",
    "$BaseDir\src_120\client\java\org\example\bindManager\customcross\mixin\client\InGameHudMixin.java"
)

$sourcesSrc = @(
    "$BaseDir\src\client\java\org\example\bindManager\customcross\client\gui\ModernWidgets.java",
    "$BaseDir\src\client\java\org\example\bindManager\customcross\client\gui\ColorPickerWidget.java",
    "$BaseDir\src\client\java\org\example\bindManager\customcross\client\gui\SettingsScreen.java",
    "$BaseDir\src\client\java\org\example\bindManager\customcross\client\render\CrosshairRenderer.java",
    "$BaseDir\src\client\java\org\example\bindManager\customcross\client\render\GifCrosshair.java"
)

$sources12111 = @(
    "$BaseDir\src_12111\client\java\org\example\bindManager\customcross\client\gui\ModernWidgets.java",
    "$BaseDir\src_12111\client\java\org\example\bindManager\customcross\client\gui\ColorPickerWidget.java",
    "$BaseDir\src_12111\client\java\org\example\bindManager\customcross\client\gui\SettingsScreen.java",
    "$BaseDir\src_12111\client\java\org\example\bindManager\customcross\client\render\CrosshairRenderer.java",
    "$BaseDir\src_12111\client\java\org\example\bindManager\customcross\client\render\GifCrosshair.java"
)

$remapConfigs = @(
    @{
        ver = "1.20.1"
        origJar = "$OutputDir\customcross-1.20.1-1.0.0.jar"
        mapTiny = "C:\Users\user\.gradle\caches\fabric-loom\1.20.1\net.fabricmc.yarn.1_20_1.1.20.1+build.10-v2\mappings.tiny"
        mcYarn = @(
            "C:\Users\user\.gradle\caches\fabric-loom\minecraftMaven\net\minecraft\minecraft-clientonly\1.20.1-net.fabricmc.yarn.1_20_1.1.20.1+build.10-v2\minecraft-clientonly-1.20.1-net.fabricmc.yarn.1_20_1.1.20.1+build.10-v2.jar",
            "C:\Users\user\.gradle\caches\fabric-loom\minecraftMaven\net\minecraft\minecraft-common\1.20.1-net.fabricmc.yarn.1_20_1.1.20.1+build.10-v2\minecraft-common-1.20.1-net.fabricmc.yarn.1_20_1.1.20.1+build.10-v2.jar"
        )
        sources = $sources120
        javaRelease = "17"
    },
    @{
        ver = "1.21.4"
        origJar = "$OutputDir\customcross-1.21.4-1.0.0.jar"
        mapTiny = "C:\Users\user\.gradle\caches\fabric-loom\1.21.4\net.fabricmc.yarn.1_21_4.1.21.4+build.8-v2\mappings.tiny"
        mcYarn = @(
            "C:\Users\user\.gradle\caches\fabric-loom\minecraftMaven\net\minecraft\minecraft-clientonly\1.21.4-net.fabricmc.yarn.1_21_4.1.21.4+build.8-v2\minecraft-clientonly-1.21.4-net.fabricmc.yarn.1_21_4.1.21.4+build.8-v2.jar",
            "C:\Users\user\.gradle\caches\fabric-loom\minecraftMaven\net\minecraft\minecraft-common\1.21.4-net.fabricmc.yarn.1_21_4.1.21.4+build.8-v2\minecraft-common-1.21.4-net.fabricmc.yarn.1_21_4.1.21.4+build.8-v2.jar"
        )
        sources = $sourcesSrc
        javaRelease = "21"
    },
    @{
        ver = "1.21.5"
        origJar = "$OutputDir\customcross-1.21.5-1.0.0.jar"
        mapTiny = "C:\Users\user\.gradle\caches\fabric-loom\1.21.5\net.fabricmc.yarn.1_21_5.1.21.5+build.1-v2\mappings.tiny"
        mcYarn = @(
            "C:\Users\user\.gradle\caches\fabric-loom\minecraftMaven\net\minecraft\minecraft-clientonly\1.21.5-net.fabricmc.yarn.1_21_5.1.21.5+build.1-v2\minecraft-clientonly-1.21.5-net.fabricmc.yarn.1_21_5.1.21.5+build.1-v2.jar",
            "C:\Users\user\.gradle\caches\fabric-loom\minecraftMaven\net\minecraft\minecraft-common\1.21.5-net.fabricmc.yarn.1_21_5.1.21.5+build.1-v2\minecraft-common-1.21.5-net.fabricmc.yarn.1_21_5.1.21.5+build.1-v2.jar"
        )
        sources = $sourcesSrc
        javaRelease = "21"
    },
    @{
        ver = "1.21.11"
        origJar = "$OutputDir\customcross-1.21.11-1.0.0.jar"
        mapTiny = "C:\Users\user\.gradle\caches\fabric-loom\1.21.11\net.fabricmc.yarn.1_21_11.1.21.11+build.6-v2\mappings.tiny"
        mcYarn = @(
            "C:\Users\user\.gradle\caches\fabric-loom\minecraftMaven\net\minecraft\minecraft-merged\1.21.11-net.fabricmc.yarn.1_21_11.1.21.11+build.6-v2\minecraft-merged-1.21.11-net.fabricmc.yarn.1_21_11.1.21.11+build.6-v2.jar"
        )
        sources = $sources12111
        javaRelease = "21"
    }
)

foreach ($cfg in $remapConfigs) {
    $ver = $cfg.ver
    Write-Host "`n>>> Processing Version: $ver" -ForegroundColor Yellow

    $namedJar = "$BaseDir\build\customcross-$ver-named-full.jar"
    Remove-Item -Force $namedJar -ErrorAction SilentlyContinue
    & java -cp $cpRemapper net.fabricmc.tinyremapper.Main $cfg.origJar $namedJar $cfg.mapTiny "intermediary" "named" $cfg.mcYarn
    if ($LASTEXITCODE -ne 0) { throw "Remap to named failed for $ver" }

    $cpCompile = ($cfg.mcYarn + @($namedJar, $gson, $slf4j, $brigadier, $loader, $joml, $fastutil, $jspecify, $dfu, $mixin)) -join ";"
    $compileDir = "$BaseDir\build\compile_${ver}_patch"
    Remove-Item -Recurse -Force $compileDir -ErrorAction SilentlyContinue
    New-Item -ItemType Directory -Force -Path $compileDir | Out-Null

    & javac --release $cfg.javaRelease -cp $cpCompile -d $compileDir $cfg.sources
    if ($LASTEXITCODE -ne 0) { throw "Javac failed for $ver" }

    $inJar = "$BaseDir\build\tmp_${ver}_patch_named.jar"
    $outInterJar = "$BaseDir\build\tmp_${ver}_patch_inter.jar"
    Remove-Item -Force $inJar, $outInterJar -ErrorAction SilentlyContinue
    & jar cf $inJar -C $compileDir .

    & java -cp $cpRemapper net.fabricmc.tinyremapper.Main $inJar $outInterJar $cfg.mapTiny "named" "intermediary" $cfg.mcYarn
    if ($LASTEXITCODE -ne 0) { throw "Remap to intermediary failed for $ver" }

    # Clean repack using tar extraction and jar cf to avoid ZipExceptions
    $cleanDir = "$BaseDir\build\clean_${ver}"
    Remove-Item -Recurse -Force $cleanDir -ErrorAction SilentlyContinue
    New-Item -ItemType Directory -Force -Path $cleanDir | Out-Null
    Set-Location $cleanDir
    tar -xf $cfg.origJar
    tar -xf $outInterJar
    Remove-Item -Force $cfg.origJar
    & jar cf $cfg.origJar .
    Set-Location $BaseDir

    Write-Host ">>> $ver successfully remapped and updated." -ForegroundColor Green
}

# -------------------------------------------------------------
# Copy 1.21.5 base to 1.21.6, 1.21.7, 1.21.8
# -------------------------------------------------------------
Write-Host "`n>>> Updating 1.21.6, 1.21.7, 1.21.8 from 1.21.5..." -ForegroundColor Yellow
$jar1215 = "$OutputDir\customcross-1.21.5-1.0.0.jar"
foreach ($subVer in @("1.21.6", "1.21.7", "1.21.8")) {
    $dest = "$OutputDir\customcross-$subVer-1.0.0.jar"
    Copy-Item $jar1215 $dest -Force
    Write-Host ">>> Updated $dest" -ForegroundColor Green
}

# -------------------------------------------------------------
# 26.x Compilation (Mojang mappings, Java 25)
# -------------------------------------------------------------
Write-Host "`n>>> Processing 26.x (26.1, 26.1.1, 26.1.2, 26.2, 26.3)..." -ForegroundColor Yellow
$mc26Jar = "C:\Users\user\.gradle\caches\fabric-loom\minecraftMaven\net\minecraft\minecraft-merged-deobf\26.1.2\minecraft-merged-deobf-26.1.2.jar"
$orig26 = "$OutputDir\customcross-26.1-1.0.0.jar"
$cp26 = @($mc26Jar, $orig26, $gson, $slf4j, $brigadier, $loader, $joml, $fastutil, $jspecify, $dfu, $mixin) -join ";"

$sources26 = @(
    "$BaseDir\src_26\client\java\org\example\bindManager\customcross\client\gui\ModernWidgets.java",
    "$BaseDir\src_26\client\java\org\example\bindManager\customcross\client\gui\ColorPickerWidget.java",
    "$BaseDir\src_26\client\java\org\example\bindManager\customcross\client\gui\SettingsScreen.java",
    "$BaseDir\src_26\client\java\org\example\bindManager\customcross\client\render\CrosshairRenderer.java",
    "$BaseDir\src_26\client\java\org\example\bindManager\customcross\client\render\GifCrosshair.java",
    "$BaseDir\src_26\client\java\org\example\bindManager\customcross\client\util\BlitHelper.java"
)

$compileDir26 = "$BaseDir\build\compile_26_classes"
Remove-Item -Recurse -Force $compileDir26 -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Force -Path $compileDir26 | Out-Null

& javac --release 25 -cp $cp26 -d $compileDir26 $sources26
if ($LASTEXITCODE -ne 0) { throw "Javac failed for 26.x" }

$patchJar26 = "$BaseDir\build\tmp_26_patch.jar"
Remove-Item -Force $patchJar26 -ErrorAction SilentlyContinue
& jar cf $patchJar26 -C $compileDir26 .

foreach ($v26 in @("26.1", "26.1.1", "26.1.2", "26.2", "26.3")) {
    $j = "$OutputDir\customcross-$v26-1.0.0.jar"
    if (Test-Path $j) {
        $cleanDir26 = "$BaseDir\build\clean_$v26"
        Remove-Item -Recurse -Force $cleanDir26 -ErrorAction SilentlyContinue
        New-Item -ItemType Directory -Force -Path $cleanDir26 | Out-Null
        Set-Location $cleanDir26
        tar -xf $j
        tar -xf $patchJar26
        Remove-Item -Force $j
        & jar cf $j .
        Set-Location $BaseDir
        Write-Host "Updated 26.x classes in $j" -ForegroundColor Green
    }
}

# -------------------------------------------------------------
# Update fabric.mod.json across ALL versions
# -------------------------------------------------------------
Write-Host "`n>>> Updating fabric.mod.json across ALL versions..." -ForegroundColor Yellow

$allVersions = @(
    @{ name = "1.20.1";  dep = "1.20.1" }
    @{ name = "1.21.4";  dep = "1.21.4" }
    @{ name = "1.21.5";  dep = ">=1.21.5 <=1.21.8" }
    @{ name = "1.21.6";  dep = ">=1.21.5 <=1.21.8" }
    @{ name = "1.21.7";  dep = ">=1.21.5 <=1.21.8" }
    @{ name = "1.21.8";  dep = ">=1.21.5 <=1.21.8" }
    @{ name = "1.21.11"; dep = "1.21.11" }
    @{ name = "26.1";    dep = ">=26.1 <=26.1.2" }
    @{ name = "26.1.1";  dep = ">=26.1 <=26.1.2" }
    @{ name = "26.1.2";  dep = ">=26.1 <=26.1.2" }
    @{ name = "26.2";    dep = ">=26.2 <=26.2.2" }
    @{ name = "26.3";    dep = ">=26.3 <=26.3.2" }
)

foreach ($v in $allVersions) {
    $verName = $v.name
    $jarPath = "$OutputDir\customcross-$verName-1.0.0.jar"
    if (-not (Test-Path $jarPath)) {
        Write-Warning "Missing jar: $jarPath"
        continue
    }

    $tempDir = "$BaseDir\build\tmp_fmj_$verName"
    Remove-Item -Recurse -Force $tempDir -ErrorAction SilentlyContinue
    New-Item -ItemType Directory -Force -Path $tempDir | Out-Null

    tar -xf $jarPath -C $tempDir fabric.mod.json
    $fmjFile = "$tempDir\fabric.mod.json"
    if (Test-Path $fmjFile) {
        $json = Get-Content $fmjFile -Raw | ConvertFrom-Json
        $json.contact.sources = "https://github.com/PonosDinozavr/CustomCross/tree/$verName"
        $json.depends.minecraft = $v.dep
        $jsonText = $json | ConvertTo-Json -Depth 10
        Set-Content -Path $fmjFile -Value $jsonText -NoNewline

        Set-Location $tempDir
        & jar uf $jarPath fabric.mod.json
        Set-Location $BaseDir
        Write-Host "Updated fabric.mod.json: $verName -> sources: https://github.com/PonosDinozavr/CustomCross/tree/$verName" -ForegroundColor Green
    }
}

# -------------------------------------------------------------
# Verification of Bytecode
# -------------------------------------------------------------
Write-Host "`n>>> Verifying Bytecode across ALL versions..." -ForegroundColor Yellow

foreach ($v in $allVersions) {
    $verName = $v.name
    $jarPath = "$OutputDir\customcross-$verName-1.0.0.jar"
    if (-not (Test-Path $jarPath)) { continue }

    $toggleCheck = & javap -cp $jarPath org.example.bindManager.customcross.client.gui.ModernWidgets`$Toggle 2>&1 | Select-String "public void (render|extractWidgetRenderState)"
    $sliderCheck = & javap -cp $jarPath org.example.bindManager.customcross.client.gui.ModernWidgets`$Slider 2>&1 | Select-String "public void (render|extractWidgetRenderState)"

    if (-not $toggleCheck) { throw "FAIL: ModernWidgets`$Toggle.render missing in $verName" }
    if (-not $sliderCheck) { throw "FAIL: ModernWidgets`$Slider.render missing in $verName" }

    if ($verName -notlike "26.*") {
        $classes = & jar tf $jarPath | Where-Object { $_ -match "\.class$" -and $_ -notmatch "\$" }
        foreach ($c in $classes) {
            $cn = $c -replace "\.class$", ""
            $output = & javap -cp $jarPath -c $cn 2>&1
            $bad = $output | Select-String '(Method|Field)\s+net/minecraft/class_[0-9]+\.(?!(method_|field_|comp_|<init>|<clinit>))[a-zA-Z]'
            if ($bad) {
                Write-Host "ERROR: Unmapped in $verName ($cn):" -ForegroundColor Red
                $bad | ForEach-Object { Write-Host "   $_" }
                throw "Bytecode verification failed for $verName"
            }
        }
    }
    Write-Host "VERIFIED: $verName passed all checks (Toggle, Slider, zero unmapped)." -ForegroundColor Green
}

# -------------------------------------------------------------
# Deployment to Backup & Modrinth Profiles
# -------------------------------------------------------------
Write-Host "`n>>> Deploying to Backups and ModrinthApp Profiles..." -ForegroundColor Yellow

New-Item -ItemType Directory -Force -Path $BackupDir | Out-Null
Copy-Item "$OutputDir\customcross-*.jar" "$BackupDir\" -Force
Write-Host "Backed up all jars to $BackupDir" -ForegroundColor Green

if (Test-Path $ModrinthProfiles) {
    Get-ChildItem -Directory $ModrinthProfiles | ForEach-Object {
        $modsDir = Join-Path $_.FullName "mods"
        if (Test-Path $modsDir) {
            Get-ChildItem -Path $modsDir -Filter "customcross-*.jar" | ForEach-Object {
                $targetFile = $_.FullName
                $targetName = $_.Name
                $sourceMatch = Get-ChildItem -Path $OutputDir -Filter $targetName | Select-Object -First 1
                if ($sourceMatch) {
                    Copy-Item $sourceMatch.FullName $targetFile -Force
                    Write-Host "  -> Updated Modrinth mod: $targetFile" -ForegroundColor Green
                }
            }
        }
    }
}

Write-Host "`n==================================================" -ForegroundColor Green
Write-Host "ALL BUILDS, VERIFICATIONS, AND DEPLOYMENTS DONE!" -ForegroundColor Green
Write-Host "==================================================" -ForegroundColor Green
