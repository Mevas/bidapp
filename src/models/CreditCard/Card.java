package models.CreditCard;

import models.Price.Price;

public class Card {
    private Price balance;

    public Card(Price balance) {
        this.balance = balance;
    }

    public Price getBalance() {
        return balance;
    }

    public void subtractBalance(double value) {
        this.balance = new Price(Math.max(balance.getValue() - value, 0), balance.getCurrency());
    }

    public void addBalance(double value) {
        this.balance = new Price(Math.max(balance.getValue() + value, 0), balance.getCurrency());
    }

    @Override
    public String toString() {
        return String.format("Balance: %s", balance);
    }
}
