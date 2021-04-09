package models.Price;

public class Price {
    private final double value;
    private final String currency;

    public Price(double value, String currency) {
        this.value = value;
        this.currency = currency;
    }

    @Override
    public String toString() {
        return String.format("%s %s", this.value, this.currency);
    }

    public double getValue() {
        return value;
    }

    public String getCurrency() {
        return currency;
    }
}
