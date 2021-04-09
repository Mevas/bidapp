package models.Bid;

import models.Price.Price;
import models.User.User;

public class Bid {
    private final Price price;
    private final User user;

    public Bid(Price price, User user) {
        this.price = price;
        this.user = user;
    }

    public Price getPrice() {
        return price;
    }

    public User getUser() {
        return user;
    }

    @Override
    public String toString() {
        return String.format("Bid: %s by %s", price, user);
    }
}
