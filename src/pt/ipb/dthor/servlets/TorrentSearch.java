package pt.ipb.dthor.servlets;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.lucene.queryparser.classic.ParseException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import pt.ipb.dthor.LuceneServer;

public class TorrentSearch extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        LuceneServer searchEngine = LuceneServer.getInstance();

        String query = request.getParameter("query");

        try {
            String keys = searchEngine.search(query);
            response.getOutputStream().print(keys);
            response.flushBuffer();

        } catch (ParseException | InterruptedException ex) {
            Logger.getLogger(TorrentSearch.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(TorrentSearch.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
