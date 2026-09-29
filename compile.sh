#!/bin/bash

# Car Inventory Management System Compilation Script

echo "Compiling Car Inventory Management System..."

# Create bin directory if it doesn't exist
mkdir -p bin

# Compile all Java files from src directory to bin
javac -d bin src/*.java

# Check if compilation was successful
if [ $? -eq 0 ]; then
    echo "Compilation successful!"
    echo "To run the application in GUI mode, use: java -cp bin CarInventoryLauncher"
    echo "To run the application in console mode, use: java -cp bin CarInventoryLauncher --console"
else
    echo "Compilation failed!"
fi 