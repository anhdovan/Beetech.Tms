package beetech.app.core.utils;

/**
 * A simple implementation of the ChaCha20 stream cipher.
 * Ported to Java for cross-platform compatibility with the backend.
 */
public class ChaCha20 {
    private final int[] state = new int[16];

    public ChaCha20(byte[] key, byte[] nonce, int counter) {
        if (key.length != 32 || nonce.length != 12) {
            throw new IllegalArgumentException("Key must be 32 bytes, nonce must be 12 bytes");
        }

        // Constants "expand 32-byte k"
        state[0] = 0x61707865;
        state[1] = 0x3320646e;
        state[2] = 0x79622d32;
        state[3] = 0x6b206574;

        // Key
        for (int i = 0; i < 8; i++) {
            state[4 + i] = bytesToInt(key, i * 4);
        }

        // Counter and Nonce
        state[12] = counter;
        state[13] = bytesToInt(nonce, 0);
        state[14] = bytesToInt(nonce, 4);
        state[15] = bytesToInt(nonce, 8);
    }

    public void encrypt(byte[] output, byte[] input, int len) {
        int[] x = new int[16];
        byte[] block = new byte[64];
        int pos = 0;

        while (pos < len) {
            System.arraycopy(state, 0, x, 0, 16);
            for (int i = 0; i < 10; i++) {
                quarterRound(x, 0, 4, 8, 12);
                quarterRound(x, 1, 5, 9, 13);
                quarterRound(x, 2, 6, 10, 14);
                quarterRound(x, 3, 7, 11, 15);
                quarterRound(x, 0, 5, 10, 15);
                quarterRound(x, 1, 6, 11, 12);
                quarterRound(x, 2, 7, 8, 13);
                quarterRound(x, 3, 4, 9, 14);
            }

            for (int i = 0; i < 16; i++) {
                intToBytes(x[i] + state[i], block, i * 4);
            }

            int count = Math.min(64, len - pos);
            for (int i = 0; i < count; i++) {
                output[pos + i] = (byte) (input[pos + i] ^ block[i]);
            }

            pos += count;
            state[12]++; // Increment counter
            if (state[12] == 0)
                state[13]++;
        }
    }

    private void quarterRound(int[] x, int a, int b, int c, int d) {
        x[a] += x[b];
        x[d] ^= x[a];
        x[d] = rotateLeft(x[d], 16);
        x[c] += x[d];
        x[b] ^= x[c];
        x[b] = rotateLeft(x[b], 12);
        x[a] += x[b];
        x[d] ^= x[a];
        x[d] = rotateLeft(x[d], 8);
        x[c] += x[d];
        x[b] ^= x[c];
        x[b] = rotateLeft(x[b], 7);
    }

    private int rotateLeft(int x, int y) {
        return (x << y) | (x >>> (32 - y));
    }

    private int bytesToInt(byte[] b, int off) {
        return (b[off] & 0xFF) | ((b[off + 1] & 0xFF) << 8) | ((b[off + 2] & 0xFF) << 16) | (b[off + 3] << 24);
    }

    private void intToBytes(int v, byte[] b, int off) {
        b[off] = (byte) v;
        b[off + 1] = (byte) (v >>> 8);
        b[off + 2] = (byte) (v >>> 16);
        b[off + 3] = (byte) (v >>> 24);
    }
}
