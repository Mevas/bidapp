package services;

import models.Auction.Auction;
import models.Auction.ServiceAuction;
import models.Bid.Bid;
import models.CreditCard.Card;
import models.Organizer.Organizer;
import models.Price.Price;
import models.User.User;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class Service {
    private static Service instance = null;
    private final AuditService auditService;
    private List<User> users = new ArrayList<>();
    private List<Organizer> organizers = new ArrayList<>();

    private Service() {
        auditService = AuditService.getInstance();
    }

    public static Service getInstance() {
        if (instance == null) {
            instance = new Service();
        }
        return instance;
    }

    public User createUser(String name, Card card) {
        auditService.log("create_user");
        User user = new User(UUID.randomUUID(), name, card);
        users.add(user);
        return user;
    }

    public Card createCard(double balance, String currency) {
        auditService.log("create_card");
        return new Card(new Price(balance, currency));
    }

    public Organizer createOrganizer(User user) {
        auditService.log("create_organizer");
        Organizer organizer = new Organizer(UUID.randomUUID(), user);
        organizers.add(organizer);
        return organizer;
    }

    public Price createPrice(double value, String currency) {
        auditService.log("create_price");
        return new Price(value, currency);
    }

    public void saveToDb() {
        DatabaseService db = DatabaseService.getInstance();
        db.wipe();

        for (User user : users) {
            db.createUser(user);
        }
        for (Organizer organizer : organizers) {
            db.createOrganizer(organizer);

            for (Auction auction : organizer.getAuctions()) {
                db.createAuction(auction);
            }
        }
    }

    public void loadFromDb() {
        DatabaseService db = DatabaseService.getInstance();

        users = db.getUsersData();
        organizers = db.getOrganizersData();

        System.out.println(organizers);
    }

    public void saveToCsv() throws IOException {
        CsvWriterService writer = CsvWriterService.getInstance();

        String usersPath = "data\\users.csv";
        String organizersPath = "data\\organizers.csv";
        String auctionsPath = "data\\auctions.csv";
        String bidsPath = "data\\bids.csv";

        List<String> data = new ArrayList<>();

        // Users header
        writer.wipe(usersPath);
        data.add("id");
        data.add("name");
        data.add("balance");
        writer.write(usersPath, data, true);

        // Organizers header
        writer.wipe(organizersPath);
        data.clear();
        data.add("id_organizer");
        data.add("id_user");
        writer.write(organizersPath, data, true);

        // Auctions header
        writer.wipe(auctionsPath);
        data.clear();
        data.add("id");
        data.add("id_organizer");
        data.add("start");
        data.add("end");
        data.add("location");
        data.add("initial_price");
        data.add("current_price");
        data.add("is_canceled");
        data.add("type");
        writer.write(auctionsPath, data, true);

        // Bids header
        writer.wipe(bidsPath);
        data.clear();
        data.add("id");
        data.add("id_auction");
        data.add("id_user");
        data.add("price");
        writer.write(bidsPath, data, true);


//        1. Save the users
        for (User user : users) {
            data.clear();

            data.add(user.getId().toString());
            data.add(user.getName());
            data.add(user.getCard().getBalance().toString());

            writer.write(usersPath, data, true);
        }

//        2. Save the organizers
        for (Organizer organizer : organizers) {
            data.clear();

            data.add(organizer.getId().toString());
            data.add(organizer.getUser().getId().toString());

            writer.write(organizersPath, data, true);

//            3. Save the auctions
            data.clear();
            for (Auction auction : organizer.getAuctions()) {
                data.clear();

                data.add(auction.getId().toString());
                data.add(organizer.getId().toString());
                data.add(auction.getStart().toString());
                data.add(auction.getEnd().toString());
                data.add(auction.getLocation());
                data.add(auction.getInitialPrice().toString());
                data.add(auction.getCurrentPrice().toString());
                data.add(auction.isCanceled() ? "true" : "false");
                data.add(auction.getClass() == ServiceAuction.class ? "service" : "product");

                writer.write(auctionsPath, data, true);

//                4. Save the bids
                for (Bid bid : auction.getBids()) {
                    data.clear();

                    data.add(bid.getId().toString());
                    data.add(auction.getId().toString());
                    data.add(bid.getUser().getId().toString());
                    data.add(bid.getPrice().toString());

                    writer.write(bidsPath, data, true);
                }
            }
        }
    }

    public void loadFromCsv() throws FileNotFoundException {
        CsvReaderService reader = CsvReaderService.getInstance();

        String usersPath = "data\\users.csv";
        String organizersPath = "data\\organizers.csv";

        List<List<String>> data = reader.read(usersPath);
        for (List<String> datum : data) {
            User user = new User(UUID.fromString(datum.get(0)), datum.get(1), new Card(Price.fromString(datum.get(2))));
            users.add(user);
        }

        data = reader.read(organizersPath);
        for (List<String> datum : data) {
            Optional<User> user = users.stream().filter(u -> u.getId().compareTo(UUID.fromString(datum.get(1))) == 0).findFirst();

            if (user.isPresent()) {
                Organizer organizer = new Organizer(UUID.fromString(datum.get(0)), user.get());

                organizer.loadFromCsv();

                organizers.add(organizer);
            }
        }
    }

    public List<User> getUsers() {
        return users;
    }

    public List<Organizer> getOrganizers() {
        return organizers;
    }
}
