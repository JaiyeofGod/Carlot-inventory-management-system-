#!/bin/bash
DIR="$(cd "$(dirname "$0")" && pwd)"
xattr -cr "$DIR/CarLot.app" 2>/dev/null || true
open "$DIR/CarLot.app"
