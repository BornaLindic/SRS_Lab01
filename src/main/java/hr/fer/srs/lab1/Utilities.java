package hr.fer.srs.lab1;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

public class Utilities {

    public static SecretKey generateSecretKey(String masterPassword, byte[] iv) {
        SecretKey secret = null;

        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            KeySpec spec = new PBEKeySpec(masterPassword.toCharArray(), iv, 65536, 256);
            SecretKey tmp = factory.generateSecret(spec);
            secret = new SecretKeySpec(tmp.getEncoded(), "AES");
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }

        return secret;
    }


    public static void encryption(byte[] iv, SecretKey key, Path in, Path out, boolean encrypt) {
        AlgorithmParameterSpec paramSpec = new IvParameterSpec(iv);
        Cipher cipher = null;

        // initialize cipher
        try {
            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(encrypt ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE, key, paramSpec);
        } catch (InvalidAlgorithmParameterException | NoSuchPaddingException |
                NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
            System.exit(1);
        }

        // perform encryption/decryption
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
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            System.out.println("Wrong password!");
            System.exit(0);
        }
    }


    public static byte[] generateIv() {
        byte[] iv = new byte[16];

        try {
            SecureRandom randomSecureRandom = SecureRandom.getInstance("SHA1PRNG");
            iv = new byte[16];
            randomSecureRandom.nextBytes(iv);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return iv;
    }

}
