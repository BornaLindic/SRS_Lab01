package hr.fer.srs.lab1;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Scanner;

public class Crypto {

    public static void main(String[] args) throws NoSuchAlgorithmException, IOException,
            NoSuchPaddingException, InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {

        Scanner sc = new Scanner(System.in);

        if (args[0].toLowerCase().equals("checksha")) {
            Path p = Paths.get(args[1]);
            MessageDigest sha = MessageDigest.getInstance("SHA-256");

            System.out.println("Please provide expected sha-256 digest for hw05test.bin:");
            String expected = sc.nextLine();
            sc.close();

            try (InputStream is = Files.newInputStream(p)) {
                byte[] buff = new byte[4096];
                while (true) {
                    int r = is.read(buff);
                    if (r < 1) break;
                    // use just buff[0] - buff[r-1]
                    byte[] temp = new byte[r];
                    System.arraycopy(buff, 0, temp, 0, r);
                    sha.update(temp);
                }
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            }

            byte[] digest = sha.digest();
            String calculated = Util.bytetohex(digest);

            if (calculated.equals(expected)) {
                System.out.println("Digesting completed. Digest of " + args[1] + " matches expected digest.");
            } else
                System.out.println("Digesting completed. Digest of " + args[1] +
                        " does not match the expected digest. Digest :was" + calculated);

            System.exit(0);
        }

        boolean encrypt;
        if (args[0].toLowerCase().equals("encrypt")) {
            encrypt = true;
        } else
            encrypt = false;

        System.out.println("Please provide password as hex-encoded text (16 bytes, i.e. 32 hex-digits):");
        String keyText = sc.nextLine();

        System.out.println("Please provide initialization vector as hex-encoded text (32 hex-digits):");
        String ivText = sc.nextLine();
        sc.close();

        SecretKeySpec keySpec = new SecretKeySpec(Util.hextobyte(keyText), "AES");
        AlgorithmParameterSpec paramSpec = new IvParameterSpec(Util.hextobyte(ivText));
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(encrypt ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE, keySpec, paramSpec);

        Path in = Paths.get(args[1]);
        Path out = Paths.get(args[2]);

        try (InputStream is = Files.newInputStream(in); OutputStream os = Files.newOutputStream(out)) {
            byte[] buff = new byte[4096];
            while (true) {
                int r = is.read(buff);
                if (r < 1) {
                    os.write(cipher.doFinal());
                    break;
                }
                // use just buff[0] - buff[r-1]
                byte[] temp = new byte[r];
                System.arraycopy(buff, 0, temp, 0, r);
                byte[] output = cipher.update(temp);
                os.write(output);
            }
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }

        if (encrypt) {
            System.out.println("Encryption completed. Generated file " +  args[2] +
                    " based on file " + args[1]);
        } else
            System.out.println("Decryption completed. Generated file " +  args[1] +
                    " based on file " + args[2]);

    }


}
