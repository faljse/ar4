package info.faljse.ar4.cmd;

import dev.plexapi.sdk.PlexAPI;
import dev.plexapi.sdk.models.operations.*;
import picocli.CommandLine;
import java.nio.file.Path;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "plexplaylist", mixinStandardHelpOptions = false, version = "ar4 1.0",
        description = "create playlists in plex")
public class PlexPlaylistCmd implements Callable<Integer> {

    @CommandLine.Parameters(paramLabel = "m3uFile", description = "m3u file")
    Path m3uFile;

    @CommandLine.Option(names = {"-i", "--ip"}, description = "plex ip", defaultValue = "127.0.0.1", showDefaultValue= CommandLine.Help.Visibility.ALWAYS)
    String ip;

    @CommandLine.Option(names = {"-p", "--port"}, description = "plex port", defaultValue = "32400", showDefaultValue= CommandLine.Help.Visibility.ALWAYS)
    String port;

    @CommandLine.Option(names = {"-a", "--authtoken"}, description = "plex auth token", defaultValue = "", showDefaultValue= CommandLine.Help.Visibility.ALWAYS)
    String authToken;


    @Override
    public Integer call() throws Exception {
        try {
            exportPlex(ip, port, authToken, m3uFile);
        }catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    public void exportPlex(String ip, String port, String accessToken, Path m3uFile) throws Exception {
        PlexAPI sdk=plexLogin(ip, port, accessToken);
        UploadPlaylistResponse res = sdk.playlists().uploadPlaylist()
                .path(m3uFile.toAbsolutePath().toString())
                .force(QueryParamForce.ZERO)
                .sectionID(11L)
                .call();

        System.out.println(res.toString());
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