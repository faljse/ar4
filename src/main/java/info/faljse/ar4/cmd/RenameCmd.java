package info.faljse.ar4.cmd;

import info.faljse.ar4.StationDownloader;
import info.faljse.ar4.StationRenamer;
import picocli.CommandLine;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "rename", version = "ar4 1.0",
        description = "rename old new")
public class RenameCmd implements Callable<Integer> {
    @CommandLine.Parameters(paramLabel = "folder", description = "archive folder")
    Path folder;

    @Override
    public Integer call() throws Exception {
        StationRenamer s=new StationRenamer(folder);
        s.renameFiles();
        return 0;
    }
}