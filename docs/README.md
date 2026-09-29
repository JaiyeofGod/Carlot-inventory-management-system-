# CarLot

Desktop inventory app for managing a used-car lot. Built from the CarLot JavaDoc domain (`Car`, `CarLot`) with a standard Swing inventory UI, packaged as a macOS app.

## Open the app

```bash
open "/Users/jaiye/Downloads/Car copy/release/CarLot.app"
```

Or double-click **`release/CarLot.app`** in Finder.

## What you can do

- Browse inventory in a table (search, status filter, sort)
- Add cars with validation (unique ID, no spaces)
- Sell cars with profit preview (`priceSold − cost`)
- See insights: average MPG, best MPG, highest mileage, total profit
- Save / load inventory

## Data location

Inventory is stored at:

`~/Library/Application Support/CarLot/carlot.txt`

## Rebuild

```bash
./scripts/build.sh
```

Requires JDK 22 at `/Library/Java/JavaVirtualMachines/jdk-22.jdk` (or set `JAVA_HOME`).

## Run from source (dev)

```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-22.jdk/Contents/Home
"$JAVA_HOME/bin/java" -jar dist/CarLot.jar
```
