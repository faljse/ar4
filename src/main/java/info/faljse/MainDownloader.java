package info.faljse;

import java.io.IOException;
import java.net.http.HttpClient;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MainDownloader {
    private final Path path;
    private final int concurrency;

    public MainDownloader(Path path, int concurrency) {
        this.path=path;
        this.concurrency=concurrency;
    }

    public void download() throws InterruptedException, IOException {
        Files.createDirectories(this.path);
        var downloaders = downloadMetadata();
        downloadFiles(downloaders);
    }

    public List<BroadcastDownloader> downloadMetadata() throws InterruptedException {
        HttpClient httpClient= HttpClient.newHttpClient();
        try (var es = Executors.newVirtualThreadPerTaskExecutor()) {
            var aDownloaders = List.of(
                    new BroadcastDownloader(path.resolve("fm4"),
                            "https://audioapi.orf.at/fm4/api/json/5.0/broadcasts/", httpClient),
                    new BroadcastDownloader(path.resolve("oe1"),
                            "https://audioapi.orf.at/oe1/api/json/5.0/broadcasts/", httpClient),
                    new BroadcastDownloader(path.resolve("oe3"),
                            "https://audioapi.orf.at/oe3/api/json/5.0/broadcasts/", httpClient),
                    new BroadcastDownloader(path.resolve("wie"),
                            "https://audioapi.orf.at/wie/api/json/5.0/broadcasts/", httpClient)
            );
            CountDownLatch doneSignal = new CountDownLatch(aDownloaders.size());
            for (var aDownloader : aDownloaders) {
                es.submit(() -> aDownloader.downloadMetadata(doneSignal));
            }
            doneSignal.await();
            return aDownloaders;
        }
    }

    public void downloadFiles(List<BroadcastDownloader> aDownloaders) throws InterruptedException {
        try (var es = Executors.newVirtualThreadPerTaskExecutor()) {
            for (var aDownloader : aDownloaders) {
                es.submit(() -> aDownloader.downloadFiles(concurrency));
            }
            startStatsTimer(aDownloaders);
            es.shutdown();
            boolean termRes=es.awaitTermination(1, TimeUnit.DAYS);
            System.out.println(termRes);
        }
    }

    private void startStatsTimer(List<BroadcastDownloader> dlers) {
        Timer t=new Timer("Stats", true);
        t.scheduleAtFixedRate(new TimerTask() {
            long lastBytes = 0;
            @Override
            public void run() {
                long bytes = 0;
                String stat="";
                for (var dler : dlers) {
                    bytes += dler.bytesLoaded.get();
                    stat += dler.getFolderName()+"("+ dler.getPercent()+"%) ";
                }
                System.out.printf("Downloading %s %d kB/s\n", stat, (bytes - lastBytes) / 1024);
                lastBytes = bytes;
            }
        },0,1000);
    }
}