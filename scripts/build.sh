#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
export JAVA_HOME="${JAVA_HOME:-/Library/Java/JavaVirtualMachines/jdk-22.jdk/Contents/Home}"
export PATH="$JAVA_HOME/bin:$PATH"

SRC="$ROOT/src"
BUILD="$ROOT/build"
DIST="$ROOT/dist"
APP_OUT="$ROOT/release"

mkdir -p "$BUILD" "$DIST" "$APP_OUT"
rm -rf "$BUILD"/* "$DIST"/* "$APP_OUT"/CarLot.app "$APP_OUT"/CarLot-*.dmg 2>/dev/null || true

echo "==> Compiling"
javac --release 17 -d "$BUILD" "$SRC"/*.java

echo "==> Creating jar"
jar --create --file "$DIST/CarLot.jar" --main-class CarLotApp -C "$BUILD" .

echo "==> Packaging macOS app"
jpackage \
  --type app-image \
  --name CarLot \
  --app-version 1.0.0 \
  --input "$DIST" \
  --main-jar CarLot.jar \
  --main-class CarLotApp \
  --dest "$APP_OUT" \
  --java-options "-Dapple.awt.application.name=CarLot" \
  --java-options "-Dfile.encoding=UTF-8"

# Clear quarantine so Finder can open the app locally
xattr -cr "$APP_OUT/CarLot.app" 2>/dev/null || true

# Convenience launcher next to the app
cat > "$APP_OUT/Launch CarLot.command" <<'EOF'
#!/bin/bash
DIR="$(cd "$(dirname "$0")" && pwd)"
xattr -cr "$DIR/CarLot.app" 2>/dev/null || true
open "$DIR/CarLot.app"
EOF
chmod +x "$APP_OUT/Launch CarLot.command"
rm -f "$APP_OUT/Open CarLot.command" 2>/dev/null || true

echo
echo "Build complete."
echo "App: $APP_OUT/CarLot.app"
echo "Run: open \"$APP_OUT/CarLot.app\""
echo "Data: ~/Library/Application Support/CarLot/carlot.txt"
