package hr.fer.oprpp1.hw05;

import java.math.BigInteger;

public class Util {

    public static byte[] hextobyte(String keyText) {
        if (keyText.length() == 0) return new byte[0];
        if (keyText.length() % 2 == 1) throw new IllegalArgumentException("String is not odd!");

        int len = keyText.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(keyText.charAt(i), 16) << 4)
                    + Character.digit(keyText.charAt(i+1), 16));
        }
        return data;
    }

    public static String bytetohex(byte[] byteArray) {
        StringBuilder sb = new StringBuilder(byteArray.length * 2);
        for(byte b : byteArray)
            sb.append(String.format("%02x", b));
        return sb.toString();
    }

}
