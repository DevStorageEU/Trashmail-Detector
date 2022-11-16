package eu.devstorage.tmd;

import eu.devstorage.tmd.database.ContentFetchExecutor;
import eu.devstorage.tmd.database.DomainDatabase;
import eu.devstorage.tmd.http.ApiHttpServer;

public class Main {

    private static Main instance;
    private ApiHttpServer httpServer;
    private DomainDatabase database;
    private ContentFetchExecutor contentFetchExecutor;


    public static void main(String[] args) {

        try {
            getInstance().getDatabase().loadURlSources();
            getInstance().getContentFetchExecutor().run();
            getInstance().getHttpServer().listen();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public ApiHttpServer getHttpServer() {
        return httpServer == null ? httpServer = new ApiHttpServer() : httpServer;
    }

    public ContentFetchExecutor getContentFetchExecutor() {
        return contentFetchExecutor == null ? contentFetchExecutor = new ContentFetchExecutor() : contentFetchExecutor;
    }

    public DomainDatabase getDatabase() {
        return database == null ? database = new DomainDatabase() : database;
    }

    public static Main getInstance() {
        return instance == null ? instance = new Main() : instance;
    }
}
