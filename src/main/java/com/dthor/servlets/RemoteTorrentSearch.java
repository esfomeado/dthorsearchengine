package com.dthor.servlets;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.lucene.queryparser.classic.ParseException;
import com.dthor.LuceneServer;

public class RemoteTorrentSearch extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        LuceneServer searchEngine = LuceneServer.getInstance();

        String query = request.getParameter("query");

        try {
            String keys = searchEngine.search(query);
            response.getOutputStream().print(keys);
            response.flushBuffer();

        } catch (ParseException | InterruptedException ex) {
            Logger.getLogger(RemoteTorrentSearch.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
