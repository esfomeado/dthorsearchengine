package pt.ipb.dthor;

public class Main {

    public static void main(String[] args) throws Exception {
        DThorConfig config = new DThorConfig();
        config.load();
        DThorTomP2P dht = DThorTomP2P.getInstance();

        DThorCrawling request = new DThorCrawling();
        Thread requestThread = new Thread(request);
        requestThread.start();

        LuceneServer search = LuceneServer.getInstance();

        JettyServer server = JettyServer.getInstance();
        server.waitForInterrupt();

    }

}
