package services;

import models.Auction.Auction;
import models.Auction.ProductAuction;
import models.Auction.ServiceAuction;
import models.Bid.Bid;
import models.CreditCard.Card;
import models.Organizer.Organizer;
import models.Price.Price;
import models.User.User;

import java.security.PublicKey;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class DatabaseService {
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/bidapp";

    private final Connection connection;
    private static DatabaseService instance;

    private DatabaseService() {
        try {
            Class.forName("org.postgresql.Driver");
            Properties props = new Properties();
            props.setProperty("user", "root");
            props.setProperty("password", "root");
            connection = DriverManager.getConnection(DB_URL, props);
        } catch (SQLException exception) {
            throw new RuntimeException("Error at initializing connection: " + exception);
        } catch (ClassNotFoundException exception) {
            throw new RuntimeException("JDBC driver not found: " + exception);
        }
    }

    public static DatabaseService getInstance() {
        if (instance == null) {
            instance = new DatabaseService();
        }

        return instance;
    }

    public ResultSet getUsers() throws SQLException {
        return connection.prepareStatement("SELECT * FROM \"User\"").executeQuery();
    }

    public List<User> getUsersData() {
        try {
            ResultSet result = getUsers();

            List<User> users = new ArrayList<>();
            while (result.next()) {
                users.add(new User(
                        UUID.fromString(result.getString("id")),
                        result.getString("name"),
                        new Card(Price.fromString(result.getString("balance")))
                ));
            }

            return users;
        } catch (SQLException exception) {
            exception.printStackTrace();
        }

        return null;
    }

    public ResultSet getOrganizers() throws SQLException {
        return connection.prepareStatement("SELECT * FROM \"Organizer\"").executeQuery();
    }

    public ResultSet getBids() throws SQLException {
        return connection.prepareStatement("SELECT * FROM \"Bid\"").executeQuery();
    }

    public ResultSet getAuctions() throws SQLException {
        return connection.prepareStatement("SELECT * FROM \"Auction\"").executeQuery();
    }

    public Optional<User> getUserById(UUID id) {
        List<User> users = getUsersData();
        return users.stream().filter(u -> u.getId().compareTo(id) == 0).findFirst();
    }

    public List<Organizer> getOrganizersData() {
        try {
            ResultSet result = getOrganizers();

            ArrayList<Organizer> organizers = new ArrayList<>();
            while (result.next()) {
                UUID id_organizer = UUID.fromString(result.getString("organizer_id"));
                UUID id_user = UUID.fromString(result.getString("user_id"));
                Optional<User> user = getUserById(id_user);

                if (user.isPresent()) {
                    Organizer organizer = new Organizer(id_organizer, user.get());

                    ResultSet auctionsResult = getAuctions();

                    List<ProductAuction> productAuctions = new ArrayList<>();
                    List<ServiceAuction> serviceAuctions = new ArrayList<>();
                    while (auctionsResult.next()) {
                        String auctionType = auctionsResult.getString("type");

                        Auction auction;

                        if (auctionType.equals("product")) {
                            auction = new ProductAuction(
                                    UUID.fromString(auctionsResult.getString("id")),
                                    LocalDateTime.parse(auctionsResult.getString("start")),
                                    LocalDateTime.parse(auctionsResult.getString("end")),
                                    auctionsResult.getString("location"),
                                    Price.fromString(auctionsResult.getString("initial_price")),
                                    organizer
                            );
                            productAuctions.add((ProductAuction) auction);
                        } else {
                            auction = new ServiceAuction(
                                    UUID.fromString(auctionsResult.getString("id")),
                                    LocalDateTime.parse(auctionsResult.getString("start")),
                                    LocalDateTime.parse(auctionsResult.getString("end")),
                                    auctionsResult.getString("location"),
                                    Price.fromString(auctionsResult.getString("initial_price")),
                                    organizer
                            );
                            serviceAuctions.add((ServiceAuction) auction);
                        }

                        ResultSet bidsResult = getBids();

                        while (bidsResult.next()) {
                            Optional<User> bidUser = getUserById(UUID.fromString(bidsResult.getString("id_user")));

                            List<Bid> bids = new ArrayList<>();
                            if (bidUser.isPresent()) {
                                bids.add(new Bid(
                                        UUID.fromString(bidsResult.getString("id")),
                                        Price.fromString(bidsResult.getString("price")),
                                        bidUser.get()
                                ));
                            }
                            auction.setBids(bids);
                        }
                    }

                    organizer.setProductAuctions(productAuctions);
                    organizer.setServiceAuctions(serviceAuctions);

                    organizers.add(organizer);
                }
            }


            return organizers;
        } catch (SQLException exception) {
            exception.printStackTrace();
        }

        return null;
    }

    public void createUser(User user) {
        try {
            String query = "INSERT INTO \"User\" VALUES (?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(query);

            preparedStatement.setObject(1, user.getId());
            preparedStatement.setString(2, user.getName());
            preparedStatement.setString(3, user.getCard().getBalance().toString());

            preparedStatement.executeUpdate();
            preparedStatement.close();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    public void createOrganizer(Organizer organizer) {
        try {
            String query = "INSERT INTO \"Organizer\" VALUES (?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(query);

            preparedStatement.setObject(1, organizer.getId());
            preparedStatement.setObject(2, organizer.getUser().getId());

            preparedStatement.executeUpdate();
            preparedStatement.close();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    public void createAuction(Auction auction) {
        try {
            String query = "INSERT INTO \"Auction\" VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(query);

            preparedStatement.setObject(1, auction.getId());
            preparedStatement.setObject(2, auction.getOrganizer().getId());
            preparedStatement.setString(3, auction.getStart().toString());
            preparedStatement.setString(4, auction.getEnd().toString());
            preparedStatement.setString(5, auction.getLocation());
            preparedStatement.setString(6, auction.getInitialPrice().toString());
            preparedStatement.setString(7, auction.getCurrentPrice().toString());
            preparedStatement.setBoolean(8, auction.isCanceled());
            preparedStatement.setString(9, auction.getClass() == ServiceAuction.class ? "service" : "product");

            preparedStatement.executeUpdate();
            preparedStatement.close();

            for (Bid bid : auction.getBids()) {
                createBid(auction, bid);
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    public void createBid(Auction auction, Bid bid) {
        try {
            String query = "INSERT INTO \"Bid\" VALUES (?, ?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(query);

            preparedStatement.setObject(1, bid.getId());
            preparedStatement.setObject(2, auction.getId());
            preparedStatement.setObject(3, bid.getUser().getId());
            preparedStatement.setString(4, bid.getPrice().toString());

            preparedStatement.executeUpdate();
            preparedStatement.close();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    public void deleteUser(User user) {
        try {
            String query = "DELETE FROM \"User\" WHERE id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);

            preparedStatement.setObject(1, user.getId());

            preparedStatement.executeUpdate();
            preparedStatement.close();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    public void updateUser(User user) {
        try {
            String query = "UPDATE \"User\" SET name = ?, balance = ? WHERE id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);

            preparedStatement.setString(1, user.getName());
            preparedStatement.setString(2, user.getCard().getBalance().toString());
            preparedStatement.setObject(3, user.getId());

            preparedStatement.executeUpdate();
            preparedStatement.close();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    public void wipe() {
        try {
            connection.prepareStatement("TRUNCATE TABLE \"User\" CASCADE").executeUpdate();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }
}