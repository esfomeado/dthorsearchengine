package pt.ipb.dthor;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.futures.FutureDirect;
import net.tomp2p.futures.FutureDiscover;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.storage.Data;
import pt.ipb.dthor.torrent.DThorTorrent;

public class DThorTomP2P implements IDThor {

    private final String SUPER_PEER_IP = DThorConfig.SUPER_PEER_IP;
    private final int SUPER_PEER_PORT = DThorConfig.SUPER_PEER_PORT;
    private static DThorTomP2P instance = null;
    private PeerDHT peer;

    public static DThorTomP2P getInstance() throws IOException, Exception {
        if(instance == null) {
            instance = new DThorTomP2P();
        }
        return instance;
    }

    private DThorTomP2P() throws IOException, Exception {
        Random r = new Random();
        int listenPort = SUPER_PEER_PORT + 1 + (int) Math.floor(Math.random() * 1000);
        peer = new PeerBuilderDHT(new PeerBuilder(new Number160(r)).ports(listenPort).start()).start();
        PeerAddress peerAddress = new PeerAddress(Number160.ZERO, InetAddress.getByName(SUPER_PEER_IP), SUPER_PEER_PORT, SUPER_PEER_PORT);

        FutureDiscover discovery = peer.peer().discover().peerAddress(peerAddress).start();
        discovery.awaitUninterruptibly();

        if(discovery.isFailed()) {
            //Executar NAT
        }

        peerAddress = discovery.peerAddress();

        FutureBootstrap bootstrap = peer.peer().bootstrap().peerAddress(peerAddress).start();
        bootstrap.awaitUninterruptibly();

        if(bootstrap.isFailed()) {
            throw new Exception("Failed to bootstrap to host " + bootstrap.failedReason());
        }

        System.out.println("Peer Connected!");
    }

    @Override
    public ArrayList<DThorTorrent> getTorrents(ArrayList<Number160> keys) throws ClassNotFoundException, IOException {
        ArrayList<DThorTorrent> torrents = new ArrayList<>();

        for(Number160 key : keys) {
            FutureGet fg = peer.get(key).start();
            fg.awaitUninterruptibly();

            Data torrentData = fg.data();
            DThorTorrent torrent = (DThorTorrent) torrentData.object();
            torrents.add(torrent);
        }

        return torrents;
    }

    @Override
    public DThorTorrent searchTorrent(Number160 key) throws ClassNotFoundException, IOException {
        FutureGet fg = peer.get(key).start();
        fg.awaitUninterruptibly();

        Data torrentData = fg.data();

        return (DThorTorrent) torrentData.object();
    }

    public List<PeerAddress> getPeers() {
        List<PeerAddress> peers = peer.peerBean().peerMap().all();

        return peers;
    }

    public ArrayList<Number160> requestKeys(final PeerAddress destination) throws ClassNotFoundException, IOException {
        FutureDirect request = peer.peer().sendDirect(destination).object("keys").start();
        request.awaitUninterruptibly();

        ArrayList<Number160> keys = new ArrayList<>();

        if(request.isSuccess()) {
            keys = (ArrayList<Number160>) request.object();
        }

        return keys;
    }
}
