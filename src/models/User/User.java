package models.User;

import models.Auction.Auction;
import models.Bid.Bid;
import models.CreditCard.Card;

import java.util.UUID;

public class User {
    private final UUID id;
    private final String name;
    private final Card card;

    public User(UUID id, String name, Card card) {
        this.id = id;
        this.name = name;
        this.card = card;
    }

    public Bid placeBid(Auction auction, double value) {
        return auction.placeBid(this, value);
    }

    public void cancelBid(Auction auction, Bid bid) {
        auction.cancelBid(this, bid);
    }

    public Bid modifyBid(Auction auction, Bid bid, double newValue) {
        return auction.modifyBid(this, bid, newValue);
    }

    @Override
    public String toString() {
        return String.format("%s", name);
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Card getCard() {
        return card;
    }
}
