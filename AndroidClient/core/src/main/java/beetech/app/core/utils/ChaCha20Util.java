package beetech.app.core.utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

public class ChaCha20Util {
    private static final byte[] KEY = "this_is_a_32_byte_secret_key____".getBytes(StandardCharsets.UTF_8);
    private static final byte[] NONCE = "123456789012".getBytes(StandardCharsets.UTF_8);

    public static String encryptExt(long number, String typeHex) {
        if (typeHex == null || typeHex.length() != 4) {
            throw new IllegalArgumentException("Type must be 4 hex digits");
        }

        // Layout: [8 bytes number][2 bytes salt] = 10 bytes
        byte[] input = new byte[10];
        ByteBuffer.wrap(input).order(ByteOrder.LITTLE_ENDIAN).putLong(number);

        // Salt derived deterministically from the number itself
        short salt = (short) (number & 0xFFFF);
        ByteBuffer.wrap(input, 8, 2).order(ByteOrder.LITTLE_ENDIAN).putShort(salt);

        byte[] output = new byte[input.length];
        ChaCha20 chacha = new ChaCha20(KEY, NONCE, 0);
        chacha.encrypt(output, input, input.length);

        // 10 bytes -> 20 hex chars, prefix with type
        return typeHex.toUpperCase() + bytesToHex(output);
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }
}
