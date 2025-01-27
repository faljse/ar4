package info.faljse;

import java.io.IOException;
import java.net.http.HttpClient;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MainDownloader {
    private final Path path;
    private final int concurrency;
    private final List<String> stations;

    public MainDownloader(Path path, List<String> stations, int concurrency) {
        this.path=path;
        this.concurrency=concurrency;
        this.stations=stations;
    }

    public void download() throws InterruptedException, IOException {
        Files.createDirectories(this.path);
        var downloaders = downloadMetadata();
        downloadFiles(downloaders);
    }



    public List<StationDownloader> downloadMetadata() throws InterruptedException {
        try (var es = Executors.newVirtualThreadPerTaskExecutor()) {
            List<StationDownloader> stationDownloaders=new ArrayList<>();
            for(String station:stations) {
                stationDownloaders.add(new StationDownloader(path.resolve(station),
                        "https://audioapi.orf.at/" + station + "/api/json/5.0/broadcasts/", HttpClient.newHttpClient()));
            }
            CountDownLatch doneSignal = new CountDownLatch(stationDownloaders.size());
            for (var sDownloader : stationDownloaders) {
                es.submit(() -> sDownloader.downloadMetadata(doneSignal));
            }
            doneSignal.await();
            return stationDownloaders;
        }
    }

    public void downloadFiles(List<StationDownloader> aDownloaders) throws InterruptedException {
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

    private void startStatsTimer(List<StationDownloader> dlers) {
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