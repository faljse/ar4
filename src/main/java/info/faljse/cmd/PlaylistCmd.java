package info.faljse.cmd;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import info.faljse.Playlist;
import info.faljse.broadcast.Broadcast;
import info.faljse.broadcast.ResponseDetail;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "playlist", mixinStandardHelpOptions = false, version = "ar4 1.0",
        description = "create m3u playlist from metadata")
public class PlaylistCmd implements Callable<Integer> {

    @CommandLine.Parameters(paramLabel = "archiveDir", description = "archive folder")
    Path archiveDir;

    @CommandLine.Parameters(paramLabel = "playlistDir", description = "playlist folder")
    Path playlistDir;

    @CommandLine.Option(names = {"-a", "--absolute"}, description = "Create m3u file with absolute paths.", defaultValue = "false")
    boolean absolute;

    @Override
    public Integer call() throws Exception {
        convert(archiveDir, playlistDir);
        return 0;
    }

    private void convert(Path archiveDir, Path playlistDir) throws InterruptedException, IOException {
        var files= Files.newDirectoryStream(archiveDir);
        Map<String, Playlist> plists=new HashMap<>();
        for(Path file:files) {
            System.out.println(file.getFileName());
            if(file.toString().endsWith(".json")) {
                sortIntoPlaylists(file, plists);
            }
        }
        for(Playlist p: plists.values()) {
            Files.createDirectories(playlistDir);
            p.savem3u(playlistDir.resolve(p.title + ".m3u"), absolute);
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