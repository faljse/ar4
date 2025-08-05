package info.faljse.ar4.cmd;

import picocli.CommandLine;

@CommandLine.Command(
        name = "ar4",
        subcommands = {
                ArchiveCmd.class,
                PlaylistCmd.class,
                PlexPlaylistCmd.class,
                RenameCmd.class
        })
public class MainCmd  {

}