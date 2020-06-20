package cc.whohow;

import java.nio.ByteBuffer;
import java.util.concurrent.ThreadLocalRandom;

public class RandomContent {
    public static char randomChar() {
        return Character.forDigit(ThreadLocalRandom.current().nextInt(36), 36);
    }

    public static byte[] randomBytes(int length) {
        byte[] buffer = new byte[length];
        for (int i = 0; i < length; i++) {
            buffer[i] = (byte) randomChar();
        }
        return buffer;
    }

    public static ByteBuffer randomByteBuffer(int length) {
        return ByteBuffer.wrap(randomBytes(length));
    }

    public static String randomString(int length) {
        char[] buffer = new char[length];
        for (int i = 0; i < length; i++) {
            buffer[i] = randomChar();
        }
        return new String(buffer);
    }

    public static byte[] randomBytes(int minLength, int maxLength) {
        return randomBytes(ThreadLocalRandom.current().nextInt(minLength, maxLength));
    }

    public static ByteBuffer randomByteBuffer(int minLength, int maxLength) {
        return randomByteBuffer(ThreadLocalRandom.current().nextInt(minLength, maxLength));
    }

    public static String randomString(int minLength, int maxLength) {
        return randomString(ThreadLocalRandom.current().nextInt(minLength, maxLength));
    }
}
