import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Scanner;

/**
 * A CarLot represents a collection of distinct Cars that serve as the car lot's
 * inventory and associated operations on the car lot.
 */
public class CarLot {

    /** File name used for inventory persistence. */
    public static final String CARLOT_INVENTORY_FILENAME = "carlot.txt";

    /**
     * Absolute path where the inventory of a CarLot is stored.
     * Resolves to ~/Library/Application Support/CarLot/carlot.txt on macOS,
     * or an equivalent user data directory on other platforms.
     */
    public static final String CARLOT_INVENTORY_LOCATION = resolveInventoryLocation().toString();

    private ArrayList<Car> inventory;

    /** Default constructor. Initializes the inventory. */
    public CarLot() {
        this.inventory = new ArrayList<>();
    }

    private static Path resolveInventoryLocation() {
        String override = System.getProperty("carlot.inventory");
        if (override != null && !override.isBlank()) {
            return Path.of(override).toAbsolutePath();
        }

        String os = System.getProperty("os.name", "").toLowerCase();
        Path base;
        if (os.contains("mac")) {
            base = Path.of(System.getProperty("user.home"), "Library", "Application Support", "CarLot");
        } else if (os.contains("win")) {
            String appData = System.getenv("APPDATA");
            if (appData == null || appData.isBlank()) {
                appData = System.getProperty("user.home");
            }
            base = Path.of(appData, "CarLot");
        } else {
            base = Path.of(System.getProperty("user.home"), ".carlot");
        }
        return base.resolve(CARLOT_INVENTORY_FILENAME);
    }

    private File inventoryFile() {
        Path path = Path.of(CARLOT_INVENTORY_LOCATION);
        Path parent = path.getParent();
        if (parent != null) {
            try {
                Files.createDirectories(parent);
            } catch (Exception ignored) {
                // save/load will surface the real I/O error
            }
        }
        return path.toFile();
    }

    /**
     * Add a new car to this CarLot's inventory.
     *
     * @param id         Identifier of the new Car
     * @param mileage    Current mileage of the new Car
     * @param mpg        Car's miles per gallon
     * @param cost       Amount paid to acquire this Car
     * @param salesPrice Asking price for the Car
     */
    public void addCar(String id, int mileage, int mpg, double cost, double salesPrice) {
        inventory.add(new Car(id, mileage, mpg, cost, salesPrice));
    }

    public ArrayList<Car> getInventory() {
        return inventory;
    }

    public void setInventory(ArrayList<Car> inventory) {
        this.inventory = inventory;
    }

    /**
     * Return the Car in inventory that matches the identifier.
     *
     * @param identifier Car identifier to search for
     * @return Car with matching identifier or null if no match
     */
    public Car findCarByIdentifier(String identifier) {
        for (Car car : inventory) {
            if (car.getId().equals(identifier)) {
                return car;
            }
        }
        return null;
    }

    /**
     * Sell the car identified by identifier for the priceSold.
     *
     * @param identifier Identifier for the Car being sold
     * @param priceSold  Price the Car is selling for
     * @throws IllegalArgumentException if there is no car with identifier in the inventory
     */
    public void sellCar(String identifier, double priceSold) throws IllegalArgumentException {
        Car car = findCarByIdentifier(identifier);
        if (car == null) {
            throw new IllegalArgumentException("No car found with identifier: " + identifier);
        }
        if (car.isSold()) {
            throw new IllegalArgumentException("Car already sold: " + identifier);
        }
        car.sellCar(priceSold);
    }

    /**
     * Return a new list containing all of the Cars in inventory, in the order
     * that they were added to the inventory.
     *
     * @return new list of Cars
     */
    public ArrayList<Car> getCarsInOrderOfEntry() {
        return new ArrayList<>(inventory);
    }

    /**
     * Return a new list containing all of the Cars in inventory, sorted by
     * highest to lowest MPG.
     *
     * @return new, sorted list
     */
    public ArrayList<Car> getCardsSortedByMPG() {
        ArrayList<Car> sorted = new ArrayList<>(inventory);
        sorted.sort(Comparator.comparingInt(Car::getMpg).reversed());
        return sorted;
    }

    /**
     * Return the single Car in the inventory that has the highest MPG.
     *
     * @return Car with highest MPG, or null if inventory is empty
     */
    public Car getCarWithBestMPG() {
        if (inventory.isEmpty()) {
            return null;
        }
        Car best = inventory.get(0);
        for (Car car : inventory) {
            if (car.compareMPG(best) > 0) {
                best = car;
            }
        }
        return best;
    }

    /**
     * Return the single Car in the inventory that has the highest mileage.
     *
     * @return Car with the highest mileage, or null if inventory is empty
     */
    public Car getCarWithHighestMileage() {
        if (inventory.isEmpty()) {
            return null;
        }
        Car highest = inventory.get(0);
        for (Car car : inventory) {
            if (car.compareMileage(highest) > 0) {
                highest = car;
            }
        }
        return highest;
    }

    /**
     * Return the average MPG of all cars in inventory.
     *
     * @return average MPG of all cars, or 0 if empty
     */
    public double getAverageMpg() {
        if (inventory.isEmpty()) {
            return 0.0;
        }
        double total = 0.0;
        for (Car car : inventory) {
            total += car.getMpg();
        }
        return total / inventory.size();
    }

    /**
     * Return the total profit of all sold cars in inventory.
     *
     * @return total profit of all sold cars
     */
    public double getTotalProfit() {
        double total = 0.0;
        for (Car car : inventory) {
            if (car.isSold()) {
                total += car.getProfit();
            }
        }
        return total;
    }

    /**
     * Save the cars in this carlot to the file specified in CARLOT_INVENTORY_LOCATION.
     *
     * @throws FileNotFoundException if the file cannot be opened
     */
    public void saveToDisk() throws FileNotFoundException {
        try (PrintWriter writer = new PrintWriter(inventoryFile())) {
            for (Car car : inventory) {
                writer.printf("%s|%d|%d|%.2f|%.2f|%s|%.2f|%.2f%n",
                        escape(car.getId()),
                        car.getMileage(),
                        car.getMpg(),
                        car.getCost(),
                        car.getSalesPrice(),
                        car.isSold(),
                        car.getPriceSold(),
                        car.getProfit());
            }
        }
    }

    /**
     * Load the cars stored in CARLOT_INVENTORY_LOCATION.
     *
     * @throws FileNotFoundException if the file cannot be opened
     */
    public void loadFromDisk() throws FileNotFoundException {
        File file = inventoryFile();
        if (!file.exists()) {
            throw new FileNotFoundException(CARLOT_INVENTORY_LOCATION);
        }
        ArrayList<Car> loaded = new ArrayList<>();
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                String[] parts = line.split("\\|", -1);
                if (parts.length < 8) {
                    continue;
                }
                Car car = new Car(
                        unescape(parts[0]),
                        Integer.parseInt(parts[1]),
                        Integer.parseInt(parts[2]),
                        Double.parseDouble(parts[3]),
                        Double.parseDouble(parts[4]));
                car.setSold(Boolean.parseBoolean(parts[5]));
                car.setPriceSold(Double.parseDouble(parts[6]));
                car.setProfit(Double.parseDouble(parts[7]));
                loaded.add(car);
            }
        }
        this.inventory = loaded;
    }

    /** Load if the inventory file exists; otherwise leave inventory empty. */
    public boolean loadFromDiskIfPresent() {
        File file = new File(CARLOT_INVENTORY_LOCATION);
        if (!file.exists()) {
            return false;
        }
        try {
            loadFromDisk();
            return true;
        } catch (FileNotFoundException e) {
            return false;
        }
    }

    public int getAvailableCount() {
        int count = 0;
        for (Car car : inventory) {
            if (!car.isSold()) {
                count++;
            }
        }
        return count;
    }

    private static String escape(String value) {
        return value.replace("|", "%7C");
    }

    private static String unescape(String value) {
        return value.replace("%7C", "|");
    }
}
