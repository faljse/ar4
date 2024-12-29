package info.faljse;

import java.util.List;
import java.util.concurrent.Executors;

public class Main {
    private static int threadCount=10;

    public static void main(String[] args) {
        Main m=new Main();
        try {
            m.download();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void download() throws InterruptedException {
        var es=Executors.newFixedThreadPool(4);
        var dlers=List.of(new ADownloader("fm4",
            "https://audioapi.orf.at/fm4/api/json/5.0/broadcasts/",
                threadCount),
        new ADownloader("oe1",
                "https://audioapi.orf.at/oe1/api/json/5.0/broadcasts/",
                threadCount),
        new ADownloader("oe3",
                "https://audioapi.orf.at/oe3/api/json/5.0/broadcasts/",
                threadCount),
        new ADownloader("wie",
                "https://audioapi.orf.at/wie/api/json/5.0/broadcasts/",
                threadCount)
        );

        for(var dler:dlers) {
           es.submit(dler::download);
        }
        long lastBytes=0;
        while (!es.isTerminated()) {
            long bytes=0;
            for(var dler:dlers) {
                bytes+=dler.bytesLoaded.get();
            }
            System.out.printf("%d kB/s\n", (bytes-lastBytes)/1024);
            lastBytes=bytes;
            Thread.sleep(1000);
        }
    }
}