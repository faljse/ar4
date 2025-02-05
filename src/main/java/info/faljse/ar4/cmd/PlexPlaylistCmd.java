package info.faljse.ar4.cmd;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import info.faljse.ar4.Playlist;
import info.faljse.ar4.broadcast.Broadcast;
import info.faljse.ar4.broadcast.ResponseDetail;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "plexplaylist", mixinStandardHelpOptions = false, version = "ar4 1.0",
        description = "create playlists in plex")
public class PlexPlaylistCmd implements Callable<Integer> {

    @CommandLine.Parameters(paramLabel = "archiveDir", description = "archive folder")
    Path archiveDir;

    @CommandLine.Option(names = {"-i", "--ip"}, description = "plex ip", defaultValue = "127.0.0.1", showDefaultValue= CommandLine.Help.Visibility.ALWAYS)
    String ip;

    @CommandLine.Option(names = {"-p", "--port"}, description = "plex port", defaultValue = "32400", showDefaultValue= CommandLine.Help.Visibility.ALWAYS)
    String port;

    @CommandLine.Option(names = {"-a", "--authtoken"}, description = "plex auth token", defaultValue = "", showDefaultValue= CommandLine.Help.Visibility.ALWAYS)
    String authToken;


    @Override
    public Integer call() throws Exception {
        var plists=buildPlaylist();
        exportM3U(plists);
        return 0;
    }

    private Map<String, Playlist> buildPlaylist() throws InterruptedException, IOException {
        var files= Files.newDirectoryStream(archiveDir);
        Map<String, Playlist> plists=new HashMap<>();
        for(Path file:files) {
            if(file.toString().endsWith(".json")) {
                sortIntoPlaylists(file, plists);
            }
        }
        return plists;
    }

    private void exportM3U(Map<String, Playlist> plists) throws Exception {
        for(Playlist p: plists.values()) {
            p.exportPlex(ip, port, authToken);
        }
    }

    private void sortIntoPlaylists(Path file, Map<String, Playlist> playlists) throws IOException, InterruptedException {
        ObjectMapper mapper=new ObjectMapper();
        Broadcast broadcast =
                mapper.readValue(Files.readAllBytes(file), new TypeReference<ResponseDetail>() {}).getBroadcast();
        broadcast.mp3file=Path.of(file.toString().substring(0,file.toString().lastIndexOf(".json"))+"_0.mp3");
        Playlist playlist= playlists.get(broadcast.getTitle());
        if(playlist!=null) {
            playlist.entries.add(broadcast);
        } else {
            playlists.put(broadcast.getTitle(), new Playlist(broadcast.getTitle()));
        }
    }

    private static String normalize(String input) {
        return input == null ? null : Normalizer.normalize(input, Normalizer.Form.NFKD).replace(':','_');
    }
}