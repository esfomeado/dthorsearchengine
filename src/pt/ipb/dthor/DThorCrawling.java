package pt.ipb.dthor;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;
import pt.ipb.dthor.torrent.DThorTorrent;
import pt.ipb.dthor.torrent.TorrentDoc;

public class DThorCrawling implements Runnable {

    @Override
    public void run() {
        try {
            DThorTomP2P dht = DThorTomP2P.getInstance();
            LuceneServer searchEngine = LuceneServer.getInstance();
            while (true) {
                List<PeerAddress> peers = dht.getPeers();
                for (PeerAddress peer : peers) {
                    ArrayList<Number160> keys = dht.requestKeys(peer);
                    ArrayList<DThorTorrent> torrents = dht.getTorrents(keys);
                    
                    for(int i = 0; i < torrents.size(); i++) {
                        TorrentDoc torrentDoc = searchEngine.indexTorrent(torrents.get(i), keys.get(i).toString());
                        searchEngine.indexer(torrentDoc);
                    }
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(DThorCrawling.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
