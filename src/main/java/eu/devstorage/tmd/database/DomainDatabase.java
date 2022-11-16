package eu.devstorage.tmd.database;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import java.io.*;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DomainDatabase {
    public static List<String> urlSources = new ArrayList<>();
    public static List<String> domains = new ArrayList<>();
    public static final File DOMAIN_FILE = new File("domains.json");
    public static final File CONFIG_FILE = new File("config.json");


    public void loadURlSources() {
        try {
            JsonReader jsonReader = new JsonReader(new FileReader(CONFIG_FILE));
            List<String> data = new Gson().fromJson(jsonReader, new TypeToken<List<String>>() {}.getType());
            urlSources.addAll(data);
            urlSources.forEach(s -> System.out.println("[CONFIG] Loaded URL Source " + s +" from config."));

        } catch (FileNotFoundException e) {
            /**
             * It is allowed that the software does not use external sources to build its domain database.
             * However, it must be ensured in any case that the domains.json is then filled with own or old content.
             * It is also possible that the domains.json is filled once with a valid config.json and then continues to work with this domain inventory.
             * @see CONFIG_FILE
             * @see domains
             */
            System.out.println("[WARNING] config.json not found. Please make sure that the domains.json has been filled with content, because no new content can be (re)loaded.");
        }
    }

    public void initializeDatabaseContent() {
        /**
         * At software start the existing domain inventory is loaded from domains.json into the domain array/list.
         * The reason for this is that config.json no longer has any external URLs or these are no longer accessible.
         * So the API knows at least an old inventory and works until the new fetch with this data.
         * The data is parsed from the json and loaded into an array list.
         */
        try {
            Type domainType = new TypeToken<List<String>>() {}.getType();
            JsonReader jsonReader = new JsonReader(new FileReader(DOMAIN_FILE));
            List<String> data = new Gson().fromJson(jsonReader, domainType);
            domains.addAll(data);
        } catch (FileNotFoundException e) {
            System.out.println("[WARNING] Currently there is no domain file (domains.json). This will be created after the first domain queries.");
        }

        /**
         * If url sources have been loaded, they will now be passed through piece by piece and the data will be read out. The software assumes that the data in the external URLs are simple lists without HTML or similar and can be read down line by line.
         * Expected structure is as follows:
         * <pre>
         *     mytopwebhosting.com
         *     mytrafficsecretsbook.com
         *     mytrashmail.com
         * </pre>
         * @see urlSources
         */

        urlSources.forEach(source -> {
            System.out.println("[INFO] Try to fetch domains from " + source);
            try {
                URLConnection conn = new URL(source).openConnection();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    String response = reader.lines().collect(Collectors.joining("\n"));
                    Stream<String> stream = Arrays.stream(response.split("\n"));
                    AtomicInteger domainsAdded = new AtomicInteger(0);
                    stream.filter(domain -> !domains.contains(domain.toLowerCase())).forEach(domain -> {
                        /**
                         * The domains are always all stored uniformly as lowercased.
                         * This unifies the retrieval and storage of the data and saves the comparison with <pre>equalsIgnoreCase()</pre>
                         */
                        domains.add(domain.toLowerCase());
                        domainsAdded.addAndGet(1);
                    });

                    System.out.println("[INFO] Added " + domainsAdded.get() + " new Domains to the cache");
                }
            } catch (IOException e) {
                System.err.println("[INFO] Failed while fetching domain list from " + source + ". Error: " + e.getMessage());
            }

        });


        try {
            /**
             * After the domains are added to the list or replaced in it, the data from the cache is overwritten in the domains.json.
             * Thus the software runs afterwards independently of an existing Internet connection.
             */
            PrintWriter writer = new PrintWriter(DOMAIN_FILE, StandardCharsets.UTF_8);
            writer.println(new GsonBuilder().setPrettyPrinting().create().toJson(domains));
            writer.close();
            System.out.println("[SUCCESS] Added all Domains to the domains.json");

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public boolean found(String domain) {
        return getDomains().contains(domain.toLowerCase());
    }


    public List<String> getDomains() {
        return domains;
    }
}
