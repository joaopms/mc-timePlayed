package net.joaopms.MinecraftTimePlayed;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class Main {
    private static Map<String, Integer> players = new HashMap<String, Integer>();

    public static void main(String[] args) {
        System.out.println("╔==========================================================╗");
        System.out.println("║ Minecraft Played Time version 1.0 - Developed by joaopms ║");
        System.out.println("╚==========================================================╝");
        System.out.println("");
        System.out.println("Please enter the path where the folder \"stats\" is located: ");

        Scanner scanner = new Scanner(System.in);
        String path = scanner.nextLine();
        scanner.close();

        System.out.println("");
        System.out.println("Provided path: " + path);
        System.out.println("");
        File folder = new File(path);

        if (!folder.exists()) {
            System.err.println("The provided path doesn't exist!");
            System.exit(-1);
        }

        if (!folder.isDirectory()) {
            System.err.println("The provided path isn't a directory!");
            System.exit(-1);
        }

        for (File file : folder.listFiles()) {
            if (!file.isFile())
                continue;

            if (!file.getName().contains(".json"))
                continue;

            String playerUUID = file.getName().replace(".json", "");
            playerUUID = playerUUID.replace("-", "");

            String playerProfile = getPlayerProfile(playerUUID);
            String playerName = getPlayerName(playerProfile);

            String playerStats = getPlayerStats(file);
            int minutesPlayed = getMinutesPlayed(playerStats);

            registerPlayer(playerName, minutesPlayed);
        }

        printResults();
    }

    private static String getPlayerProfile(String playerUUID) {
        String playerProfile = null;

        try {
            URL url = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + playerUUID);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            InputStreamReader input = new InputStreamReader(connection.getInputStream());
            BufferedReader buff = new BufferedReader(input);
            playerProfile = buff.readLine();
            buff.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return playerProfile;
    }

    private static String getPlayerName(String playerProfile) {
        if (playerProfile == null) {
            System.err.println("An error occurred!");
            System.exit(-1);
        }

        JSONParser parser = new JSONParser();
        try {
            JSONObject jsonProfile = (JSONObject) parser.parse(playerProfile);
            return jsonProfile.get("name").toString();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static String getPlayerStats(File stats) {
        String playerStats = null;

        try {
            FileReader input = new FileReader(stats);
            BufferedReader buff = new BufferedReader(input);
            playerStats = buff.readLine();
            buff.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return playerStats;
    }

    private static int getMinutesPlayed(String playerStats) {
        int ticksPlayed = 0;

        if (playerStats == null) {
            System.err.println("An error occurred!");
            System.exit(-1);
        }

        JSONParser parser = new JSONParser();
        try {
            JSONObject jsonProfile = (JSONObject) parser.parse(playerStats);
            ticksPlayed = Integer.parseInt(jsonProfile.get("stat.playOneMinute").toString());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return ticksPlayed / 1200;
    }

    private static String formatMinutes(int minutesPlayed) {
        int days = minutesPlayed / 24 / 60;
        int hours = minutesPlayed / 60 % 24;
        int minutes = minutesPlayed % 60;

        String daysFormatted = String.format("%02d", days) + (days == 1 ? " day" : " days");
        String hoursFormatted = String.format("%02d", hours) + (hours == 1 ? " hour" : " hours");
        String minutesFormatted = String.format("%02d", minutes) + (minutes == 1 ? " minute" : " minutes");
        String totalMinutes = "Total Minutes: " + String.format("%02d", minutesPlayed) + (minutes == 1 ? " minute" : " minutes");

        return String.format("%s, %s, %s (%s)", daysFormatted, hoursFormatted, minutesFormatted, totalMinutes);
    }

    private static void registerPlayer(String playerName, int minutesPlayed) {
        System.out.println("Player " + playerName + " processed");
        players.put(playerName, minutesPlayed);
    }

    private static void sortPlayersByTime() {
        Map<String, Integer> oldMap = players;

        List list = new LinkedList(oldMap.entrySet());
        Collections.sort(list, new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((Comparable) ((Map.Entry) (o1)).getValue()).compareTo(((Map.Entry) (o2)).getValue());
            }
        });

        Map<String, Integer> result = new LinkedHashMap<String, Integer>();
        for (Iterator it = list.iterator(); it.hasNext(); ) {
            Map.Entry entry = (Map.Entry) it.next();
            result.put(entry.getKey().toString(), Integer.parseInt(entry.getValue().toString()));
        }

        players = result;
    }

    private static void printResults() {
        sortPlayersByTime();

        try {
            File file = new File("timePlayed.txt");
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.append("Generated on " + Calendar.getInstance().getTime() + "\n\n");

            System.out.println("");
            System.out.println("Player Username | Time Played");
            fileWriter.append("Player Username | Time Played\n");
            for (String player : players.keySet()) {
                System.out.println(String.format("%s | %s", player, formatMinutes(players.get(player))));
                fileWriter.append(String.format("%s | %s", player, formatMinutes(players.get(player))) + "\n");
            }

            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
