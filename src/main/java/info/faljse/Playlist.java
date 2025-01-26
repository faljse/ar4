package info.faljse;

import info.faljse.broadcast.Broadcast;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Playlist {

    Playlist(String title) {
        this.title=title;
    }

    public String title;
    public List<Broadcast> entries=new ArrayList<>();
    public String description;

    public void savem3u(Path filename) throws IOException {
        StringBuilder sb=new StringBuilder();
        sb.append("#EXTM3U\r\n");
        for(Broadcast b:entries) {


            if(!Files.exists(b.mp3file))
                continue;
            sb.append(String.format("#EXTINF:%d, %s (%s)\r\n", b.getDuration()/1000, b.getTitle(), b.getStart()));
            sb.append(String.format("%s\r\n", "../../"+b.mp3file.toString()));
        }
        Files.writeString(filename, sb.toString());

    }
}
