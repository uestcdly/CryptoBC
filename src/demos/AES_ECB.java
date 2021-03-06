package demos;

import org.bouncycastle.util.encoders.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.SecureRandom;

public class AES_ECB {
    // uncomment as needed
    // public static final String ALGORITHM = "AES/ECB/NoPadding";
    public static final String ALGORITHM = "AES/ECB/PKCS7Padding";

    public static void generate(int size, Writer writer)
            throws Exception {
        SecureRandom random = new SecureRandom();
        byte[] keyBytes;
        if (size <= 128)
            keyBytes = new byte[16];
        else if (size <= 192)
            keyBytes = new byte[24];
        else
            keyBytes = new byte[32];
        random.nextBytes(keyBytes);

        writer.write(Base64.toBase64String(keyBytes));
    }

    public static void encrypt(InputStream in, OutputStream out, byte[] key)
            throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM, "BC");
        SecretKey secretKey = new SecretKeySpec(key, "AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        byte[] buffer_in = new byte[1024];
        byte[] buffer_out = new byte[cipher.getOutputSize(buffer_in.length)];
        while (true) {
            int read = in.read(buffer_in);
            if (read < 0)
                break;
            int processed = cipher.update(buffer_in, 0, read, buffer_out);
            out.write(buffer_out, 0, processed);
        }
        int n = cipher.doFinal(buffer_out, 0);
        out.write(buffer_out, 0, n);
    }

    public static void decrypt(InputStream in, OutputStream out, byte[] key)
            throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM, "BC");
        SecretKey secretKey = new SecretKeySpec(key, "AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);

        byte[] buffer_in = new byte[1024];
        byte[] buffer_out = new byte[cipher.getOutputSize(buffer_in.length)];
        while (true) {
            int read = in.read(buffer_in);
            if (read < 0)
                break;
            int processed = cipher.update(buffer_in, 0, read, buffer_out);
            out.write(buffer_out, 0, processed);
        }
        int n = cipher.doFinal(buffer_out, 0);
        out.write(buffer_out, 0, n);
    }

    public static void main(String[] args)
            throws Exception {
        if (args.length == 0) {
            System.out.println("gen size key_file");
            System.out.println("encrypt file key_file");
            System.out.println("decrypt file key_file");
            System.out.println("  key_file: base64");
            System.out.println("  encrypt: bytes -> bytes");
            System.out.println("  decrypt: bytes -> bytes");
            System.exit(1);
        }

        Setup.BC();

        String command = args[0];
        if (command.equalsIgnoreCase("gen")) {
            PrintWriter writer = new PrintWriter(args[2] + ".aes");
            generate(Integer.parseInt(args[1]), writer);
            writer.close();
        }

        if (command.equalsIgnoreCase("encrypt")) {
            Reader keyReader = new FileReader(args[2] + ".aes");
            byte[] key = getKey(keyReader);
            keyReader.close();

            File file = new File(args[1]);
            File blackFile = new File(args[1] + ".enc");
            FileInputStream red_in = new FileInputStream(file);
            FileOutputStream black_out = new FileOutputStream(blackFile);
            encrypt(red_in, black_out, key);
            red_in.close();
            black_out.close();
        }

        if (command.equalsIgnoreCase("decrypt")) {
            Reader keyReader = new FileReader(args[2] + ".aes");
            byte[] key = getKey(keyReader);
            keyReader.close();

            File file = new File(args[1]);
            File redFile = new File(args[1] + ".dec");
            FileInputStream black_in = new FileInputStream(file);
            FileOutputStream red_out = new FileOutputStream(redFile);
            decrypt(black_in, red_out, key);
            black_in.close();
            red_out.close();
        }
    }

    private static byte[] getKey(Reader reader)
            throws Exception {
        StringBuilder builder = new StringBuilder();
        char[] buffer = new char[1024];
        while (true) {
            int n = reader.read(buffer);
            if (n < 0)
                break;
            builder.append(buffer, 0, n);
        }
        return Base64.decode(builder.toString());
    }
}
