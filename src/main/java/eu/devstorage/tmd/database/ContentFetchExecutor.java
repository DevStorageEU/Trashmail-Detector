package eu.devstorage.tmd.database;

import eu.devstorage.tmd.Main;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class ContentFetchExecutor  {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public void run() {
        scheduler.scheduleAtFixedRate(() -> Main.getInstance().getDatabase().initializeDatabaseContent(), 0, 1, TimeUnit.DAYS);
    }
}
