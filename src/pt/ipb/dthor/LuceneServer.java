package pt.ipb.dthor;

import java.io.File;
import java.io.IOException;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import pt.ipb.dthor.torrent.DThorTorrent;
import pt.ipb.dthor.torrent.TorrentDoc;

public class LuceneServer {

    private static LuceneServer instance = null;
    private final String indexDir = "./lucene";
    private StandardAnalyzer analyzer;

    public static LuceneServer getInstance() {
        if(instance == null) {
            instance = new LuceneServer();
        }
        return instance;
    }

    private LuceneServer() {
        analyzer = new StandardAnalyzer(Version.LUCENE_4_9);
    }

    public TorrentDoc indexTorrent(DThorTorrent torrent, String key) {
        TorrentDoc torrentDoc = new TorrentDoc(key, torrent.getSaveAs(), torrent.getFiles());
        return torrentDoc;
    }

    public void indexer(TorrentDoc torrent) throws IOException {
        IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_4_9, analyzer);
        IndexWriter indexWriter = new IndexWriter(FSDirectory.open(new File(indexDir)), config);

        Document doc = new Document();
        doc.add(new StringField(TorrentDoc.ID, torrent.getId(), Field.Store.YES));
        doc.add(new TextField(TorrentDoc.TITLE, torrent.getTitle(), Field.Store.YES));

        for(int i = 0; i < torrent.getFiles().size(); i++) {
            doc.add(new TextField(TorrentDoc.FILES, torrent.getFiles().get(i), Field.Store.YES));
        }
        indexWriter.updateDocument(new Term("id", torrent.getId()), doc);
        indexWriter.close();
    }

    public String search(String query) throws IOException, ParseException, InterruptedException {
        IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(indexDir)));
        IndexSearcher searcher = new IndexSearcher(reader);

        QueryParser titleParser = new QueryParser(Version.LUCENE_4_9, "title", analyzer);
        QueryParser fileParser = new QueryParser(Version.LUCENE_4_9, "files", analyzer);

        BooleanQuery q = new BooleanQuery();
        q.add(titleParser.parse(query), BooleanClause.Occur.SHOULD);
        q.add(fileParser.parse(query), BooleanClause.Occur.SHOULD);

        TopDocs topDocs = searcher.search(q, 10);

        JSONArray results = new JSONArray();
        System.out.println("Found " + topDocs.totalHits + " hits");
        for(int i = 0; i < topDocs.totalHits; i++) {
            JSONObject result = new JSONObject();
            result.put("key", searcher.doc(topDocs.scoreDocs[i].doc).get(TorrentDoc.ID));
            result.put("title", searcher.doc(topDocs.scoreDocs[i].doc).get(TorrentDoc.TITLE));
            results.add(result);
        }

        return results.toJSONString();
    }

    public void delete(String key) throws IOException {
        IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_4_9, analyzer);
        IndexWriter indexWriter = new IndexWriter(FSDirectory.open(new File(indexDir)), config);
        
        indexWriter.deleteDocuments(new Term("id", key));
        indexWriter.close();
    }
}
