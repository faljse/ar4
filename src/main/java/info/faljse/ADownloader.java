package info.faljse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import info.faljse.broadcast.*;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

public class ADownloader {
    private final String broadcastsURL;
    private final String folderName;

    private final ExecutorService executorService;
    public AtomicLong bytesLoaded = new AtomicLong();

    public ADownloader(String folderName, String broadcastsURL, int threads) {
        this.broadcastsURL = broadcastsURL;
        this.folderName = folderName;
        this.executorService = Executors.newFixedThreadPool(threads);
    }


    public void download() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(broadcastsURL))
                    .build();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());
            var broadcasts = readJSON(response.body());
            for (var broadcastDay : broadcasts) {
                downloadBroadcastDay(broadcastDay);

            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void downloadBroadcastDay(BroadcastDay broadcastDay) {
        for (Broadcast bc : broadcastDay.getBroadcasts()) {
            downloadBroadcast(bc);
        }
    }

    private void downloadBroadcast(Broadcast bc) {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(bc.getHref()))
                .build();
        try {
            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());
            ObjectMapper objectMapper = new ObjectMapper();
            System.out.printf("Loading \"%s\"\n", bc.getHref());
            var broadcastDetail = objectMapper.readValue(response.body(), ResponseDetail.class).getBroadcast();
            executorService.submit(() -> dlStreamItem(broadcastDetail));
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void dlStreamItem(Broadcast broadcastDetail) {
        HttpClient client = HttpClient.newHttpClient();
        String dateStr = broadcastDetail.getStart();
        dateStr = dateStr.substring(0, dateStr.indexOf("."));
        String lastFileURL="";
        for (int i = 0; i < broadcastDetail.getStreams().size(); i++) {
            var stream=broadcastDetail.getStreams().get(i);
            String fileName = String.format("%s_%s_%d.mp3", dateStr, broadcastDetail.getId(), i);
            if (Paths.get(folderName, fileName).toFile().exists()) {
                System.out.printf("Skip %d\n", broadcastDetail.getId());
                return;
            }
            var streamURL=stream.getUrls().getProgressive();
            var fileURL=streamURL.substring(0,streamURL.lastIndexOf(".mp3")+".mp3".length());
            if(fileURL.equals(lastFileURL)) {
                System.out.printf("same url: %s\n", fileURL);
                continue;
            }
            lastFileURL=fileURL;
            dlFile(client, fileURL, fileName);
        }

        try (OutputStream jsonOut = new FileOutputStream(Paths.get(folderName, String.format("%s_%s.json", dateStr, broadcastDetail.getId())).toFile())) {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(jsonOut, broadcastDetail);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void dlFile(HttpClient client, String url, String fileName) {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();
        System.out.println(url);

        System.out.printf("Downloading %s\n", url);

        Path path = Paths.get(folderName, fileName + ".part");
        try (OutputStream outStream = new FileOutputStream(path.toFile())) {
            HttpResponse<InputStream> response =
                    client.send(request, HttpResponse.BodyHandlers.ofInputStream());

            byte[] buffer = new byte[8 * 1024];
            int bytesRead;
            int totalBytesRead = 0;
            int lastBytesRead = 0;
            while ((bytesRead = response.body().read(buffer)) != -1) {
                outStream.write(buffer, 0, bytesRead);
                this.bytesLoaded.getAndAdd(bytesRead);
                totalBytesRead += bytesRead;
                if (totalBytesRead - lastBytesRead > 1024 * 1024) {
                    lastBytesRead = totalBytesRead;
                }
            }

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            File fileToMove = path.toFile();
            fileToMove.renameTo(Paths.get(folderName, fileName).toFile());
        }

    }

    private static List<BroadcastDay> readJSON(String jsonData) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(jsonData, new TypeReference<Response>() {
        }).getPayload();
    }
}
