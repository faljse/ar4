package info.faljse;

import java.net.http.HttpClient;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

public class Main {


    public static void main(String[] args) {
        Main m=new Main();
        try {
            var dlers=m.downloadMetadata();
            m.downloadFiles(dlers);
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
            startStatsTimer(aDownloaders);
            doneSignal.await();
            return aDownloaders;
        }
    }

    public void downloadFiles(List<ADownloader> downloaders) throws InterruptedException {
        try (var es = Executors.newVirtualThreadPerTaskExecutor()) {
            for(var aDownloader: downloaders) {
                es.submit(()->{
                    aDownloader.downloadFiles(4);
                });

            }
        }
    }



    private void startStatsTimer(List<ADownloader> dlers) {
        Timer t=new Timer("Stats");
        t.scheduleAtFixedRate(new TimerTask() {
            long lastBytes = 0;
            @Override
            public void run() {
                long bytes = 0;
                String stat="";
                for (var dler : dlers) {
                    bytes += dler.bytesLoaded.get();
                }
                System.out.printf(stat +"%d kB/s\n", (bytes - lastBytes) / 1024);
                lastBytes = bytes;
            }
        },0,500);
    }


}