package models.Price;

import java.util.List;

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

    public static Price fromString(String string) {
        String[] things = string.split(" ");

        return new Price(Float.parseFloat(things[0]), things[1]);
    }

    public double getValue() {
        return value;
    }

    public String getCurrency() {
        return currency;
    }
}
