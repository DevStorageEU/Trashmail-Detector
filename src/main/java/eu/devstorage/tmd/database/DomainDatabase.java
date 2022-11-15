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
    static  {
        urlSources.add("https://raw.githubusercontent.com/wesbos/burner-email-providers/master/emails.txt");
        urlSources.add("https://raw.githubusercontent.com/disposable-email-domains/disposable-email-domains/master/disposable_email_blocklist.conf");
        urlSources.add("https://gist.githubusercontent.com/michenriksen/8710649/raw/e09ee253960ec1ff0add4f92b62616ebbe24ab87/disposable-email-provider-domains");
    }

    public void initializeDatabaseContent() {
        try {
            Type domainType = new TypeToken<List<String>>() {}.getType();
            JsonReader jsonReader = new JsonReader(new FileReader(DOMAIN_FILE));
            List<String> data = new Gson().fromJson(jsonReader, domainType);
            domains.addAll(data);
        } catch (FileNotFoundException e) {
            System.out.println("[WARNING] Currently there is no domain file (domains.json). This will be created after the first domain queries.");
        }

        urlSources.forEach(source -> {
            System.out.println("[INFO] Try to fetch domains from " + source);
            try {
                URLConnection conn = new URL(source).openConnection();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    String response = reader.lines().collect(Collectors.joining("\n"));
                    Stream<String> stream = Arrays.stream(response.split("\n"));
                    AtomicInteger domainsAdded = new AtomicInteger(0);
                    stream.filter(domain -> !domains.contains(domain.toLowerCase())).forEach(domain -> {
                        domains.add(domain.toLowerCase());
                        domainsAdded.addAndGet(1);
                    });

                    System.out.println("[INFO] Added " + domainsAdded.get()+ " new Domains to the cache");
                }
            } catch (IOException e) {
                System.err.println("[INFO] Failed while fetching domain list from " + source +". Error: " + e.getMessage());
            }

        });


        try {
            if(DOMAIN_FILE.delete() && DOMAIN_FILE.createNewFile()){
                PrintWriter writer = new PrintWriter(DOMAIN_FILE, StandardCharsets.UTF_8);
                writer.println(new GsonBuilder().setPrettyPrinting().create().toJson(domains));
                writer.close();
                System.out.println("[SUCCESS] Added all Domains to the domains.json");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public boolean found(String domain){
        return getDomains().contains(domain.toLowerCase());
    }


    public List<String> getDomains() {
        return domains;
    }
}
