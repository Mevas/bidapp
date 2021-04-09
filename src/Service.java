import models.CreditCard.Card;
import models.Organizer.Organizer;
import models.Price.Price;
import models.User.User;

public class Service {
    public Service() {
    }

    public User createUser(String name, Card card) {
        return new User(name, card);
    }

    public Card createCard(double balance, String currency) {
        return new Card(new Price(balance, currency));
    }

    public Organizer createOrganizer(User user) {
        return new Organizer(user);
    }

    public Price createPrice(double value, String currency) {
        return new Price(value, currency);
    }
}
