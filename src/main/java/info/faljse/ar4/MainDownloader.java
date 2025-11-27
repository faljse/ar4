package info.faljse.ar4;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.http.HttpClient;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j(topic = "MainDownloader")
public class MainDownloader {
    private final Path path;
    private final int concurrency;
    private final List<String> stations;
    private final int progress;
    private final LocalDate endDate;
    private final LocalDate startDate;
    DateTimeFormatter broadCastDateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");

    public MainDownloader(Path path, List<String> stations, int concurrency, int progress, LocalDate startDate, LocalDate endDate) {
        this.path=path;
        this.concurrency=concurrency;
        this.stations=stations;
        this.progress=progress;
        this.startDate=startDate;
        this.endDate=endDate;
    }

    public void download() throws InterruptedException, IOException {
        log.info("Download to {}", this.path);
        Files.createDirectories(this.path);
        List<StationDownloader> downloaders = downloadMetadata();
        downloadFiles(downloaders);
    }

    public List<StationDownloader> downloadMetadata() throws InterruptedException {
        try (var es = Executors.newVirtualThreadPerTaskExecutor()) {
            List<StationDownloader> stationDownloaders=new ArrayList<>();
            for(String station:stations) {
                for (LocalDate date = startDate; date.isEqual(endDate)||date.isBefore(endDate); date = date.plusDays(1)) {
                    String dateString=broadCastDateFormatter.format(date);
                    stationDownloaders.add(new StationDownloader(path.resolve(station),
                        String.format("https://audioapi.orf.at/%s/api/json/5.0/broadcasts/%s", station, dateString), HttpClient.newHttpClient()));
                }
            }
            CountDownLatch doneSignal = new CountDownLatch(stationDownloaders.size());
            for (var sDownloader : stationDownloaders) {
                log.info("Start metadata download for \"{}\"", sDownloader.getFolderName());
                es.submit(() -> { try{
                    sDownloader.downloadMetadata(doneSignal);
                } catch (Exception e) {
                    log.warn("Error downloading metadata for \"{}\"", sDownloader.getFolderName(), e);
                }});
            }
            doneSignal.await();
            return stationDownloaders;
        }
    }

    public void downloadFiles(List<StationDownloader> aDownloaders) throws InterruptedException {
        try (var es = Executors.newVirtualThreadPerTaskExecutor()) {
            for (var aDownloader : aDownloaders) {
                log.info("Start stream download for {}", aDownloader.getFolderName());
                es.submit(() -> aDownloader.downloadFiles(concurrency));
            }
            startStatsTimer(aDownloaders);
            es.shutdown();
            if(!es.awaitTermination(1, TimeUnit.DAYS)) {
                log.warn("Downloaders did not finish in time");
            }
        }
    }

    private void startStatsTimer(List<StationDownloader> dlers) {
        Timer t=new Timer("Stats", true);
        t.scheduleAtFixedRate(new TimerTask() {
            long lastBytes = 0;
            @Override
            public void run() {
                long bytes = 0;
                int filesTotal=0;
                int filesDone=0;
                StringBuilder stat= new StringBuilder();
                for (var dler : dlers) {
                    bytes += dler.bytesLoaded.get();
                    stat.append("\"");
                    stat.append(dler.getFolderName()).append("\":").append(dler.getPercent());
                    stat.append("%(");
                    stat.append(dler.getDownloadsDone()).append("/").append(dler.getDownloadsTotal());
                    stat.append(") ");
                    filesTotal+=dler.getDownloadsTotal();
                    filesDone+=dler.getDownloadsDone();
                }
                log.info("Progress: {}%({}/{}) {} {} kB/s", (int)((float)filesDone/filesTotal*100), filesDone,filesTotal, stat, (bytes - lastBytes) / (1024L * progress));
                lastBytes = bytes;
            }
        },1000L, progress* 1000L);
    }
}