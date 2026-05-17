#!/usr/bin/env bash
# ============================================================
#  AlgoSeries-Pro — Build Script (Linux / macOS)
#  Usage: chmod +x build.sh && ./build.sh
# ============================================================
set -e

echo "==================================================="
echo "  AlgoSeries-Pro — Build Script"
echo "==================================================="

# Check java / javac
if ! command -v javac &>/dev/null; then
    echo "[ERROR] javac not found. Install JDK 11+ and add to PATH."
    exit 1
fi

echo "[INFO] Java version: $(java -version 2>&1 | head -1)"

# Clean
rm -rf out AlgoSeries-Pro.jar
mkdir -p out

# Compile
echo "[INFO] Compiling sources..."
find src -name "*.java" > sources.txt
javac -cp "lib/jfreechart.jar:lib/jcommon.jar" -d out @sources.txt

# Extract dependencies into out/
echo "[INFO] Bundling dependencies..."
cd out
jar xf ../lib/jfreechart.jar
jar xf ../lib/jcommon.jar
cd ..

# Manifest
cat > manifest.mf <<EOF
Main-Class: com.algoseries.Main
Class-Path: .
EOF

# Package
echo "[INFO] Creating runnable JAR..."
jar cfm AlgoSeries-Pro.jar manifest.mf -C out .
rm -f sources.txt manifest.mf

echo ""
echo "==================================================="
echo "  Build successful!"
echo "  Run with:  java -jar AlgoSeries-Pro.jar"
echo "==================================================="
