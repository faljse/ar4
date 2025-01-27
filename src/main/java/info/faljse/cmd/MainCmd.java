package info.faljse.cmd;

import picocli.CommandLine;

@CommandLine.Command(
        name = "ar4",
        subcommands = {
                ArchiveCmd.class,
                PlaylistCmd.class
        })
public class MainCmd  {

}