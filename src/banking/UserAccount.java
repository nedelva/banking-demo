package banking;

import java.io.PrintStream;
import java.util.Objects;

public class UserAccount {
    /** unique identifier */
    int id;
    String cardNumber;
    String pin;
    int balance;

    UserAccount(String cardNumber, String pin) {
        this.cardNumber = cardNumber;
        this.pin = pin;
    }

    UserAccount(int id, String cardNumber, String pin, int balance) {
        this.id = id;
        this.cardNumber = cardNumber;
        this.pin = pin;
        this.balance = balance;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserAccount that = (UserAccount) o;
        return cardNumber.equals(that.cardNumber) &&
                pin.equals(that.pin);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cardNumber, pin);
    }

    public void toString(PrintStream out) {
        out.println("Your card number:" + cardNumber);
        out.println("Your card PIN:" + pin);
    }

}
