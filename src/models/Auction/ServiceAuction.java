package models.Auction;

import models.Organizer.Organizer;
import models.Price.Price;
import models.User.User;

import java.time.LocalDateTime;

/**
 * Service auctions are the type of auction where the auctioneer is the one paying, for a service provided by the bidders
 * Price is sorted from lowest to highest
 */
public class ServiceAuction extends Auction {
    public ServiceAuction(LocalDateTime start, LocalDateTime end, String location, Price initialPrice, Organizer organizer) {
        super(start, end, location, initialPrice, organizer);
    }

    @Override
    public void sortBids() {
        // Sort the bids from lowest to highest
        bids.sort((lhs, rhs) -> Integer.signum((int) (lhs.getPrice().getValue() - rhs.getPrice().getValue())));
    }

    @Override
    public boolean checkBalance(User user, double value) {
        // If the suggested offer is outside the organizer's budget, we deny it
        if (value > initialPrice.getValue()) {
            System.out.printf("%s is outside of the organizer's budget%n", new Price(value, initialPrice.getCurrency()));
            return false;
        }

        return true;
    }

    // We don't need these 2 methods for this type of auction
    @Override
    public void removeBalance(User user, double value) {
    }

    @Override
    public void addBalance(User user, double value) {
    }

    @Override
    public String toString() {
        return String.format("Service auction - best offer: %s - {start: %s | end: %s | bids: %s}", currentPrice, start, end, bids);
    }
}
