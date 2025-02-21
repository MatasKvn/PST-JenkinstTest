import java.util.Random;

public class Randomizer {
    private final static char[] BASE36_CHARS = "0123456789abcdefghijklmnopqrstuvwxyz".toCharArray();
    private final static char[] NUMBERS = "0123456789".toCharArray();
    private final static int CUID_LENGTH = 25;
    private final static int NUM_LENGTH = 11;

    public static String numString() {
        Random random = new Random();
        char[] num = new char[NUM_LENGTH];
        for (int i = 0; i < NUM_LENGTH; i++) {
            num[i] = NUMBERS[random.nextInt(NUMBERS.length)];
        }
        return new String(num);
    }

    public static String cuid() {
        Random random = new Random();
        char[] cuid = new char[CUID_LENGTH];
        for (int i = 0; i < CUID_LENGTH; i++) {
            cuid[i] = BASE36_CHARS[random.nextInt(BASE36_CHARS.length)];
        }
        return new String(cuid);
    }

    public static char gender() {
        return Math.random() < 0.5 ? 'M' : 'F';
    }

    public static String email() {
        String user = cuid();
        String domain = cuid() + ".com";

        return user + "@" + domain;
    }

    public static String name() {
        return cuid();
    }

    public static String password() {
        return cuid();
    }

    public static String phone() {
        return numString();
    }

    public static String zipCode() {
        return numString();
    }

}
