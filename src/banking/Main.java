package banking;

import javax.lang.model.SourceVersion;
import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.sql.*;
import java.util.Optional;
import java.util.Scanner;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    static final int EXIT = 0;
    static final int LOGON = 2;
    static final int CREATE_ACCOUNT = 1;

    static UserDatabase userDatabase = null;

    public static void main(String[] args) {
        assert args.length >= 2;

        String fileNameArg = args[0];
        if (!fileNameArg.equals("-fileName")) {
            throw new RuntimeException("Unrecognized argument");
        }
        String fileName = args[1];
        userDatabase = createSQLiteDatabase(fileName);

        displayMainUserChoices();
        int action = readUserMenuChoice();
        while (action != EXIT) {
            switch (action) {
                case CREATE_ACCOUNT:
                    createAndSaveUserAccount();
                    action = readUserMenuChoice();
                    break;
                case LOGON:
                    boolean bExitApp = processCustomer();
                    if (bExitApp) {
                        action = EXIT;
                    } else {
                        displayMainUserChoices();
                        action = readUserMenuChoice();
                    }
                    break;
                default:
                    System.out.println("Invalid choice");
                    displayMainUserChoices();
                    action = readUserMenuChoice();
            }

        }
        displayGoodByeMessage();
    }

    private static UserDatabase createSQLiteDatabase(String fileName) {
        Path path = FileSystems.getDefault().getPath(fileName);
        String url = "jdbc:sqlite:";
        if (path.isAbsolute()) {
            url += path;
        } else {
            File currentDirectory = new File(new File(".").getAbsolutePath());

            Path p = Path.of(currentDirectory.getAbsolutePath(), path.toFile().getPath()).normalize();
            url += p.toString();
        }

        try {
            UserDatabaseSQLite database = new UserDatabaseSQLite(url);
            System.out.println(String.format("Opened database '%s'", url));
            database.createSchemaIfNeeded();
            return database;
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }

        return null;
    }


    private static void createAndSaveUserAccount() {
        UserAccount userAccount = createUserAccount();
        userDatabase.save(userAccount);
        System.out.println("Your card have been created");
        System.out.println("Your card number:");
        System.out.println(userAccount.cardNumber);
        System.out.println("Your card PIN:");
        System.out.println(userAccount.pin);

        displayMainUserChoices();
    }

    private static boolean processCustomer() {
        Optional<UserAccount> userAccountOptional = promptUserForCredentials();
        boolean isLogonSuccessful = userAccountOptional.isPresent();
        if (!isLogonSuccessful) {
            System.out.println("Wrong card number or PIN!");
            return false;
        }
        System.out.println("You have successfully logged in!");
        displayCustomerMenu();
        int choice = readUserMenuChoice();
        UserAccount loggedUserAccount = userAccountOptional.get();

        while (true) {
            if (0 == choice) { // exit
                return true;

            } else if (1 == choice) { //Balance

                System.out.println("Balance: " + loggedUserAccount.balance);
                displayCustomerMenu();
            } else if (2 == choice) { //add income
                System.out.println("Enter the amount to be credited to your account: ");
                int amount = scanner.nextInt();
                loggedUserAccount.balance += amount;
                userDatabase.save(loggedUserAccount);
                displayCustomerMenu();

            } else if (3 == choice) { //do transfer
                System.out.print("Enter the destination account: ");
                String destCardNumber = scanner.next();
                if(destCardNumber.equals(loggedUserAccount.cardNumber)) {
                    System.out.println("You can't transfer money to the same account!");
                    continue;
                }

                if (!CardNumberGenerator.isValid(destCardNumber)) {
                    System.out.println("Probably you make mistake in card number. Please try again!");
                    continue;
                }

                Optional<UserAccount> destinationUserAccountOpt = userDatabase.findByCardNumber(destCardNumber);
                if(destinationUserAccountOpt.isEmpty()) {
                    System.out.println("Such a card does not exist.");
                    continue;
                }
                System.out.print("Enter the amount to be transferred : ");
                int amount = scanner.nextInt();

                if (destinationUserAccountOpt.isPresent()) {
                    UserAccount destinationUserAccount = destinationUserAccountOpt.get();
                    userDatabase.transferMoney(loggedUserAccount, destinationUserAccount, amount);
                }
                displayCustomerMenu();

            } else if (4 == choice) { //close account
                userDatabase.delete(loggedUserAccount);
                System.out.println("Account deleted.");
                return false;

            } else if (5 == choice) { //Log out
                System.out.println("You have successfully logged out!");
                return false;
            }
            choice = readUserMenuChoice();
        }
    }

    private static void displayCustomerMenu() {
        System.out.println("1. Balance");
        System.out.println("2. Add income");
        System.out.println("3. Do transfer");
        System.out.println("4. Close account");
        System.out.println("5. Log out");
        System.out.println("0. Exit");
    }

    private static Optional<UserAccount> promptUserForCredentials() {
        System.out.println("Enter your card number:");
        String sCardNumber = scanner.next();
        System.out.println("Enter your PIN:");
        int pin = scanner.nextInt();
        Optional<UserAccount> account = userDatabase.findByExample(new UserAccount(sCardNumber, String.format("%04d", pin)));

        return account;
    }

    private static UserAccount createUserAccount() {
        return new UserAccountSupplier().get();
    }

    private static void displayGoodByeMessage() {
        System.out.println("Bye!");
    }

    private static int readUserMenuChoice() {
        return scanner.nextInt();
    }

    private static void displayMainUserChoices() {
        System.out.println("1. Create account");
        System.out.println("2. Log into account");
        System.out.println("0. Exit");
    }
}
