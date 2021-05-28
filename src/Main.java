import models.Auction.ProductAuction;
import models.Auction.ServiceAuction;
import models.Bid.Bid;
import models.CreditCard.Card;
import models.Organizer.Organizer;
import models.Price.Price;
import models.User.User;
import services.DatabaseService;
import services.Service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        // We initialize the service that creates cards, users, organizers and prices
        Service service = Service.getInstance();

//        try {
//            service.loadFromCsv();
//        } catch (FileNotFoundException e) {
//            System.out.println("Couldn't load data from csv.");
//        }

//        Reading users, organizers, auctions and bids
        service.loadFromDb();

        // 1. Creating a card for a user
        Card bobCard = service.createCard(100000, "RON");
        Card petricaCard = service.createCard(100000, "RON");
        Card miguelCard = service.createCard(200, "RON");
        Card johnCard = service.createCard(2000000, "RON");

        // 2. Creating a user
        User bob = service.createUser("Bob", bobCard);
        User petrica = service.createUser("Petrica", petricaCard);
        User miguel = service.createUser("Miguel", miguelCard);
        User john = service.createUser("John", johnCard);
        // 3. Creating an organizer from a user
        Organizer petricaOrganizer = service.createOrganizer(petrica);

        Price initialProductPrice = service.createPrice(123, "RON");
        // 4. Creating a product auction
        ProductAuction productAuction = petricaOrganizer.createProductAuction(LocalDateTime.now(), LocalDateTime.now().plusDays(1), "Bucharest", initialProductPrice);
        System.out.println("The empty product auction");
        System.out.println(productAuction);

        // 5. Placing bids on auctions
        // Add 3 bids in random order
        System.out.println("\nTrying to place 5 bids on the product auction");
        bob.placeBid(productAuction, 200);
        bob.placeBid(productAuction, 600);
        bob.placeBid(productAuction, 300);
        miguel.placeBid(productAuction, 250);
        miguel.placeBid(productAuction, 150);
        petrica.placeBid(productAuction, 10000);
        john.placeBid(productAuction, 220);
        System.out.println(productAuction);
        System.out.printf("Bob's balance after placing bids on the product auction: %s", bob.getCard());

        // 6. Cancelling bids on auctions
        // Cancel a product auction bid
        bob.cancelBid(productAuction, productAuction.getBestBid());
        System.out.println("\nCanceling the best bid on the product auction");
        System.out.println(productAuction);

        // 7. Modifying bids on auctions
        // Modify a product auction bid
        bob.modifyBid(productAuction, productAuction.getBids().get(1), 999);
        System.out.println("\nModifying the second to best bid on the product auction to have a value of 999");
        System.out.println(productAuction);

        Price initialServiceOffer = service.createPrice(70000, "RON");
        // 8. Creating a service auction, and doing all the above stuff with it too
        ServiceAuction serviceAuction = petricaOrganizer.createServiceAuction(LocalDateTime.now().minusMinutes(1), LocalDateTime.now().plusDays(1), "Bucharest", initialServiceOffer);
        // Add 3 bids in random order - keep some of their them for later use
        System.out.println("\nPlacing 3 bids on the service auction");
        Bid lowestBid = bob.placeBid(serviceAuction, 10000);
        bob.placeBid(serviceAuction, 40000);
        Bid thirdBobBid = bob.placeBid(serviceAuction, 30000);
        System.out.println(serviceAuction);
        System.out.printf("Bob's balance after placing bids on the service auction: %s", bob.getCard());

        // Cancel a service auction bid
        bob.cancelBid(serviceAuction, lowestBid);
        System.out.println("\nCanceling the best bid on the service auction");
        System.out.println(serviceAuction);

        // Modify a service auction bid
        bob.modifyBid(serviceAuction, thirdBobBid, 25000);
        System.out.println("\nModifying bob's third bid to have a value of 25000");
        System.out.println(serviceAuction);

        System.out.println();
        System.out.println(petricaOrganizer);

        Organizer bobOrganzier = service.createOrganizer(bob);
        ServiceAuction bobServiceAuction = bobOrganzier.createServiceAuction(LocalDateTime.now().minusMinutes(1), LocalDateTime.now().plusDays(1), "Bucharest", initialServiceOffer);

        // 9. Ending an auction
        System.out.println("\nEnding an auction with the wrong user:");
        bobOrganzier.endAuction(serviceAuction);
        System.out.println("\nEnding an auction with the owner:");
        petricaOrganizer.endAuction(serviceAuction);
        TimeUnit.MILLISECONDS.sleep(100);
        System.out.println("\nTrying to end an auction again:");
        petricaOrganizer.endAuction(serviceAuction);
        System.out.println("\nTrying to cancel an auction after it's been ended:");
        petricaOrganizer.cancelAuction(serviceAuction);

        // 10. Cancelling an auction
        System.out.println("\nCanceling an auction:");
        bobOrganzier.cancelAuction(bobServiceAuction);
        System.out.println("\nTrying to cancel an auction after it's been canceled:");
        bobOrganzier.cancelAuction(bobServiceAuction);

//        Creating users, organizers, auctions and bids
        service.saveToDb();

        DatabaseService db = DatabaseService.getInstance();
//        Updaring users
        miguel.setName("Miguel magnificul");
        db.updateUser(miguel);
//        Deleting users
        db.deleteUser(bob);

//        try {
//            service.saveToCsv();
//        } catch (IOException e) {
//            System.out.println("Couldn't save data to csv.");
//        }
    }
}
