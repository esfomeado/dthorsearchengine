package com.dthor.torrent;

import java.util.List;

public class TorrentDoc {

    private final String id;
    private final String title;
    private final List<String> files;

    public static final String ID = "id";
    public static final String TITLE = "title";
    public static final String FILES = "files";

    public TorrentDoc(String id, String title, List<String> files) {
        this.id = id;
        this.title = title;
        this.files = files;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public List<String> getFiles() {
        return files;
    }
}
