package info.faljse.cmd;

import info.faljse.MainDownloader;
import picocli.CommandLine;
import java.nio.file.Path;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "archive", mixinStandardHelpOptions = false, version = "ar4 1.0",
        description = "save streams to directory")
public class ArchiveCmd implements Callable<Integer> {

    @CommandLine.Parameters(paramLabel = "folder", description = "archive folder")
    Path archiveFolder;

    @CommandLine.Option(names = {"-c", "--concurrency"}, description = "Concurrent stream downloads per radio station.", defaultValue = "4", showDefaultValue= CommandLine.Help.Visibility.ALWAYS)
    int concurrency;

    @Override
    public Integer call() throws Exception {
        MainDownloader m=new MainDownloader(archiveFolder, concurrency);
        m.download();
        return 0;
    }


}