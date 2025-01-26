package info.faljse;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import info.faljse.broadcast.Broadcast;
import info.faljse.broadcast.ResponseDetail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

public class PlaylistMain {

    public static void main(String[] args) {
        PlaylistMain m=new PlaylistMain();
        try {
            m.convert(Path.of("fm4"), "fm4");
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void convert(Path dir, String publisher) throws InterruptedException, IOException {
        var files=Files.newDirectoryStream(dir);
        Map<String, Playlist> plists=new HashMap<>();
        for(Path file:files) {
            System.out.println(file.getFileName());
            if(file.toString().endsWith(".json")) {
                convertFile(file, publisher, plists);
            }
        }
        for(Playlist p: plists.values()) {
            Path pldir=Path.of("playlists", publisher);
            Files.createDirectories(pldir);
            p.savem3u(pldir.resolve(p.title + ".m3u"));
        }
    }

    private void convertFile(Path file, String publisher, Map<String, Playlist> plists) throws IOException, InterruptedException {
        ObjectMapper mapper=new ObjectMapper();
        Broadcast broadcast =
                mapper.readValue(Files.readAllBytes(file), new TypeReference<ResponseDetail>() {}).getBroadcast();
        broadcast.mp3file=Path.of(file.toString().substring(0,file.toString().lastIndexOf(".json"))+"_0.mp3");

        var pl=plists.get(broadcast.getTitle());
        if(pl!=null) {
            pl.entries.add(broadcast);
        } else {
            plists.put(broadcast.getTitle(), new Playlist(broadcast.getTitle()));
        }
    }

    private static String normalize(String input) {
        return input == null ? null : Normalizer.normalize(input, Normalizer.Form.NFKD).replace(':','_');

    }
}