package hr.fer.srs.lab1;

import javax.crypto.SecretKey;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.Scanner;

public class PasswordManager {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);
        String masterPassword;
        byte[] ivCipher = new byte[16];
        byte[] ivMac = new byte[16];


        // read initialization vectors
        try {
            FileInputStream isIvC = new FileInputStream("iv.cipher");
            ivCipher = isIvC.readAllBytes();
            isIvC.close();

            FileInputStream isIvM = new FileInputStream("iv.mac");
            ivMac = isIvM.readAllBytes();
            isIvM.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        // get the master password and generate keys
        System.out.println("Please enter your master password: ");
        masterPassword = sc.nextLine();
        SecretKey secretCipher = Utilities.generateSecretKey(masterPassword, ivCipher);
        SecretKey secretMac = Utilities.generateSecretKey(masterPassword, ivMac);

        //check integrity
        byte[] storedMac = null;
        byte[] calculatedMac;

        try {
            FileInputStream isMac = new FileInputStream("mac");
            storedMac = isMac.readAllBytes();
            isMac.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        calculatedMac = Utilities.calculateHMacSHA256(secretMac);

        if(!Arrays.equals(storedMac, calculatedMac)) {
            System.out.println("Integrity check failed!");
            System.exit(0);
        } else System.out.println("Integrity check complete!\n");

        // decrypt file
        Path in = Paths.get("map.encrypted");
        Path out = Paths.get("map.decrypted");
        Utilities.encryption(ivCipher, secretCipher, in, out, false);

        // load file
        Map<String, String> passwords = null;
        try {
            FileInputStream fis = new FileInputStream("map.decrypted");
            ObjectInputStream ois = new ObjectInputStream(fis);
            passwords = (Map) ois.readObject();
            ois.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        // let user modify and read from the file
        System.out.println("Store passwords with: put [destination] [password]");
        System.out.println("Get passwords with: get [destination]");
        System.out.println("Exit application with: close");
        System.out.println();

        String userInput = sc.nextLine();
        boolean modified = false;

        while (!userInput.equals("close")) {
            String[] inputs = userInput.split(" ");
            if (inputs[0].equals("put")) {
                passwords.put(inputs[1], inputs[2]);
                System.out.println("Stored password " + inputs[2] + " for " + inputs[1]);
                modified = true;
            } else if (inputs[0].equals("get")) {
                if(!passwords.containsKey(inputs[1])) {
                    System.out.println("No password is stored for " + inputs[1]);
                } else {
                    System.out.println("Password for " + inputs[1] + " is " + passwords.get(inputs[1]));
                }
            }
            userInput = sc.nextLine();
        }

        // store new map
        if(modified) {
            try {
                FileOutputStream fos = new FileOutputStream("map.decrypted");
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(passwords);
                oos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // generate new initialization vectors
        ivCipher = Utilities.generateIv();
        ivMac = Utilities.generateIv();
        try {
            FileOutputStream osIvC = new FileOutputStream("iv.cipher");
            osIvC.write(ivCipher);
            osIvC.close();

            FileOutputStream osIvM = new FileOutputStream("iv.mac");
            osIvM.write(ivMac);
            osIvM.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        // generate cipher key with new iv and encrypt
        secretCipher = Utilities.generateSecretKey(masterPassword, ivCipher);
        in = Paths.get("map.decrypted");
        out = Paths.get("map.encrypted");
        Utilities.encryption(ivCipher, secretCipher, in, out, true);

        //generate mac key with new iv and calculate mac
        secretMac = Utilities.generateSecretKey(masterPassword, ivMac);
        byte[] mac = Utilities.calculateHMacSHA256(secretMac);

        try {
            FileOutputStream osMac = new FileOutputStream("mac");
            osMac.write(mac);
            osMac.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // delete decrypted file from the disk
        File map = new File("map.decrypted");
        map.delete();


    }
}
