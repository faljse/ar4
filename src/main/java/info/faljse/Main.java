package info.faljse;

import java.net.http.HttpClient;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {


    public static void main(String[] args) {
        Main m=new Main();
        try {
            var downloaders=m.downloadMetadata();
            m.downloadFiles(downloaders);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    public List<ADownloader> downloadMetadata() throws InterruptedException {
        HttpClient httpClient= HttpClient.newHttpClient();
        try (var es = Executors.newVirtualThreadPerTaskExecutor()) {
            var aDownloaders = List.of(
                    new ADownloader("fm4",
                            "https://audioapi.orf.at/fm4/api/json/5.0/broadcasts/", httpClient),
                    new ADownloader("oe1",
                            "https://audioapi.orf.at/oe1/api/json/5.0/broadcasts/", httpClient),
                    new ADownloader("oe3",
                            "https://audioapi.orf.at/oe3/api/json/5.0/broadcasts/", httpClient),
                    new ADownloader("wie",
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

    public void downloadFiles(List<ADownloader> aDownloaders) throws InterruptedException {
        try (var es = Executors.newVirtualThreadPerTaskExecutor()) {
            for (var aDownloader : aDownloaders) {
                es.submit(() -> aDownloader.downloadFiles(4));
            }
            startStatsTimer(aDownloaders);
            es.shutdown();
            es.awaitTermination(1,TimeUnit.DAYS);
        }
    }

    private void startStatsTimer(List<ADownloader> dlers) {
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