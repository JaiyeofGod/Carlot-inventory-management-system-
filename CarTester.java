
import java.io.*;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


// The CarTester class is used to test the functionality of the Car class and the CarLot class.
public class CarTester {
    public static void main(String[] args) {
        // Create instances of Car
        Car car1 = new Car("1985 Ferrari 308GT", 10000, 30, 12500.00, 17500.00);
        Car car2 = new Car();
        car2.setId("2022 Lamborghini Aventador");
        car2.setMileage(5000);
        car2.setMpg(15);
        car2.setCost(150000.00);
        car2.setSalesPrice(200000.00);

        // Test the sellCar() method for car1
        car1.sellCar(18000.00);

        // Print comparison results between car1 and car2
        System.out.println("Comparison by MPG:");
        System.out.println("car1 vs car2: " + car1.compareMPG(car2));
        System.out.println("car2 vs car1: " + car2.compareMPG(car1));

        System.out.println("\nComparison by Miles:");
        System.out.println("car1 vs car2: " + car1.compareMiles(car2));
        System.out.println("car2 vs car1: " + car2.compareMiles(car1));

        System.out.println("\nComparison by Price:");
        System.out.println("car1 vs car2: " + car1.comparePrice(car2));
        System.out.println("car2 vs car1: " + car2.comparePrice(car1));

        // Print car details of car1 and car2
        System.out.println("\nCar 1 details:");
        System.out.println(car1);

        System.out.println("\nCar 2 details:");
        System.out.println(car2);

        // Create CarLot instance
        CarLot carLot = new CarLot();

        // Add cars to the CarLot
        carLot.add(new Car("123", 5000, 25, 12000.0, 15000.0));
        carLot.add(new Car("456", 10000, 30, 25000.0, 30000.0));

        // Print initial inventory
        System.out.println("\nInitial Inventory:");
        printInventory(carLot);

        // Test sellCar() method in CarLot
        try {
            carLot.sellCar("123", 16000.0);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }

        try {
            carLot.sellCar("789", 20000.0);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }

        // Print inventory after selling a car
        System.out.println("\nAfter Selling a Car:");
        printInventory(carLot);

        // Test accessor methods
        testAccessors(carLot);

        // Test saveToDisk() and loadFromDisk() methods
        System.out.println("\nSaving to disk...");
        carLot.saveToDisk();

        // Create a new CarLot instance and load data from disk
        CarLot loadedCarLot = new CarLot();
        System.out.println("\nLoading from disk...");
        loadedCarLot.loadFromDisk();

        // Print inventory from the loaded CarLot
        System.out.println("\nInventory Loaded from Disk:");
        printInventory(loadedCarLot);

        // Test the menu-driven CarLotMain class
        CarLotMain carLotMain = new CarLotMain();
        carLotMain.CarLotMain1();
    }

    // Method to print the inventory of the CarLot
    private static void printInventory(CarLot carLot) {
        System.out.println("Inventory:");
        for (Car car : carLot) {
            System.out.println(car);
        }
    }

    // Method to test various accessor methods of CarLot
    private static void testAccessors(CarLot carLot) {
        System.out.println("\nCars in order of entry:");
        for (Car car : carLot.getCarsInOrderOfEntry()) {
            System.out.println(car.getId());
        }

        System.out.println("\nCars sorted by MPG:");
        for (Car car : carLot.getCarsSortedByMPG()) {
            System.out.println(car.getId() + " - MPG: " + car.getMpg());
        }

        System.out.println("\nCar with best MPG:");
        System.out.println(carLot.getCarWithBestMPG().getId() + " - MPG: " + carLot.getCarWithBestMPG().getMpg());

        System.out.println("\nCar with highest mileage:");
        System.out.println(carLot.getCarWithHighestMileage().getId() + " - Mileage: " + carLot.getCarWithHighestMileage().getMileage());

        System.out.println("\nAverage MPG:");
        System.out.println(carLot.getAverageMPG());

        System.out.println("\nTotal Profit:");
        System.out.println(carLot.getTotalProfit());
    }
}

// The Car class represents a car in the car lot inventory with attributes and methods related to each car.
// Each car has its own ID, mileage, MPG, cost, sales price, sold status, price sold, and profit made from the sale of the car.
class Car {

    private String id;
    private int mileage;
    private int mpg;
    private double cost;
    private double salesPrice;
    private boolean sold;
    private double priceSold;

    // Default constructor.
    public Car() {
        // Initializes all attributes to default values.
        this.id = "";
        this.mileage = 0;
        this.mpg = 0;
        this.cost = 0.0;
        this.salesPrice = 0.0;
        this.sold = false;
        this.priceSold = 0.0;
    }

    // Constructor to add a new Car to the inventory.
    public Car(String id, int mileage, int mpg, double cost, double salesPrice) {
        this.id = id;  // The unique identifier for the car.
        this.mileage = mileage;    // The mileage of the car.
        this.mpg = mpg;     // The fuel efficiency of the car in miles per gallon.
        this.cost = cost;  // The cost of the car.
        this.salesPrice = salesPrice;   // The asking price of the car.
        this.sold = false;
        this.priceSold = 0.0;
    }

    // Accessor for ID attribute.
    public String getId() {
        return id;
    }

    // Accessor for Mileage attribute.
    public int getMileage() {
        return mileage;
    }

    // Accessor for MPG attribute.
    public int getMpg() {
        return mpg;
    }

    // Accessor for Cost attribute.
    public double getCost() {
        return cost;
    }

    // Accessor for SalesPrice attribute.
    public double getSalesPrice() {
        return salesPrice;
    }

    // Accessor for Sold attribute.
    public boolean isSold() {
        return sold;
    }

    // Accessor for PriceSold attribute.
    public double getPriceSold() {
        return priceSold;
    }

    // Accessor for Profit attribute.
    public double getProfit() {
        if (sold) {
            return priceSold - cost;
        } else {
            return 0.0;
        }
    }

    // Mutator for ID attribute.
    public void setId(String id) {
        this.id = id;
    }

    // Mutator for Mileage attribute.
    public void setMileage(int mileage) {
        this.mileage = mileage;
    }

    // Mutator for MPG attribute.
    public void setMpg(int mpg) {
        this.mpg = mpg;
    }

    // Mutator for Cost attribute.
    public void setCost(double cost) {
        this.cost = cost;
    }

    // Mutator for SalesPrice attribute.
    public void setSalesPrice(double salesPrice) {
        this.salesPrice = salesPrice;
    }

    // Method to mark the car as sold and set the selling price
    public void sellCar(double priceSold) {
        this.sold = true;
        this.priceSold = priceSold;
    }

    // Comparison method to compare MPG between this car and another car.
    public int compareMPG(Car otherCar) {
        return this.mpg - otherCar.getMpg();
    }

    // Comparison method to compare mileage between this car and another car.
    public int compareMiles(Car otherCar) {
        return this.mileage - otherCar.getMileage();
    }

    // Comparison method to compare sales price between this car and another car.
    public int comparePrice(Car otherCar) {
        return Double.compare(this.salesPrice, otherCar.getSalesPrice());
    }

    // Method to provide a human-consumable string representation of the car.
    @Override
    public String toString() {
        String soldStatus = sold ? "Yes" : "No";
        String profitInfo = sold ? String.format(", Sold For $%.2f, Profit: $%.2f", priceSold, getProfit()) : "";
        return String.format("Car: %s, Mileage: %d, MPG: %d, Sold: %s, Cost: $%.2f, Selling Price: $%.2f%s",
                id, mileage, mpg, soldStatus, cost, salesPrice, profitInfo);


        
    }

    // Convert Car instance to CSV string
    public String toCSV() {
        return String.join(",", id, String.valueOf(mileage), String.valueOf(mpg),
                String.valueOf(cost), String.valueOf(salesPrice), String.valueOf(sold),
                String.valueOf(priceSold));
    }

    // Static method to create a Car instance from a CSV string
    public static Car fromCSV(String csv) {
        String[] fields = csv.split(",");
        Car car = new Car();
        car.setId(fields[0]);
        car.setMileage(Integer.parseInt(fields[1]));
        car.setMpg(Integer.parseInt(fields[2]));
        car.setCost(Double.parseDouble(fields[3]));
        car.setSalesPrice(Double.parseDouble(fields[4]));
        car.setSold(Boolean.parseBoolean(fields[5]));
        car.setPriceSold(Double.parseDouble(fields[6]));
        return car;
    }

    // Mutator for Sold attribute
    public void setSold(boolean sold) {
        this.sold = sold;
    }

    // Mutator for PriceSold attribute
    public void setPriceSold(double priceSold) {
        this.priceSold = priceSold;
    }
}

// CarLot now extends ArrayList<Car>
class CarLot extends ArrayList<Car> {

    // No need for the inventory attribute anymore
    // Inherited methods from ArrayList<Car> will be used directly
    public CarLot() {
        super();
    }

    // Find a car by its Identification
    public Car findCarByIdentifier(String identifier) {
        for (Car car : this) {
            if (car.getId().equals(identifier)) {
                return car;
            }
        }
        return null;
    }

    public ArrayList<Car> getCarsInOrderOfEntry() {
        return new ArrayList<>(this);
    }

    // Get cars sorted by MPG in descending order
    public ArrayList<Car> getCarsSortedByMPG() {
        ArrayList<Car> copyOfInventory = new ArrayList<>(this);
        Collections.sort(copyOfInventory, Comparator.comparingInt(Car::getMpg));
        return copyOfInventory;
    }

    // Get the car with the best MPG
    public Car getCarWithBestMPG() {
        if (this.isEmpty()) {
            return null;
        }
        Car bestMPGCar = this.get(0);
        for (Car car : this) {
            if (car.getMpg() > bestMPGCar.getMpg()) {
                bestMPGCar = car;
            }
        }
        return bestMPGCar;
    }

    // Get the car with the highest mileage
    public Car getCarWithHighestMileage() {
        if (this.isEmpty()) {
            return null;
        }

        Car highMileageCar = this.get(0);
        for (Car car : this) {
            if (car.getMileage() > highMileageCar.getMileage()) {
                highMileageCar = car;
            }
        }
        return highMileageCar;
    }

    // Calculate average MPG of all cars in the CarLot
    public double getAverageMPG() {
        if (this.isEmpty()) {
            return 0;
        }
        double totalMPG = 0;
        for (Car car : this) {
            totalMPG += car.getMpg();
        }
        return totalMPG / this.size();
    }

    // Calculate total profit from sold cars
    public double getTotalProfit() {
        double totalProfit = 0;
        for (Car car : this) {
            if (car.isSold()) {
                totalProfit += car.getProfit();
            }
        }
        return totalProfit;
    }

    public void addCar(String id, int mileage, int mpg, double cost, double salesPrice) {
        Car newCar = new Car(id, mileage, mpg, cost, salesPrice);
        this.add(newCar);
    }

    public void sellCar(String identifier, double priceSold) throws IllegalArgumentException {
        Car carToSell = findCarByIdentifier(identifier);
        if (carToSell == null) {
            throw new IllegalArgumentException("Car with identifier " + identifier + " not found. ");
        }
        carToSell.sellCar(priceSold);

    }

    // Save the CarLot inventory to a CSV file
    public void saveToDisk() {
        try (PrintWriter writer = new PrintWriter(new File("carlot.csv"))) {
            for (Car car : this) {
                writer.println(car.toCSV());
            }
        } catch (IOException e) {
            System.out.println("An error occurred while saving to disk: " + e.getMessage());
        }
    }

    // Load CarLot inventory from a CSV file
    public void loadFromDisk() {
        try (BufferedReader reader = new BufferedReader(new FileReader("carlot.csv"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Car car = Car.fromCSV(line);
                this.add(car);
            }
        } catch (IOException e) {
            System.out.println("An error occurred while loading from disk: " + e.getMessage());
        }
    }
}


// The CarLotMain class handles user interaction through a menu-driven interface
class CarLotMain {

    // The main menu loop for CarLot management
    Scanner scanner = new Scanner(System.in);
    int option;
    // Static CarLot instance for managing inventory
    static CarLot carLot = new CarLot();

    // Main method for menu-driven interface
    public void CarLotMain1() {
        do {
            printMenu();
            option = getUserInput();
            switch (option) {
                case 1:
                    // Add a car to the CarLot
                    addCar();
                    break;
                case 2:
                    // Sell a car
                    sellCar();
                    break;
                case 3:
                    // Print inventory
                    printInventoryByOrder();
                    break;
                case 4:
                    printInventoryByMPG();
                    break;
                case 5:
                    displayCarWithBestMPG();
                    break;
                case 6:
                    displayCarWithHighestMileage();
                    break;
                case 7:
                    displayAverageMPG();
                    break;
                case 8:
                    displayTotalProfit();
                    break;
                case 9:
                    // Save to disk
                    carLot.saveToDisk();
                    System.out.println("Inventory saved to disk.");
                    break;
                case 10:
                    // Load from disk
                    carLot.loadFromDisk();
                    System.out.println("Inventory loaded from disk.");
                    break;
                case 0:
                    System.out.println("Exiting...");
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        } while (option != 0);
    }

    // Main method for menu-driven interface
    private void printMenu() {
        System.out.println("\nCar Lot Menu:");
        System.out.println("[0] Exit");
        System.out.println("[1] Add a car to inventory");
        System.out.println("[2] Sell a car from inventory");
        System.out.println("[3] List inventory by order of acquisition");
        System.out.println("[4] List Inventory From Best MPG to Worst MPG");
        System.out.println("[5] Display car with best MPG");
        System.out.println("[6] Display car with the highest mileage");
        System.out.println("[7] Display overall MPG for the entire inventory");
        System.out.println("[8] Display profit for all sold cars");
        System.out.println("[9] Save inventory to disk");
        System.out.println("[10] Load inventory from disk");
        System.out.print("Enter a number from 0 to 10: ");
    }

    public int getUserInput() {
        int option = -1;
        while (option < 0 || option > 10) {
            try {
                option = scanner.nextInt();
                scanner.nextLine(); // consume the newline character
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a number from 0 to 10.");
                scanner.next(); // clear the invalid input
            }
        }
        return option;
    }

    public void addCar() {
        System.out.print("Enter car identifier: ");
        String id = scanner.nextLine();

        int mileage = getIntInput("Enter car mileage: ");
        int mpg = getIntInput("Enter car MPG: ");
        double cost = getDoubleInput("Enter car cost: ");
        double salesPrice = getDoubleInput("Enter car sales price: ");

        carLot.addCar(id, mileage, mpg, cost, salesPrice);
        System.out.println("Car added to inventory.");
    }

    public void sellCar() {
        System.out.print("Enter car identifier to sell: ");
        String id = scanner.nextLine();

        double priceSold = getDoubleInput("Enter actual price sold: ");
        try {
            carLot.sellCar(id, priceSold);
            System.out.println("Car sold.");
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    public void printInventoryByOrder() {
        System.out.println("\nInventory by Order of Acquisition:");
        for (Car car : carLot.getCarsInOrderOfEntry()) {
            System.out.println(car);
        }
    }

    public void printInventoryByMPG() {
        System.out.println("\nInventory Sorted from Best MPG to Worst MPG:");
        for (Car car : carLot.getCarsSortedByMPG()) {
            System.out.println(car);
        }
    }

    public void displayCarWithBestMPG() {
        Car bestMPGCar = carLot.getCarWithBestMPG();
        if (bestMPGCar != null) {
            System.out.println("\nCar with Best MPG:");
            System.out.println(bestMPGCar);
        } else {
            System.out.println("No cars in inventory.");
        }
    }

    public void displayCarWithHighestMileage() {
        Car highestMileageCar = carLot.getCarWithHighestMileage();
        if (highestMileageCar != null) {
            System.out.println("\nCar with Highest Mileage:");
            System.out.println(highestMileageCar);
        } else {
            System.out.println("No cars in inventory.");
        }
    }

    public void displayAverageMPG() {
        double averageMPG = carLot.getAverageMPG();
        System.out.printf("\nAverage MPG: %.2f\n", averageMPG);
    }

    public void displayTotalProfit() {
        double totalProfit = carLot.getTotalProfit();
        System.out.printf("\nTotal Profit: $%.2f\n", totalProfit);
    }

    public int getIntInput(String prompt) {
        int value = -1;
        while (value < 0) {
            System.out.print(prompt);
            try {
                value = scanner.nextInt();
                scanner.nextLine(); // consume the newline character
                if (value < 0) {
                    System.out.println("Value must be non-negative. Please try again.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a valid integer.");
                scanner.next(); // clear the invalid input
            }
        }
        return value;
    }

    public double getDoubleInput(String prompt) {
        double value = -1;
        while (value < 0) {
            System.out.print(prompt);
            try {
                value = scanner.nextDouble();
                scanner.nextLine(); // consume the newline character
                if (value < 0) {
                    System.out.println("Value must be non-negative. Please try again.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a valid number.");
                scanner.next(); // clear the invalid input
            }
        }
        return value;
    }
    // Method to save CarLot inventory to disk
    public void saveToDisk() {
        try (PrintWriter writer = new PrintWriter(new File("carlot.csv"))) {
            for (Car car : carLot) {
                writer.println(car.toCSV());
            }
            System.out.println("Inventory saved to disk successfully.");
        } catch (IOException e) {
            System.out.println("An error occurred while saving to disk: " + e.getMessage());
        }
    }

    // Method to load CarLot inventory from disk
    public void loadFromDisk() {
        try (BufferedReader reader = new BufferedReader(new FileReader("carlot.csv"))) {
            String line;
            // Clear the current inventory before loading
            carLot.clear();
            while ((line = reader.readLine()) != null) {
                Car car = Car.fromCSV(line);
                carLot.add(car);
            }
            System.out.println("Inventory loaded from disk successfully.");
        } catch (IOException e) {
            System.out.println("An error occurred while loading from disk: " + e.getMessage());
        }
    }
}





