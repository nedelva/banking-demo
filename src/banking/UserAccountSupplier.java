package banking;

import java.util.function.Supplier;

public class UserAccountSupplier implements Supplier<UserAccount> {

    private final PinGenerator pinGenerator = new PinGenerator();
    private final CardNumberGenerator cardNumberGenerator = new CardNumberGenerator();

    @Override
    public UserAccount get() {
        String pin = pinGenerator.get();

        String cardNumber = cardNumberGenerator.get();
        UserAccount userAccount = new UserAccount(cardNumber, pin);
        return userAccount;
    }
}
