package models.Bid;

import models.Price.Price;
import models.User.User;

import java.util.UUID;

public class Bid {
    private final UUID id;
    private final Price price;
    private final User user;

    public Bid(UUID id, Price price, User user) {
        this.id = id;
        this.price = price;
        this.user = user;
    }

    public Price getPrice() {
        return price;
    }

    public User getUser() {
        return user;
    }

    public UUID getId() {
        return id;
    }

    @Override
    public String toString() {
        return String.format("Bid: %s by %s", price, user);
    }
}
