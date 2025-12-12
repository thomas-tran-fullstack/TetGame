<#
Diagnostic script for TetGame repository
Runs a sequence of checks and writes output to ./tools/diagnose.log
Usage (PowerShell):
  powershell -ExecutionPolicy Bypass -File tools\diagnose.ps1
#>

$LogFile = Join-Path $PSScriptRoot "diagnose.log"
Set-Content -Path $LogFile -Value "TetGame Diagnostic Log - $(Get-Date -Format o)`n" -Encoding UTF8

function Log {
    param([string]$msg)
    $line = "[$(Get-Date -Format o)] $msg"
    $line | Tee-Object -FilePath $LogFile -Append
}

function RunCmd {
    param([string]$cmd, [string]$workDir = $PWD)
    Log "--- RUN: $cmd (wd: $workDir) ---"
    try {
        $procInfo = New-Object System.Diagnostics.ProcessStartInfo
        $procInfo.FileName = 'powershell'
        $procInfo.Arguments = "-NoProfile -Command `"Set-Location -LiteralPath '$workDir'; $cmd`""
        $procInfo.RedirectStandardOutput = $true
        $procInfo.RedirectStandardError = $true
        $procInfo.UseShellExecute = $false
        $proc = New-Object System.Diagnostics.Process
        $proc.StartInfo = $procInfo
        $proc.Start() | Out-Null
        $stdout = $proc.StandardOutput.ReadToEnd()
        $stderr = $proc.StandardError.ReadToEnd()
        $proc.WaitForExit()
        if ($stdout) { $stdout | Tee-Object -FilePath $LogFile -Append }
        if ($stderr) { "[stderr]" | Tee-Object -FilePath $LogFile -Append; $stderr | Tee-Object -FilePath $LogFile -Append }
        Log "--- EXIT CODE: $($proc.ExitCode) ---"
        return $proc.ExitCode
    } catch {
        Log "Exception while running command: $_"
        return 1
    }
}

# Start checks
Log "Environment info"
RunCmd "node -v" | Out-Null
RunCmd "npm -v" | Out-Null
RunCmd "java -version" | Out-Null
RunCmd "mvn -v" | Out-Null
RunCmd "docker --version" | Out-Null

# Check frontend dist
$frontendDist = Join-Path $PSScriptRoot '..\frontend\dist' | Resolve-Path -ErrorAction SilentlyContinue
if ($null -ne $frontendDist) {
    Log "Found frontend/dist at $frontendDist"
    $files = Get-ChildItem -Path (Join-Path $PSScriptRoot '..\frontend\dist') -Recurse -File -ErrorAction SilentlyContinue | Select-Object -First 20
    if ($files) { $files | ForEach-Object { Log "dist file: $($_.FullName)" } }
    else { Log "frontend/dist appears empty" }
} else {
    Log "frontend/dist NOT found. Attempting local build (npm ci && npm run build)."
    $nodeExit = RunCmd "npm ci; npm run build" (Join-Path $PSScriptRoot '..\frontend')
    if ($nodeExit -ne 0) { Log "Frontend build failed (exit $nodeExit)" }
}

# Ensure frontend dist exists now
if (-Not (Test-Path (Join-Path $PSScriptRoot '..\frontend\dist'))) {
    Log "ERROR: frontend/dist still missing after attempted build. Stop further checks."
    Log "You should fix frontend build errors above and re-run this script."
    exit 1
}

# Copy frontend dist to backend resources (simulate Docker stage)
$targetStatic = Join-Path $PSScriptRoot '..\backend\src\main\resources\static'
if (-Not (Test-Path $targetStatic)) { New-Item -ItemType Directory -Path $targetStatic -Force | Out-Null }
Log "Copying frontend/dist -> $targetStatic"
Copy-Item -Path (Join-Path $PSScriptRoot '..\frontend\dist\*') -Destination $targetStatic -Recurse -Force

# Run mvn package
$mvExit = RunCmd "mvn -f backend/pom.xml -DskipTests package" | Out-Null

# Inspect jar for index.html
$jarPath = Join-Path $PSScriptRoot '..\backend\target\backend-0.0.1-SNAPSHOT.jar'
if (Test-Path $jarPath) {
    Log "Jar found: $jarPath"
    try {
        Add-Type -AssemblyName System.IO.Compression.FileSystem -ErrorAction SilentlyContinue
        $entries = [System.IO.Compression.ZipFile]::OpenRead($jarPath).Entries | Where-Object { $_.FullName -match 'index\.html' }
        if ($entries.Count -gt 0) {
            foreach ($e in $entries) { Log "Jar contains: $($e.FullName)" }
        } else {
            Log "Jar does NOT contain index.html"
        }
    } catch {
        Log "Exception while inspecting jar: $_"
    }
} else {
    Log "Jar not found at $jarPath"
}

# Try running jar for a short smoke test (background)
if (Test-Path $jarPath) {
    Log "Starting jar (background) with PORT=8080"
    $env:PORT = 8080
    $psi = New-Object System.Diagnostics.ProcessStartInfo
    $psi.FileName = 'java'
    $psi.Arguments = "-Dserver.port=8080 -jar `"$jarPath`""
    $psi.RedirectStandardOutput = $true
    $psi.RedirectStandardError = $true
    $psi.UseShellExecute = $false
    $proc = New-Object System.Diagnostics.Process
    $proc.StartInfo = $psi
    $proc.Start() | Out-Null
    Start-Sleep -Seconds 8
    try {
        $resp = Invoke-WebRequest -UseBasicParsing -Uri http://localhost:8080/ -TimeoutSec 5
        Log "HTTP GET / -> status: $($resp.StatusCode)"
        $bodyHead = $resp.Content.Substring(0,[Math]::Min(400,$resp.Content.Length))
        Log "Response head: $bodyHead"
    } catch {
        Log "HTTP request failed: $_"
    }
    # Stop process
    try { $proc.Kill() } catch { }
    Start-Sleep -Seconds 1
}

# Try docker build (if docker available)
try {
    $dockerVersion = (& docker --version) -join "`n"
    if ($LASTEXITCODE -eq 0) {
        Log "Docker available: $dockerVersion"
        RunCmd "docker build -t tetgame:local ."
    } else {
        Log "Docker reported non-zero exit code; skipping docker build"
    }
} catch {
    Log "Docker not available or failed: $_"
}

Log "Diagnostic finished. See $LogFile"
Write-Output "Diagnostic finished. Log saved to $LogFile"
