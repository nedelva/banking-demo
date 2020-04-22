package banking;

import java.util.Objects;
import java.util.Random;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CardNumberGenerator implements Supplier<String> {
    /**
     * First 6 digits are IIN - Issuer Identification Number
     * Digits 7-15 are the customer account number (9 digits)
     * Last digit (16) is the checksum digit.
     */
    public static final String CARD_REGEX = "([0-9]{6})([0-9]{9})[0-9]";

    public static final String ISSUER_ID_NUMBER = "400000";
    Random random = new Random();

    public static void main(String[] args) {
        CardNumberGenerator cardNumberGenerator = new CardNumberGenerator();
        for (int i = 0; i < 100; i++) {
            String customerAccountNumber = cardNumberGenerator.generateCustomerAccountNumber();
            System.out.println(String.format("customerAccountNumber(%d) = %s", i, customerAccountNumber));
            System.out.println("card = " + cardNumberGenerator.get());
        }
    }

    @Override
    public String get() {

        String fifteenDigits = ISSUER_ID_NUMBER + generateCustomerAccountNumber();
        return fifteenDigits + computeChecksumDigit(fifteenDigits);
    }

    private static String computeChecksumDigit(String fifteenDigits) {
        //Luhn algorithm
        Objects.requireNonNull(fifteenDigits);
        assert fifteenDigits.length() == 15;
        int[] buffer = new int[15];
        /*
         * 1. Multiply odd digits by 2
         * 2. Substract 9 to all numbers over 9
         * 3. Add all numbers
         * 4. if the total is divisible by 10, then checksum digit is 0; otherwise is the number X such that (total+X) is divisible by 10
         * */
        int total = 0;
        for (int i = 0; i < 15; i++) {
            buffer[i] = Integer.parseInt(String.valueOf(fifteenDigits.charAt(i)));
            //odd digits
            if ((i+1) % 2 != 0) {
                buffer[i] = 2 * buffer[i];
            }
            if (buffer[i] > 9) {
                buffer[i] = buffer[i] - 9;
            }

            total += buffer[i];

        }
        int checksum = 0;
        if (total % 10 != 0) {
            for (int x = 1; x <= 9; x++) {
                if ((total + x) % 10 == 0) {
                    checksum = x;
                    break;
                }
            }
        }
        return String.valueOf(checksum);
    }

    private String generateCustomerAccountNumber() {
        String acctNumber;
        int i = random.nextInt(999999999);
        acctNumber = String.format("%09d", i);
        assert acctNumber.length() == 9;
        return acctNumber;
    }

    public static boolean isValid(String cardNumber) {
        Pattern pattern = Pattern.compile(CARD_REGEX);
        Matcher matcher = pattern.matcher(cardNumber);
        if(!matcher.matches()) {
            return false;
        }
        String first15 = cardNumber.substring(0,15);
        String checksumDigit = computeChecksumDigit(first15);

        return Objects.equals(checksumDigit, cardNumber.substring(15, 16));
    }
}
