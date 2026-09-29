@echo off
REM Car Inventory Management System Compilation Script

echo Compiling Car Inventory Management System...

REM Create bin directory if it doesn't exist
if not exist bin mkdir bin

REM Compile all Java files from src directory to bin
javac -d bin src\*.java

REM Check if compilation was successful
if %ERRORLEVEL% EQU 0 (
    echo Compilation successful!
    echo To run the application in GUI mode, use: java -cp bin CarInventoryLauncher
    echo To run the application in console mode, use: java -cp bin CarInventoryLauncher --console
) else (
    echo Compilation failed!
)

pause 