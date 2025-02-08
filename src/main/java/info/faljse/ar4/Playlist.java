package info.faljse.ar4;

import dev.plexapi.sdk.PlexAPI;
import dev.plexapi.sdk.models.operations.*;
import info.faljse.ar4.broadcast.Broadcast;

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

    public void saveM3U(Path filename, boolean absolute) throws IOException {
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

    public void exportPlex(String ip, String port, String accessToken) throws Exception {
        PlexAPI sdk=plexLogin(ip, port, accessToken);
        CreatePlaylistRequest req = CreatePlaylistRequest.builder()
                .title("asdf")
                .type(CreatePlaylistQueryParamType.AUDIO)
                .smart(Smart.ZERO)
                .uri("https://hoarse-testing.info/")
                .build();

        CreatePlaylistResponse res = sdk.playlists().createPlaylist()
                .request(req)
                .call();
        if (res.object().isPresent()) {
            res.toString();
            // handle response
        }
    }

    private PlexAPI plexLogin(String ip, String port, String accessToken) throws Exception {
        PlexAPI sdk = PlexAPI.builder().ip(ip)
                .port(port)
                .protocol(PlexAPI.Builder.ServerProtocol.HTTP)
                .accessToken(accessToken)
                .build();

        GetServerCapabilitiesResponse res = sdk.server().getServerCapabilities()
                .call();

        if (res.object().isPresent()) {
            res.toString();
            // handle response
        }
        return sdk;
    }
}
