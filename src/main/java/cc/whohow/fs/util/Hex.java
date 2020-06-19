package cc.whohow.fs.util;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;

public class Hex {
    public static String encode(byte[] data) {
        return encode(data, 0, data.length);
    }

    public static String encode(byte[] data, int offset, int length) {
        char[] hex = new char[length << 1];
        for (int i = offset, j = 0; i < length; i++) {
            byte b = data[i];
            hex[j++] = Character.forDigit((0xF0 & b) >>> 4, 16);
            hex[j++] = Character.forDigit(0x0F & b, 16);
        }
        return new String(hex);
    }

    public static String encode(ByteBuffer data) {
        if (data.hasArray()) {
            String hex = encode(data.array(), data.arrayOffset() + data.position(), data.remaining());
            data.position(data.limit());
            return hex;
        } else {
            byte[] buffer = new byte[data.remaining()];
            data.get(buffer);
            return encode(buffer);
        }
    }

    public static byte[] decode(char[] hex) {
        return decode(CharBuffer.wrap(hex));
    }

    public static byte[] decode(CharSequence hex) {
        if (hex.length() % 2 != 0) {
            throw new IllegalArgumentException(hex.toString());
        }
        byte[] data = new byte[hex.length() >> 1];
        for (int i = 0, j = 0; i < data.length; i++) {
            int h = Character.digit(hex.charAt(j++), 16) << 4;
            int l = Character.digit(hex.charAt(j++), 16);
            data[i] = (byte) ((h | l) & 0xFF);
        }
        return data;
    }
}
