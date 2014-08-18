package pt.ipb.dthor;

import java.io.IOException;
import java.util.ArrayList;
import net.tomp2p.peers.Number160;
import pt.ipb.dthor.torrent.DThorTorrent;

public interface IDThor {

    public ArrayList<DThorTorrent> getTorrents(ArrayList<Number160> keys) throws ClassNotFoundException, IOException;
}
