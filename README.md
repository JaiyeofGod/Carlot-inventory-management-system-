# Car Inventory Management System

A comprehensive Java application for managing automobile inventory, sales, and profit tracking.

## Features

- **User-friendly GUI**: Modern interface built with Java Swing and custom theming
- **Inventory Management**: Add, view, and sell cars in your inventory
- **Comprehensive Data Tracking**: Record and track vehicle specs, costs, sales prices, and profits
- **Advanced Filtering**: Sort and search functionality to quickly find vehicles
- **Statistics**: View average MPG, total profits, and other key metrics
- **Data Persistence**: Save and load inventory data using CSV files

## System Requirements

- Java 8 or higher
- Any operating system that supports Java

## Getting Started

### Running the Application

You can run the application in two modes:

1. **GUI Mode** (Default):
   ```
   java -cp bin CarInventoryLauncher
   ```

2. **Console Mode**:
   ```
   java -cp bin CarInventoryLauncher --console
   ```

### Building from Source

1. Clone the repository
2. Compile the Java files:
   ```
   javac -d bin src/*.java
   ```
3. Run the application:
   ```
   java -cp bin CarInventoryLauncher
   ```

## Usage Guide

### Adding a Car

1. Click the "Add Car" button or use the "File" menu
2. Enter the vehicle details:
   - ID (make, model, year)
   - Mileage
   - MPG (Miles Per Gallon)
   - Cost
   - Sales Price
3. Click "Save" to add the car to inventory

### Selling a Car

1. Select a car from the inventory table
2. Click the "Sell Car" button
3. Enter the actual selling price
4. Click "Sell" to record the sale

### Managing Inventory

- Use the search box to filter cars by ID
- Sort by MPG using the "View" menu
- View statistics at the bottom of the window
- Save inventory to disk using the "File" menu or Save button

## Project Structure

- `Car.java`: Class representing individual cars with attributes and methods
- `CarLot.java`: Collection class for managing the car inventory
- `CarInventoryApp.java`: Main GUI application
- `CarInventoryTheme.java`: Custom theme for the application
- `AboutDialog.java`: About dialog with application information
- `CarInventoryLauncher.java`: Entry point with support for GUI and console modes

## License

This project is available for educational purposes.

## Acknowledgments

- Developed as a Java programming project
- Uses Java Swing for the graphical user interface 