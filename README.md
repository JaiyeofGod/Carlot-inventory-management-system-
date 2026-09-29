# CarLot

CarLot is a desktop app for a used-car lot. You can add cars, mark them sold, search and sort the inventory, and see profit and mileage insights.

## Open the app (easiest)

This is a Mac app. It is not a Windows `.exe`.

1. Download [release/CarLot-macOS.zip](release/CarLot-macOS.zip) from this repository.
2. Unzip it.
3. Open **CarLot.app**.

Use that zip. The `release/CarLot.app` folder in the repository is missing one large runtime file, so it will not open on its own. The zip has the complete app.

The first time macOS blocks it, right-click the app, choose **Open**, then **Open** again.

You do not need to install Java to use the zip.

## What you can do

- See every car in one table.
- Search by ID.
- Filter by **Available** or **Sold**.
- Sort by entry order, MPG, mileage, or asking price.
- **Add car** to put a vehicle in inventory.
- Select a car and **Sell car** to record the real sale price.
- Read the insights panel: average MPG, best MPG, highest mileage, and total profit.
- **Save** the inventory so it is still there the next time you open the app.

A car ID cannot contain spaces, and two cars cannot share the same ID. Profit is the sale price minus what the lot paid for the car.

## Where your inventory is saved

The app saves cars here:

`~/Library/Application Support/CarLot/carlot.txt`

That folder is created the first time you save. Use **File → Save** (or the **Save** button) before you quit if you want to keep new changes.

This repository also includes a sample list of 300 cars in `carlot.txt` at the project root. The app does not load that file automatically. To try those cars, copy it over the file above, then open the app again.

## Run or rebuild it yourself

You need a JDK (Java 17 or newer) for these steps. Packaging the Mac app also needs the `jpackage` tool that comes with the JDK.

Run the already built jar:

```bash
java -jar dist/CarLot.jar
```

Build the jar and the Mac app again:

```bash
./scripts/build.sh
```

That script writes the new app to `release/CarLot.app`.

## Project files

| Path | What it is |
| --- | --- |
| `src/` | Java source (`Car`, `CarLot`, and the window) |
| `release/CarLot-macOS.zip` | The app you should download and open |
| `carlot.txt` | Sample inventory of 300 cars |
| `docs/` | Product notes and the original JavaDoc |
| `scripts/build.sh` | Compiles the source and packages the Mac app |
