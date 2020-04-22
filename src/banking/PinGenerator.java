package banking;

import java.util.Random;
import java.util.function.Supplier;

public class PinGenerator implements Supplier<String> {
    public final String PINREGEX = "[0-9]{4}";

    Random random = new Random();
    @Override
    public String get() {
        int randomInt = random.nextInt(10000);
        return String.format("%04d", randomInt);
    }
}
