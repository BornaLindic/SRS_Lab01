package hr.fer.srs.lab1;

import javax.crypto.SecretKey;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Scanner;

public class PasswordManager {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);
        String masterPassword;
        byte[] iv = new byte[16];
        Path in;
        Path out;

        // read iv
        try {
            FileInputStream isIV = new FileInputStream("iv");
            iv = isIV.readAllBytes();
            isIV.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // get the master password and generate key
        System.out.println("Please enter your master password: ");
        masterPassword = sc.nextLine();
        SecretKey secret = Utilities.generateSecretKey(masterPassword, iv);

        // decrypt file
        in = Paths.get("map.encrypted");
        out = Paths.get("map.decrypted");
        Utilities.encryption(iv, secret, in, out, false);

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
                FileOutputStream fos = new FileOutputStream("map.encrypted");
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(passwords);
                oos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // generate new iv
        iv = Utilities.generateIv();
        try {
            FileOutputStream osIv = new FileOutputStream("iv");
            osIv.write(iv);
            osIv.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // generate key with new iv and encrypt
        secret = Utilities.generateSecretKey(masterPassword, iv);
        in = Paths.get("map.decrypted");
        out = Paths.get("map.encrypted");
        Utilities.encryption(iv, secret, in, out, true);

        // delete decrypted file from the disk
        File map = new File("map.decrypted");
        map.delete();


    }
}
