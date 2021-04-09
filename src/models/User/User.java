package models.User;

import models.Auction.Auction;
import models.Bid.Bid;
import models.CreditCard.Card;

public class User {
    private final String name;
    private final Card card;

    public User(String name, Card card) {
        this.name = name;
        this.card = card;
    }

    public Bid placeBid(Auction auction, double value) {
        return auction.placeBid(this, value);
    }

    public void cancelBid(Auction auction,Bid bid) {
        auction.cancelBid(this, bid);
    }

    public Bid modifyBid(Auction auction,Bid bid, double newValue) {
        return auction.modifyBid(this, bid, newValue);
    }

    @Override
    public String toString() {
        return String.format("%s", name);
    }

    public Card getCard() {
        return card;
    }
}
