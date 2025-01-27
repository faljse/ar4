package info.faljse;

import info.faljse.broadcast.Broadcast;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Playlist {

    public Playlist(String title) {
        this.title=title;
    }

    public String title;
    public List<Broadcast> entries=new ArrayList<>();
    public String description;

    public void savem3u(Path filename, boolean absolute) throws IOException {
        StringBuilder sb=new StringBuilder();
        sb.append("#EXTM3U\r\n");
        for(Broadcast b:entries) {


            if(!Files.exists(b.mp3file))
                continue;
            sb.append(String.format("#EXTINF:%d, %s (%s)\r\n", b.getDuration()/1000, b.getTitle(), b.getStart()));
            String relaPath=filename.getParent().relativize(b.mp3file).toString();
            if(absolute)
                sb.append(String.format("%s\r\n",  b.mp3file.toAbsolutePath()));
            else
                sb.append(String.format("%s\r\n", relaPath));
        }
        Files.writeString(filename, sb.toString());

    }
}
