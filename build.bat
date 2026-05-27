@echo 
REM ============================================================
REM  AlgoSeries-Pro — Build Script (Windows)
REM  Usage: Double-click or run from Command Prompt
REM ============================================================

echo ===================================================
echo   AlgoSeries-Pro — Build Script
echo ===================================================

REM Check javac
where javac >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] javac not found. Install JDK 11+ and add to PATH.
    pause
    exit /b 1
)

echo [INFO] Cleaning previous build...
if exist out rmdir /s /q out
if exist AlgoSeries-Pro.jar del AlgoSeries-Pro.jar
mkdir out

echo [INFO] Compiling sources...
dir /s /b src\*.java > sources.txt
javac -cp "lib\jfreechart.jar;lib\jcommon.jar" -d out @sources.txt

echo [INFO] Bundling dependencies...
cd out
jar xf ..\lib\jfreechart.jar
jar xf ..\lib\jcommon.jar
cd ..

echo [INFO] Creating manifest...
(
echo Main-Class: com.algoseries.Main
echo Class-Path: .
) > manifest.mf

echo [INFO] Packaging JAR...
jar cfm AlgoSeries-Pro.jar manifest.mf -C out .
del sources.txt
del manifest.mf

echo.
echo ===================================================
echo   Build successful!
echo   Run with:  java -jar AlgoSeries-Pro.jar
echo ===================================================
pause
