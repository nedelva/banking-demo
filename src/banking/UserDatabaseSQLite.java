package banking;

import java.sql.*;
import java.util.Objects;
import java.util.Optional;

public class UserDatabaseSQLite implements UserDatabase {
    private Connection conn = null;

    public UserDatabaseSQLite(String databaseUrl) throws SQLException {
        conn = DriverManager.getConnection(databaseUrl);
    }

    /**
     * Attempts to find one UserAccount by cardNumber and pin.
     *
     * @param example not null
     * @return an Optional with the ONE record matching or Optional.empty() if not found
     * @throws RuntimeException if a DB error occurs
     */
    @Override
    public Optional<UserAccount> findByExample(UserAccount example) {
        Objects.requireNonNull(example);
        String cardNumber = example.cardNumber;
        String pin = example.pin;
        try (Statement statement = conn.createStatement()) {
            String sqlToExecute = String.format("select id, number, pin, balance from card where card.number = %s and card.pin = '%s'", cardNumber, pin);
            statement.execute(sqlToExecute);
            ResultSet resultSet = statement.getResultSet();
            if (resultSet.next()) {
                int id = resultSet.getInt("id");
                int balance = resultSet.getInt("balance");
                UserAccount uAcc = new UserAccount(id, cardNumber, pin, balance);
                return Optional.of(uAcc);
            }
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
        return Optional.empty();
    }

    @Override
    public Optional<UserAccount> findById(int id) {
        try (Statement statement = conn.createStatement()) {
            String sqlToExecute = String.format("select id, number, pin, balance from card where card.id = %d",
                    id);
            statement.execute(sqlToExecute);
            ResultSet resultSet = statement.getResultSet();
            if (resultSet.next()) {
                id = resultSet.getInt("id");
                int balance = resultSet.getInt("balance");
                String cardNumber = resultSet.getString("number");
                String pin = resultSet.getString("pin");
                UserAccount uAcc = new UserAccount(id, cardNumber, pin, balance);
                return Optional.of(uAcc);
            }
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }

        return Optional.empty();
    }

    @Override
    public Optional<UserAccount> findByCardNumber(String destCardNumber) {
        Objects.requireNonNull(destCardNumber);
        try (Statement statement = conn.createStatement()) {
            String sqlToExecute = String.format("select id, number, pin, balance from card where card.number = %s",
                    destCardNumber);
            statement.execute(sqlToExecute);
            ResultSet resultSet = statement.getResultSet();
            if (resultSet.next()) {
                int id = resultSet.getInt("id");
                int balance = resultSet.getInt("balance");
                String cardNumber = resultSet.getString("number");
                String pin = resultSet.getString("pin");
                UserAccount uAcc = new UserAccount(id, cardNumber, pin, balance);
                return Optional.of(uAcc);
            }
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
        return Optional.empty();
    }

    @Override
    public void transferMoney(UserAccount srcAccount, UserAccount destAccount, int amount) {
        if(srcAccount.balance - amount < 0) {
            throw new InsufficientFundsException(String.format("%d", srcAccount.balance));
        }
        boolean prevAutoCommitMode = false;
        try {
            prevAutoCommitMode = conn.getAutoCommit();
            conn.setAutoCommit(false);
        } catch (SQLException sqlException) {
            throw new RuntimeException("Cannot set auto-commit mode to 'false'", sqlException);
        }

        try {
            srcAccount.balance -= amount;
            destAccount.balance += amount;
            doUpdate(srcAccount);
            doUpdate(destAccount);
            conn.commit();
        } catch (SQLException sqlException) {
            try {
                conn.rollback();
            } catch (SQLException ignore) {
            }
            throw new RuntimeException("Transaction failed.", sqlException);
        }
        try {
            conn.setAutoCommit(prevAutoCommitMode);
        } catch (SQLException sqlException) {
            throw new RuntimeException(String.format("Cannot set auto-commit mode to %b", prevAutoCommitMode), sqlException);
        }
    }

    @Override
    public void delete(UserAccount account) {
        Objects.requireNonNull(account);
        try {
            if(recordExists(account)) {
                doDelete(account);
            }
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
    }

    @Override
    public UserAccount save(UserAccount account) {
        Objects.requireNonNull(account);
        try {
            if (recordExists(account)) {
                doUpdate(account);
            } else {
                doInsert(account);
            }

        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }

        return account;
    }

    @Override
    public void createSchemaIfNeeded() {
        try (ResultSet card = conn.getMetaData().getTables(null, null, "card", null)) {
            if (!card.next()) {
                //not found
                System.out.print("Creating table 'card'..");
                Statement statement = conn.createStatement();
                boolean result = statement.execute("CREATE TABLE card(id INTEGER PRIMARY KEY, number TEXT, pin TEXT, balance INTEGER DEFAULT 0);");
                if (statement.getUpdateCount() > 0) {
                    System.out.println("Table created.");
                }
            }
        } catch (SQLException sqlex) {
            sqlex.printStackTrace();
        }
    }


    private void doInsert(UserAccount account) throws SQLException {
        try (Statement statement = conn.createStatement()) {
            int identifier = account.id;
            String sqlInsertTemplate;
            String sqlToExecute;
            if (identifier == 0) {
                //we rely on the fact that 'id' column was created as PRIMARY KEY and became an alias of rowid column
                sqlInsertTemplate = "insert into card(id, number,pin,balance) values(null, '%s', '%s', %d)";
                sqlToExecute = String.format(sqlInsertTemplate,
                        account.cardNumber, account.pin, account.balance);
            } else {
                sqlInsertTemplate = "insert into card(id,number,pin,balance) values(id=%d, number='%s', pin='%s', balance=%d)";
                sqlToExecute = String.format(sqlInsertTemplate,
                        identifier, account.cardNumber, account.pin, account.balance);
            }
            statement.execute(sqlToExecute);
        }

    }

    private void doUpdate(UserAccount account) throws SQLException {
        try (Statement statement = conn.createStatement()) {
            String sqlToExecute = String.format("update card set number='%s', pin='%s', balance=%d where card.id = %d",
                    account.cardNumber, account.pin, account.balance, account.id);
            statement.execute(sqlToExecute);
        }

    }

    private void doDelete(UserAccount account) throws SQLException {
        try (Statement statement = conn.createStatement()) {
            String sqlToExecute = String.format("delete from card where card.id = %d",
                    account.id);
            statement.execute(sqlToExecute);
        }

    }

    private boolean recordExists(UserAccount account) throws SQLException {
        try (Statement statement = conn.createStatement()) {
            String sqlToExecute = String.format("select id, number, pin, balance from card where card.id = '%d'",
                    account.id);
            statement.execute(sqlToExecute);
            ResultSet resultSet = statement.getResultSet();
            if (resultSet.next()) {
                return true;
            }
        }
        return false;
    }
}
