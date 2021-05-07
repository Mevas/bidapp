package models.Auction;

import models.Organizer.Organizer;
import models.Price.Price;
import models.User.User;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Product auctions are the normal type of auctions, where the bidders bid for getting a product from the auctioneer
 * Price is sorted from highest to lowest
 */
public class ProductAuction extends Auction {
    public ProductAuction(UUID id, LocalDateTime start, LocalDateTime end, String location, Price initialPrice, Organizer organizer) {
        super(id, start, end, location, initialPrice, organizer);
    }

    @Override
    public void sortBids() {
        // Sort the bids from highest to lowest
        bids.sort((lhs, rhs) -> -Integer.signum((int) (lhs.getPrice().getValue() - rhs.getPrice().getValue())));
    }

    @Override
    public boolean checkBalance(User user, double value) {
        // Check if the bidder has enough money in his card
        if (user.getCard().getBalance().getValue() < value) {
            System.out.printf("%s doesn't have enough money to place a bid of %s%n", user, new Price(value, user.getCard().getBalance().getCurrency()));
            return false;
        }

        return true;
    }

    @Override
    public void removeBalance(User user, double value) {
        user.getCard().subtractBalance(value);
    }

    @Override
    public void addBalance(User user, double value) {
        user.getCard().addBalance(value);
    }

    @Override
    public String toString() {
        return String.format("Product auction - best offer: %s - {start: %s | end: %s | bids: %s}", currentPrice, start, end, bids);
    }
}
