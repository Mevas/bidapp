package models.Organizer;

import models.Auction.Auction;
import models.Auction.ProductAuction;
import models.Auction.ServiceAuction;
import models.Price.Price;
import models.User.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Organizer {
    private final User user;
    private final List<ProductAuction> productAuctions = new ArrayList<>();
    private final List<ServiceAuction> serviceAuctions = new ArrayList<>();

    public Organizer(User user) {
        this.user = user;
    }

    public ProductAuction createProductAuction(LocalDateTime start, LocalDateTime end, String location, Price initialPrice) {
        ProductAuction auction = new ProductAuction(start, end, location, initialPrice, this);
        productAuctions.add(auction);
        return auction;
    }

    public ServiceAuction createServiceAuction(LocalDateTime start, LocalDateTime end, String location, Price initialPrice) {
        ServiceAuction auction = new ServiceAuction(start, end, location, initialPrice, this);
        serviceAuctions.add(auction);
        return auction;
    }

    public void endAuction(Auction auction) {
        if (!auction.isOwner(user)) {
            System.out.printf("Organizer %s is not allowed to end %s%n", user, auction);
            return;
        }

        if(!auction.checkOpeness()) return;

        System.out.printf("Auction has been prematurely ended. Winning bid is: %s%n", auction.getBestBid());
        auction.setEnd(LocalDateTime.now());
    }

    public void cancelAuction(Auction auction) {
        if (!auction.isOwner(user)) {
            System.out.printf("Organizer %s is not allowed to cancel %s%n", user, auction);
            return;
        }

        if(!auction.checkOpeness()) return;

        auction.cancel();
    }

    public List<ProductAuction> getProductAuctions() {
        return productAuctions;
    }

    public List<ServiceAuction> getServiceAuctions() {
        return serviceAuctions;
    }

    public User getUser() {
        return user;
    }

    @Override
    public String toString() {
        return String.format("Organizer %s:\nProduct auctions: %s\nService auctions: %s", this.user, this.productAuctions, this.serviceAuctions);
    }
}
