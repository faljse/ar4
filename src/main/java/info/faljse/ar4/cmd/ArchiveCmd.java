package info.faljse.ar4.cmd;

import info.faljse.ar4.MainDownloader;
import picocli.CommandLine;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "archive", version = "ar4 1.0",
        description = "save streams to directory")
public class ArchiveCmd implements Callable<Integer> {
    private final static String[] allStations = {"fm4", "oe1", "oe3", "wie", "bgl", "ktn", "noe", "ooe", "sbg", "stm", "tir", "vbg"};

    @CommandLine.Parameters(paramLabel = "folder", description = "archive folder")
    Path archiveFolder;

    @CommandLine.Option(names = {"-c", "--concurrency"},
                        description = "Concurrent stream downloads per radio station.",
                        defaultValue = "4",
                        showDefaultValue = CommandLine.Help.Visibility.ALWAYS)
    int concurrency;

    @CommandLine.Option(names = {"-s", "--stations"},
                        split = "\\,",
                        splitSynopsisLabel = ",",
                        description = "Possible values: fm4,oe1,oe3,wie,bgl,ktn,noe,ooe,sbg,stm,tir,vbg",
                        paramLabel = "stations",
                        defaultValue = "fm4,oe1,oe3,wie",
                        showDefaultValue = CommandLine.Help.Visibility.ALWAYS)
    List<String> stations;

    @Override
    public Integer call() throws Exception {
        MainDownloader m = new MainDownloader(archiveFolder, stations, concurrency);
        m.download();
        return 0;
    }
}