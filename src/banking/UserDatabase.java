package banking;

import banking.annotations.BankTransaction;

import java.util.Optional;

public interface UserDatabase {
    Optional<UserAccount> findByExample(UserAccount example);
    Optional<UserAccount> findById(int id);

    UserAccount save(UserAccount account);

    void createSchemaIfNeeded();

    Optional<UserAccount> findByCardNumber(String destCardNumber);

    @BankTransaction
    void transferMoney(UserAccount srcAccount, UserAccount destAccount, int amount);

    void delete(UserAccount account);
}
