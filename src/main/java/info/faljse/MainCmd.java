package info.faljse;

import picocli.CommandLine;

@CommandLine.Command(
        name = "ar4",
        subcommands = {
                ArchiveCmd.class,
        })
public class MainCmd  {

}