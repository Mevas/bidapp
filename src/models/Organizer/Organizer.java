package models.Organizer;

import models.Auction.Auction;
import models.Auction.ProductAuction;
import models.Auction.ServiceAuction;
import models.CreditCard.Card;
import models.Price.Price;
import models.User.User;
import services.AuditService;
import services.CsvReaderService;

import java.io.FileNotFoundException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Organizer {
    private final UUID id;
    private final User user;
    private List<ProductAuction> productAuctions = new ArrayList<>();
    private List<ServiceAuction> serviceAuctions = new ArrayList<>();
    private final AuditService auditService;

    public Organizer(UUID id, User user) {
        this.id = id;
        this.auditService = AuditService.getInstance();
        this.user = user;
    }

    public ProductAuction createProductAuction(LocalDateTime start, LocalDateTime end, String location, Price initialPrice) {
        auditService.log("create_product_auction");
        ProductAuction auction = new ProductAuction(UUID.randomUUID(), start, end, location, initialPrice, this);
        productAuctions.add(auction);
        return auction;
    }

    public ServiceAuction createServiceAuction(LocalDateTime start, LocalDateTime end, String location, Price initialPrice) {
        auditService.log("create_service_auction");
        ServiceAuction auction = new ServiceAuction(UUID.randomUUID(), start, end, location, initialPrice, this);
        serviceAuctions.add(auction);
        return auction;
    }

    public void endAuction(Auction auction) {
        auditService.log("end_auction");
        if (!auction.isOwner(user)) {
            System.out.printf("Organizer %s is not allowed to end %s%n", user, auction);
            return;
        }

        if (!auction.checkOpeness()) return;

        System.out.printf("Auction has been prematurely ended. Winning bid is: %s%n", auction.getBestBid());
        auction.setEnd(LocalDateTime.now());
    }

    public void cancelAuction(Auction auction) {
        auditService.log("cancel_auction");
        if (!auction.isOwner(user)) {
            System.out.printf("Organizer %s is not allowed to cancel %s%n", user, auction);
            return;
        }

        if (!auction.checkOpeness()) return;

        auction.cancel();
    }

    public List<ProductAuction> getProductAuctions() {
        auditService.log("get_product_auctions");
        return productAuctions;
    }

    public List<ServiceAuction> getServiceAuctions() {
        auditService.log("get_service_auctions");
        return serviceAuctions;
    }

    public List<Auction> getAuctions() {
        auditService.log("get_auctions");
        List<Auction> auctions = new ArrayList<>();
        auctions.addAll(productAuctions);
        auctions.addAll(serviceAuctions);
        return auctions;
    }

    public User getUser() {
        return user;
    }

    public UUID getId() {
        return id;
    }

    @Override
    public String toString() {
        return String.format("Organizer %s:\nProduct auctions: %s\nService auctions: %s", this.user, this.productAuctions, this.serviceAuctions);
    }

    public void loadFromCsv() throws FileNotFoundException {
        CsvReaderService reader = CsvReaderService.getInstance();
        String auctionsPath = "data\\auctions.csv";

        List<List<String>> data = reader.read(auctionsPath);
        for (List<String> datum : data) {
            if (UUID.fromString(datum.get(1)).compareTo(this.id) != 0) {
                continue;
            }

            Auction auction;

            if (datum.get(8).equals("product")) {
                auction = new ProductAuction(UUID.fromString(datum.get(0)), LocalDateTime.parse(datum.get(2)), LocalDateTime.parse(datum.get(3)), datum.get(4), Price.fromString(datum.get(5)), this);
                auction.setCurrentPrice(Price.fromString(datum.get(6)));
                auction.setCanceled(datum.get(7).equals("true"));
                auction.loadFromCsv();
                this.productAuctions.add((ProductAuction) auction);
            } else {
                auction = new ServiceAuction(UUID.fromString(datum.get(0)), LocalDateTime.parse(datum.get(2)), LocalDateTime.parse(datum.get(3)), datum.get(4), Price.fromString(datum.get(5)), this);
                auction.setCurrentPrice(Price.fromString(datum.get(6)));
                auction.setCanceled(datum.get(7).equals("true"));
                auction.loadFromCsv();
                this.serviceAuctions.add((ServiceAuction) auction);
            }
        }
    }

    public void setProductAuctions(List<ProductAuction> productAuctions) {
        this.productAuctions = productAuctions;
    }

    public void setServiceAuctions(List<ServiceAuction> serviceAuctions) {
        this.serviceAuctions = serviceAuctions;
    }
}
