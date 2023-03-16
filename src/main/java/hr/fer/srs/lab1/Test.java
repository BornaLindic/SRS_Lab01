package hr.fer.srs.lab1;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.HashMap;
import java.util.Map;

public class Test {

    public static void main(String[] args) throws IOException, ClassNotFoundException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, InvalidKeyException {

        Map<String, String> passwords = new HashMap<>();

        passwords.put("fer", "123");

        // placing the map on the disk
        FileOutputStream fos = new FileOutputStream("map.ser");
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(passwords);
        oos.close();


        // generating IV
        SecureRandom randomSecureRandom = SecureRandom.getInstance("SHA1PRNG");
        byte[] iv = new byte[16];
        randomSecureRandom.nextBytes(iv);
        IvParameterSpec ivParams = new IvParameterSpec(iv);

        FileOutputStream osIv = new FileOutputStream("iv.cipher");
        osIv.write(iv);
        osIv.close();

        FileInputStream isIV = new FileInputStream("iv.cipher");
        iv = isIV.readAllBytes();
        isIV.close();

        // generating key from password
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        KeySpec spec = new PBEKeySpec("masterPass".toCharArray(), iv, 65536, 256);
        SecretKey tmp = factory.generateSecret(spec);
        SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");
        System.out.println(secret);

        //initializing cipher
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secret, ivParams);

        // encrypting
        Path in = Paths.get("map.ser");
        Path out = Paths.get("map.encrypted");

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
        } catch (IOException | IllegalBlockSizeException | BadPaddingException ex) {
            System.out.println(ex.getMessage());
        }

        // decrypting
        cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secret, ivParams);

        in = Paths.get("map.encrypted");
        out = Paths.get("map.ser2");

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
        } catch (IOException | IllegalBlockSizeException | BadPaddingException ex) {
            System.out.println(ex.getMessage());
        }


        // reading the decrypted map
        FileInputStream fis = new FileInputStream("map.ser2");
        ObjectInputStream ois = new ObjectInputStream(fis);
        HashMap<String, String> anotherMap = (HashMap) ois.readObject();
        ois.close();

        System.out.println(anotherMap);


    }

}
