package info.faljse;

import java.net.http.HttpClient;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

public class Main {
    private static final int concurrency =8;

    public static void main(String[] args) {
        Main m=new Main();
        try {
            m.download();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void download() throws InterruptedException {


        HttpClient httpClient= HttpClient.newHttpClient();
        try (var es = Executors.newVirtualThreadPerTaskExecutor()) {
            var dlers = List.of(
                    new ADownloader("fm4",
                            "https://audioapi.orf.at/fm4/api/json/5.0/broadcasts/", httpClient,
                            concurrency),
                    new ADownloader("oe1",
                            "https://audioapi.orf.at/oe1/api/json/5.0/broadcasts/", httpClient,
                            concurrency),
                    new ADownloader("oe3",
                            "https://audioapi.orf.at/oe3/api/json/5.0/broadcasts/", httpClient,
                            concurrency),
                    new ADownloader("wie",
                            "https://audioapi.orf.at/wie/api/json/5.0/broadcasts/", httpClient,
                            concurrency)
            );
            CountDownLatch doneSignal = new CountDownLatch(dlers.size());
            for (var dler : dlers) {
                es.submit(() -> dler.download(doneSignal));
            }
            startStatsTimer(dlers);
            doneSignal.await();
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
                    stat += dler.getPermits() + " ";
                }
                System.out.printf(stat +"%d kB/s\n", (bytes - lastBytes) / 1024);
                lastBytes = bytes;
            }
        },0,500);
    }
}