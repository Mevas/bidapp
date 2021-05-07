package models.Auction;

import models.Bid.Bid;
import models.Organizer.Organizer;
import models.Price.Price;
import models.User.User;
import services.AuditService;
import services.CsvReaderService;
import services.Service;

import java.io.FileNotFoundException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public abstract class Auction {
    protected final UUID id;
    protected final LocalDateTime start;
    protected LocalDateTime end;
    protected final String location;
    protected final Price initialPrice;
    protected Price currentPrice;
    protected final List<Bid> bids = new ArrayList<>();
    protected boolean canceled = false;
    protected final AuditService auditService;

    protected final Organizer organizer;

    public Auction(UUID id, LocalDateTime start, LocalDateTime end, String location, Price initialPrice, Organizer organizer) {
        this.id = id;
        this.start = start;
        this.end = end;
        this.location = location;
        this.initialPrice = initialPrice;
        this.currentPrice = initialPrice;
        this.organizer = organizer;
        this.auditService = AuditService.getInstance();
    }

    public Bid placeBid(User user, double value) {
        auditService.log("place_bid");
        if (this.isOwner(user)) {
            System.out.println("You are not allowed to place bids on your own auctions.");
            return null;
        }

        if (!this.checkOpeness()) return null;

        if (!this.checkBalance(user, value)) return null;

        for (Bid bid : bids) {
            if (bid.getUser() == user) {
                System.out.println("You are not allowed to place more than one bid on an auction. Modified the current one instead.");
                this.modifyBid(user, bid, value);
                return bid;
            }
        }

        Bid newBid = new Bid(UUID.randomUUID(), new Price(value, this.currentPrice.getCurrency()), user);
        bids.add(newBid);
        this.sortBids();
        this.currentPrice = this.getBestBid().getPrice();
        this.removeBalance(user, value);
        System.out.printf("%s placed a bid of %s%n", user, newBid.getPrice());
        return newBid;
    }

    public void cancelBid(User user, Bid bid) {
        auditService.log("cancel_bid");

        if (bid == null) {
            return;
        }

        if (bid.getUser() != user) {
            System.out.println("You can't cancel other people's bids.");
            return;
        }

        if (!this.checkOpeness()) return;

        bids.remove(bid);
        this.sortBids();
        this.currentPrice = this.getBestBid() != null ? this.getBestBid().getPrice() : initialPrice;
        this.addBalance(user, bid.getPrice().getValue());
    }

    public Bid modifyBid(User user, Bid bid, double newValue) {
        auditService.log("modify_bid");

        if (bid == null) {
            return null;
        }

        if (bid.getUser() != user) {
            System.out.println("You can't modify other people's bids.");
            return bid;
        }

        if (!this.checkOpeness()) return bid;

        this.cancelBid(user, bid);
        // If the user doesn't have enough money after cancelation, restore the old bid
        if (!this.checkBalance(user, newValue)) {
            this.placeBid(user, bid.getPrice().getValue());
        }
        return this.placeBid(bid.getUser(), newValue);
    }

    public boolean isOwner(User user) {
        return user == this.organizer.getUser();
    }

    public boolean checkOpeness() {
        LocalDateTime now = LocalDateTime.now();
        if (!start.isBefore(now)) {
            System.out.println("The auction has not yet started");
            return false;
        }

        if (!now.isBefore(end)) {
            System.out.printf("The auction has ended. Winning bid: %s%n", this.getBestBid());
            return false;
        }

        if (canceled) {
            System.out.println("The auction is canceled");
            return false;
        }

        return true;
    }

    public Bid getBestBid() {
        return bids.size() > 0 ? bids.get(0) : null;
    }

    public List<Bid> getBids() {
        return bids;
    }

    public void setEnd(LocalDateTime end) {
        this.end = end;
    }

    public void cancel() {
        System.out.println("Auction has now been canceled");
        this.canceled = true;
    }

    public abstract void sortBids();

    public abstract boolean checkBalance(User user, double value);

    public abstract void removeBalance(User user, double value);

    public abstract void addBalance(User user, double value);

    public UUID getId() {
        return id;
    }

    public LocalDateTime getStart() {
        return start;
    }

    public LocalDateTime getEnd() {
        return end;
    }

    public String getLocation() {
        return location;
    }

    public Price getInitialPrice() {
        return initialPrice;
    }

    public Price getCurrentPrice() {
        return currentPrice;
    }

    public boolean isCanceled() {
        return canceled;
    }

    public void setCurrentPrice(Price currentPrice) {
        this.currentPrice = currentPrice;
    }

    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }

    public void loadFromCsv() throws FileNotFoundException {
        CsvReaderService reader = CsvReaderService.getInstance();
        String bidsPath = "data\\bids.csv";
        Service service = Service.getInstance();
        List<List<String>> dbData = reader.read(bidsPath);
        for (List<String> data : dbData) {
            if (UUID.fromString(data.get(1)).compareTo(this.id) != 0) {
                continue;
            }

            Optional<User> user = service.getUsers().stream().filter(u -> u.getId().compareTo(UUID.fromString(data.get(1))) == 0).findFirst();

            if (user.isPresent()) {
                bids.add(new Bid(UUID.fromString(data.get(0)), Price.fromString(data.get(3)), user.get()));
            }
        }
    }
}
