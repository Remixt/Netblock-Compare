package remixt;


/*
* Author: Clayton Brant
* Contact: Cbrant@intrusion.com
*
* This program is used to compare netblock ip ranges between the original providers (I.E. RIPE) and Tracecop.
* An example of an IP range is 192.168.0.1 - 192.168.0.20, where the first ip address is start, and the second ip is the end of the range belonging to that netblock.
*
* In order for this source code to compile and run the following jar files are required, most of which can be found by googling the jar file name:
*
    commons-io-2.6.jar
    commons-logging-1.2.jar
    hadoop-common-2.2.0.jar
    hadoop-common-3.1.0.jar
    hadoop-core-1.2.0.jar
    hadoop-hdfs-2.2.0.jar
    hadoop-mapreduce-client-core-2.2.0.jar
    hive-cli-1.2.0.jar
    hive-common-0.7.1-cdh3u6.jar
    hive-jdbc-1.2.0.jar
    hive-metastore-1.2.0.jar
    hive-service-1.2.0.jar
    httpclient-4.5.5.jar
    httpcore-4.4.1.jar
    libthrift-0.11.0.jar
    sj4j\slf4j-api-1.7.4.jar
    slf4j-simple-1.7.4.jar
    spark-sql_2.11-1.2.1.jar
*/

import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Main {

    private static String command = ""; // stores user input from command line
    private static String username = null; // credentials for hive database
    private static String password = null; // ^
    private static String source = null; // stores the name of the database source ex: RIPE
    private static String[] ips;
    private static Scanner keys; // takes input from keyboard

    private static Map<String, String> sourceMap; // stores the start ip as the key for each netblock data structure.
    private static Map<String, String> traceMap; // stores the start ip as the key for each netblock data structure.


    private static void runHiveQuery() throws SQLException, IOException {
        if (username == null || password == null) {
            System.out.print("Database username: ");
            username = keys.nextLine();
            System.out.println();
            System.out.print("Database password: ");
            password = keys.nextLine();
        }
        // stores the query results from hive database
        ResultSet res;
        try {
            // class file for connecting to the hive database
            HiveJdbcClient hivedb = new HiveJdbcClient(username, password);
            System.out.println("Login Success!");
            System.out.println("Running Query. This will take several minutes. Please Wait...");
            res = hivedb.getQueryResult("select distinct sta.ip2str(netblock.startipnumber), sta.ip2str(netblock.endipnumber) from netblock where version =4 and lower(lookupsourcelabel) like '%" + source.toLowerCase() + "%'");
        } catch (Exception e) {
            System.out.println("Missing or incorrect parameters");
            username = null;
            password = null;
            return;
        }

        String startIP;
        String endIP;

        traceMap = new HashMap<>();
        while (res.next()) {

            //res.next(); // temporary fix because database has three of every address for some reason... This skips one line.
           // res.next(); // ^

            startIP = res.getString(1);
            endIP = res.getString(2);
            traceMap.put(startIP, endIP);


        }
        System.out.println("Query Complete");
        compareSourceToTracecop();
    }

    private static void saveSourceMap() throws IOException, SQLException {
        System.out.println("Please enter a file name, do NOT include the extension, it will be a csv file.");
        command = keys.next() + ".csv";
        try {
            PrintWriter writer = new PrintWriter(command);
            for (Map.Entry<String, String> entry : sourceMap.entrySet()) {
                writer.println(entry.getKey());
                writer.println(entry.getValue());
            }
            writer.flush();
            writer.close();
            System.out.println("File saved: " + new File(command).getAbsolutePath());
        } catch (Exception e) {
            System.out.println("Error while saving file: ");
            e.printStackTrace();
            mainMenu();
        }
        mainMenu();
    }

    private static void saveResults(Map<String, String> mapsource, String filename) throws IOException, SQLException {
        // save the results found in the compareSourceToTracecop method to a csv comma delimited file.
        PrintWriter writer = new PrintWriter(filename + ".csv");
        writer.print("start");
        writer.print(",");
        writer.print("end");
        writer.println();
        for (Map.Entry<String,String> entry : mapsource.entrySet()){
            writer.print(entry.getKey());
            writer.print(",");
            writer.print(entry.getValue());
            writer.println();
        }
        writer.println();
        writer.flush();
        writer.close();
        System.out.println("File saved: " + new File(filename + ".csv").getAbsolutePath());
    }

    private static void searchSourceMap() throws IOException, SQLException {
        System.out.println("Please Enter an IP address to search for... Ex: '192.168.0.1'. You can type 'back' to go back to the main menu. ");
        command = keys.nextLine();
        switch (command) {
            case "back":
                mainMenu();
                break;
            case "exit":
                System.exit(0);
                break;
            case "help":
                System.out.println("Commands available: back, exit, help.");
                break;
            default:
                break;
        }

        if (sourceMap.containsKey(command)) {
            System.out.println("Found: ");
            System.out.println(sourceMap.get(command));
            System.out.println();
            searchSourceMap();
        } else {
            System.out.print("Nothing found, sorry!");
            searchSourceMap();
        }
    }

    private static void buildSourceMap() throws IOException, SQLException {
        // initialize a new map for storing the ip data.
        sourceMap = new HashMap<>();
        String IPADDRESS_PATTERN;
        Pattern pattern;
        boolean isMatch;
        String line;


        // prompt the user for the source database file (usually downloaded as a .db file with ~5gb in size)
        System.out.println("Please specify a database text file to open. ex: C:\\Users\\cbrant.INTRUSION\\IdeaProjects\\Netblock-Compare\\files\\lacnic.txt ");
        // give the user unlimited attempts to give a correct file location...
        // buffered reader for parsing data from file
        BufferedReader bReader;
        while (true) {
            System.out.print("\n:>");
            command = keys.nextLine();
            if (command.equals("exit"))
                System.exit(0);
            // attempt to open the database file and break out of the loop
            try {
                bReader = new BufferedReader(new FileReader(command));
                break;
            } catch (Exception e) {
                System.out.print("An Error Occurred, please try re-entering the file location or type 'exit' to quit the program...\n");
            }
        }

        System.out.println("Please specify the source name. ex: 'RIPE'");
        source = ""; // re-initialize source to ensure the user can reselect a new one if needed.
        while (source.equals("")) {
            System.out.print(":>");
            command = keys.nextLine().toUpperCase();
            switch (command) {
                case "HELP":
                    System.out.println("Available sources: 'RIPE' 'ARIN' 'LACNIC' 'AFRINIC' 'APNIC'");
                    break;
                case "RIPE":
                    source = "RIPE";
                    break;
                case "ARIN":
                    source = "ARIN";
                    break;
                case "LACNIC":
                    source = "LACNIC";
                    break;
                case "AFRINIC":
                    source = "AFRINIC";
                    break;
                case "APNIC":
                    source = "APNIC";
                    break;
                case "EXIT":
                    System.exit(0);
                default:
                    System.out.println("Please specify a valid source name. To list possible sources type 'help'");
                    break;
            }
        }

        // prompt the user that the file read was successful and begin processing the file.
        System.out.println("Building map from database file. This will take several minutes. Please Wait... \n");

        if (source.equals("ARIN") || source.equals("LACNIC") || source.equals("AFRINIC") || source.equals("APNIC") ||source.equals("RIPE") ) {
            IPADDRESS_PATTERN = "((?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?))[|]([\\d]*)[|]";
            pattern = Pattern.compile(IPADDRESS_PATTERN);
            // read each line and use the regex pattern to determine if the line has an ip range
            while ((line = bReader.readLine()) != null) {
                Matcher matcher = pattern.matcher(line);
                isMatch = matcher.find();
                if (isMatch) {
                    INetNum nn = new INetNum(matcher.group(1), Integer.parseInt(matcher.group(2)));
                    sourceMap.put(nn.getIpStartString(), nn.getIpEndString());
                }
            }
            bReader.close();
            if (sourceMap.isEmpty()) {
                System.out.println("Error: Unable to read any usable data in file.");
                System.exit(1);
            }

            // prompt the user when done
            System.out.println("Source map built successfully! Type 'compare' to continue or 'help' for other options...\n");
            mainMenu();

        } else {

            //Regex pattern that will match an ip range in the form: 0.0.0.0 - 0.0.0.0 where any 0 can be replaced with an integer from 1 to 255

            IPADDRESS_PATTERN = "(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?) - (?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)";
            pattern = Pattern.compile(IPADDRESS_PATTERN);

            // read each line and use the regex pattern to determine if the line has an ip range
            while ((line = bReader.readLine()) != null) {
                Matcher matcher = pattern.matcher(line);
                isMatch = matcher.find();
                if (isMatch) {
                    ips = matcher.group(0).split(" - "); // split the start and end address into the ips array, remove the dash and space characters from the strings.
                    sourceMap.put(ips[0], ips[1]); // insert the start ip address as the key and the end ip address as the associated string with that key.
                }
            }
            bReader.close();
            if (sourceMap.isEmpty()) {
                System.out.println("Error: Unable to read any usable data in file.");
                System.exit(1);
            }

            // prompt the user when done
            System.out.println("Source map built successfully! Type 'compare' to continue or 'help' for other options...\n");
            mainMenu();
        }
    }

    private static void compareSourceToTracecop() throws IOException, SQLException {

        /*
        This looks confusing, but its pretty simple.
        1. Create a copy of the keys (staring ip address) in tracecop. Do this twice to avoid removing data from your original set to be used for comparisons.
        2. Create a copy of the keys (starting ip address) in the source data.
        3. Remove the keys from tracecop that also exist in the source. - this leaves a set that contains data found only tracecop.
        4. Repeat this but inversely to get the data contained in the source that is missing from tracecop.
        */
        System.out.println("---------------------------------------");
        System.out.println("Running comparison, please wait...");


        Map<String,String> missingTraceMap = new HashMap<>();
        Map<String,String> missingSourceMap = new HashMap<>();

        System.out.println("---------------------------------------");
        System.out.println("Size of " + source + ": " + sourceMap.size());
        System.out.println("Size of tracecop: " + traceMap.size());
        System.out.println("---------------------------------------");

        for (Map.Entry<String,String> entry : sourceMap.entrySet()){
            if(traceMap.containsKey(entry.getKey())){
                if(!entry.getValue().equalsIgnoreCase(traceMap.get(entry.getKey()))){
                    missingTraceMap.put(entry.getKey(),entry.getValue());
                }
            }else {missingTraceMap.put(entry.getKey(),entry.getValue());}
        }

        for (Map.Entry<String,String> entry : traceMap.entrySet()){
            if(sourceMap.containsKey(entry.getKey())){
                if(!entry.getValue().equalsIgnoreCase(sourceMap.get(entry.getKey()))){
                    missingSourceMap.put(entry.getKey(),entry.getValue());
                }
            }else {missingSourceMap.put(entry.getKey(),entry.getValue());}
        }

        // Save the results to a csv file
        System.out.println("---------------------------------------");
        System.out.println("Missing from Tracecop: " + missingTraceMap.size());
        System.out.println("Missing from " + source + ": " + missingSourceMap.size());
        System.out.println("---------------------------------------");

        // get date and time for unique file name
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyMMdd_HHmm");
        LocalDateTime now = LocalDateTime.now();

       saveResults(missingTraceMap, source.toLowerCase() + "_miss_from_trace_" + dtf.format(now));
        saveResults(missingSourceMap, "trace_miss_from_" + source.toLowerCase() + "_" + dtf.format(now));
        mainMenu();
    }

    private static void mainMenu() throws IOException, SQLException {

        while (true) {

            System.out.print("\n:>");
            command = keys.nextLine().toLowerCase();

            switch (command) {
                case "help":
                    System.out.println("Usage:" +
                            "\n 'help' - displays this menu." +
                            "\n 'search' - search the current database map for a specific ip." +
                            "\n 'save' - saves the current database map ip range to a .csv file." +
                            "\n 'compare' - compares the current database map ip range to the tracecop database." +
                            "\n 'exit' - closes the program." +
                            "\n 'new' - creates a new ip range database map from a new file.");
                    break;
                case "search":
                    searchSourceMap();
                    break;
                case "save":
                    saveSourceMap();
                    break;
                case "compare":
                    runHiveQuery();
                    break;
                case "exit":
                    System.exit(0);
                    break;
                case "new":
                    buildSourceMap();
                    break;
                default:
                    System.out.println("Usage: 'help', 'search', 'save', 'compare', 'new', 'exit'");
                    break;
            }
        }
    }

    public static void main(String[] args) throws IOException, SQLException {
        if (args.length < 2) {

            keys = new Scanner(System.in);
            buildSourceMap();


        } else {
            System.out.println("Sorry, command line arguments not yet implemented...");
            System.exit(0);
        }
    }
}
