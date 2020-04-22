package banking;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class UserDatabaseInMemory implements UserDatabase {
    //simple in-memory repository of user credentials
    private Map<Integer, UserAccount> storage = new HashMap<>();

    @Override
    public Optional<UserAccount> findByExample(UserAccount example) {
        Objects.requireNonNull(example);
        UserAccount value = storage.get(example.hashCode());

        return Optional.ofNullable(value);
    }

    @Override
    public Optional<UserAccount> findById(int id) {
        return storage.values().stream().filter(account -> account.id == id).findFirst();
    }

    /**
     *
     * @param account not null
     * @return previously mapped value (if any) or null
     */
    @Override
    public UserAccount save(UserAccount account) {
        Objects.requireNonNull(account);
        return storage.put(account.hashCode(), account);
   }

    @Override
    public void delete(UserAccount account) {
        Integer key = account.hashCode();
        storage.remove(key);
    }

    @Override
    public void createSchemaIfNeeded() {
        //do nothing
    }

    @Override
    public Optional<UserAccount> findByCardNumber(String destCardNumber) {
        return storage.values().stream().filter(account -> account.cardNumber.equals(destCardNumber)).findFirst();
    }

    @Override
    public void transferMoney(UserAccount srcAccount, UserAccount destAccount, int amount) {

    }


}
