# Sales Management System - Build and Run Script
# This script compiles and runs the Sales Management System with proper classpath

$projectPath = Get-Location
$srcPath = "."
$binPath = "bin"

# Ensure bin directory exists
if (-not (Test-Path $binPath)) {
    New-Item -ItemType Directory -Path $binPath | Out-Null
    Write-Host "Created $binPath directory"
}

# Define classpath - include all JARs needed
$jars = @(
    "local-database-module-1.0.0.jar",
    "mysql-connector-j-9.3.0.jar",
    "slf4j-api-2.0.17.jar",
    "slf4j-simple-2.0.17.jar",
    "HikariCP-5.1.0.jar"
)

# Build classpath string
$compileCp = ".;" + ($jars -join ";")

Write-Host "=========================================="
Write-Host "COMPILING Sales Management System"
Write-Host "=========================================="
Write-Host "Compile Classpath: $compileCp"
Write-Host ""

# Compile all Java files
$javaFiles = Get-ChildItem -Path $srcPath -Recurse -Filter "*.java" | ForEach-Object { $_.FullName }

if ($javaFiles.Count -eq 0) {
    Write-Host "No Java files found!"
    exit 1
}

Write-Host "Found $($javaFiles.Count) Java files to compile..."

# Compile
& javac -cp $compileCp -d $binPath @($javaFiles)

if ($LASTEXITCODE -ne 0) {
    Write-Host "Compilation FAILED!"
    exit 1
}

Write-Host "Compilation SUCCESSFUL!"
Write-Host ""
Write-Host "=========================================="
Write-Host "RUNNING Sales Management System"
Write-Host "=========================================="

# Build runtime classpath
$runCp = ".;$binPath;" + ($jars -join ";")

Write-Host "Runtime Classpath: $runCp"
Write-Host ""

# Run the application
& java -cp $runCp SalesManagementSystem

exit $LASTEXITCODE
