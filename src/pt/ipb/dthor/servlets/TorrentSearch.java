package pt.ipb.dthor.servlets;

import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.tomp2p.peers.Number160;
import org.apache.lucene.queryparser.classic.ParseException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import pt.ipb.dthor.DThorTomP2P;
import pt.ipb.dthor.LuceneServer;
import pt.ipb.dthor.torrent.DThorTorrent;
import pt.ipb.dthor.torrent.TorrentParser;

public class TorrentSearch extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String key = request.getParameter("key");

        if(key != null) {

            try {
                DThorTomP2P dht = DThorTomP2P.getInstance();
                DThorTorrent torrent = dht.searchTorrent(new Number160(key));
                String torrentName = torrent.getSaveAs() + ".torrent";

                response.setContentType("text/plain");
                response.setHeader("Content-Disposition", "attachment;filename=\"" + torrentName + "\"");

                byte[] torrentData = TorrentParser.makeTorrent(torrent);

                OutputStream os = response.getOutputStream();
                os.write(torrentData);
                os.flush();
                os.close();

            } catch (Exception ex) {
                Logger.getLogger(RemoteTorrentSearch.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            RequestDispatcher rd = getServletContext().getRequestDispatcher("/search.jsp");
            rd.forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        LuceneServer searchEngine = LuceneServer.getInstance();

        String query = request.getParameter("query");

        try {
            String keys = searchEngine.search(query);

            Object obj = JSONValue.parse(keys);
            JSONArray array = (JSONArray) obj;

            String table = "";
            for(Object array1 : array) {
                JSONObject a = (JSONObject) array1;
                table += "<tr><td>";
                table += "<a class=\"torrent_link\" href=\"?key=" + a.get("key") + "\">";
                table += "<label class=\"torrent_name\">" + a.get("title") + "</label>";
                table += "</a></td></tr>";
            }

            request.setAttribute("query", query);
            request.setAttribute("table", table);
            RequestDispatcher rd = getServletContext().getRequestDispatcher("/search.jsp");
            rd.forward(request, response);

        } catch (ParseException | InterruptedException ex) {
            Logger.getLogger(RemoteTorrentSearch.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
