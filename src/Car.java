/**
 * A Car represents a single vehicle for sale by a CarLot.
 */
public class Car {

    private String id;
    private int mileage;
    private int mpg;
    private double cost;
    private double salesPrice;
    private boolean sold;
    private double priceSold;
    private double profit;

    /** Default constructor. Sets all fields of the car to default values. */
    public Car() {
        this.id = "";
        this.mileage = 0;
        this.mpg = 0;
        this.cost = 0.0;
        this.salesPrice = 0.0;
        this.sold = false;
        this.priceSold = 0.0;
        this.profit = 0.0;
    }

    /**
     * Constructor used when creating a new Car that will be added to a CarLot.
     *
     * @param id         String representation to identify this car. Should not contain spaces
     * @param mileage    Mileage of the vehicle when added to inventory
     * @param mpg        Miles Per Gallon
     * @param cost       Price paid to acquire this car
     * @param salesPrice Price that the CarLot wants to sell this car for
     */
    public Car(String id, int mileage, int mpg, double cost, double salesPrice) {
        this.id = id;
        this.mileage = mileage;
        this.mpg = mpg;
        this.cost = cost;
        this.salesPrice = salesPrice;
        this.sold = false;
        this.priceSold = 0.0;
        this.profit = 0.0;
    }

    /**
     * Mark this Car as sold and calculate the profit.
     * Profit equals selling price minus the cost the car lot paid for the vehicle.
     *
     * @param priceSold The price that the car sold for
     */
    public void sellCar(double priceSold) {
        this.sold = true;
        this.priceSold = priceSold;
        this.profit = priceSold - this.cost;
    }

    /**
     * Compare the MPG of this car compared to otherCar.
     *
     * @param otherCar The Car compared to this Car
     * @return negative if lower, 0 if equal, positive if higher
     */
    public int compareMPG(Car otherCar) {
        return Integer.compare(this.mpg, otherCar.mpg);
    }

    /**
     * Compare the mileage of this car compared to otherCar.
     *
     * @param otherCar The Car compared to this Car
     * @return negative if lower, 0 if equal, positive if higher
     */
    public int compareMileage(Car otherCar) {
        return Integer.compare(this.mileage, otherCar.mileage);
    }

    /**
     * Compare the sales price of this car compared to otherCar.
     *
     * @param otherCar The Car compared to this Car
     * @return negative if lower, 0 if equal, positive if higher
     */
    public double compareSalesprice(Car otherCar) {
        return this.salesPrice - otherCar.salesPrice;
    }

    @Override
    public String toString() {
        return String.format(
                "Car[id=%s, mileage=%d, mpg=%d, cost=%.2f, salesPrice=%.2f, sold=%s, priceSold=%.2f, profit=%.2f]",
                id, mileage, mpg, cost, salesPrice, sold, priceSold, profit);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getMileage() {
        return mileage;
    }

    public void setMileage(int mileage) {
        this.mileage = mileage;
    }

    public int getMpg() {
        return mpg;
    }

    public void setMpg(int mpg) {
        this.mpg = mpg;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public double getSalesPrice() {
        return salesPrice;
    }

    public void setSalesPrice(double salesPrice) {
        this.salesPrice = salesPrice;
    }

    public boolean isSold() {
        return sold;
    }

    public void setSold(boolean sold) {
        this.sold = sold;
    }

    public double getPriceSold() {
        return priceSold;
    }

    public void setPriceSold(double priceSold) {
        this.priceSold = priceSold;
    }

    public double getProfit() {
        return profit;
    }

    public void setProfit(double profit) {
        this.profit = profit;
    }
}
