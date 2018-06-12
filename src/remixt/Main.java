package remixt;

/*
* This program is used to compare netblock data between the original providers and tracecop
* Author: Clayton Brant
* Contact: Cbrant@intrusion.com
*/

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Main {

    static String line = ""; // for reading lines bReader text file
    static INetNum netNum; // custom class for storing and printing start/end ip for a given netblock
    static Map<String, INetNum> netblockMap; // stores the start ip as the key for each netblock datastruct
    static Scanner keys = new Scanner(System.in); // accepts user input from keyboard
    static String command = ""; // stores user input from command line
    static BufferedReader bReader; // buffered reader for parsing data from file


    public static void main(String[] args) throws IOException {
        netblockMap = new HashMap<String, INetNum>();
        System.out.println("\nWelcome, please specify a database text file to open. ex: C:\\Users\\cbrant\\desktop\\ripe.txt ");
        while (true) {
            System.out.print(":>");
            command = keys.nextLine();
            if (command.equals("exit"))
                System.exit(0);
            try {
                bReader = new BufferedReader(new FileReader(command));
                break;
            } catch (Exception e) {
                System.out.print("An Error Occurred, please try re-entering the file location!\n");
            }
        }
        System.out.println("Building map from text file, please wait... \n");
        while ((line = bReader.readLine()) != null) {
            if (line.length() == 47 && line.contains("inetnum:")) {
                netNum = new INetNum(line);
                netblockMap.put(netNum.getIpStartString(), netNum);
            }
        }
        bReader.close();
        if (netblockMap.isEmpty()) {
            System.out.println("Error: Unable to read any usable data from file.");
            System.exit(1);
        }

        System.out.println("Map built successfully! \n");
        ThreadGroup tg = Thread.currentThread().getThreadGroup(); // kill app if the parent thread dies.
        while (tg.getParent() != null) {
            command = "";
            System.out.print("\n:> ");
            command = keys.nextLine();
            if (command.contains("help")) {
                System.out.println("Commands available: 'help', 'search', 'exit', 'save' ");
            } else if (command.contains("search")) {
                if (netblockMap.get(command.substring(7)) != null) {
                    System.out.println("Found start address in the database! ");
                    netblockMap.get(command.substring(7)).printIP();
                } else {
                    System.out.print("Nothing found, sorry!");
                }
            } else if (command.equals("exit")) {
                System.out.println("Are you sure you want to exit? Y/N");
                command = keys.nextLine();
                switch (command) {
                    case "y":
                        System.exit(0);
                    case "Y":
                        System.exit(0);
                }
            } else if (command.equals("save")) {
                System.out.println("Please enter a file name");
                command = keys.next() + ".txt";
                PrintWriter writer = new PrintWriter(command, "UTF-8");

                for (Map.Entry<String, INetNum> entry : netblockMap.entrySet()) {
                    writer.println("Start Ip: " + entry.getValue().getIpStartString() + "\nEnd Ip  : " + entry.getValue().getIpEndString());
                    writer.println("---------");
                }
                writer.flush();
                writer.close();
                System.out.println("File saved: " + new File(command).getAbsolutePath());
            }
        }
        System.out.println("Parent thread terminated, closing program...");
        System.exit(0);
    }
}
