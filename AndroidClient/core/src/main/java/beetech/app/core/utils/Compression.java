package beetech.app.core.utils;

import java.io.ByteArrayOutputStream;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class Compression {
    public static byte[] compress(byte[] input) {
        Deflater deflater = new Deflater();
        deflater.setLevel(9);
        deflater.setInput(input);
        deflater.finish();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];

        while (!deflater.finished()) {
            int compressedSize = deflater.deflate(buffer);
            outputStream.write(buffer, 0, compressedSize);
        }

        return outputStream.toByteArray();
    }

    public static byte[] decompress(byte[] input) throws DataFormatException {
        Inflater inflater = new Inflater();
        inflater.setInput(input);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];

        while (!inflater.finished()) {
            int decompressedSize = inflater.inflate(buffer);
            outputStream.write(buffer, 0, decompressedSize);
        }

        return outputStream.toByteArray();
    }
}
