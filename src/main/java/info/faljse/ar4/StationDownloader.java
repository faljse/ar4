package info.faljse.ar4;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import info.faljse.ar4.broadcast.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;


import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j(topic = "StationDownloader")
public class StationDownloader {
    private final String broadcastsURL;
    private final Path folder;

    private final HttpClient client;
    public AtomicLong bytesLoaded = new AtomicLong();
    private final List<FileDownload> fileDownloadList=new ArrayList<>();

    @Getter
    private int downloadsDone=0;

    public StationDownloader(Path folder, String broadcastsURL, HttpClient client) {
        this.broadcastsURL = broadcastsURL;
        this.folder = folder;
        this.client=client;
    }

    public void downloadMetadata(CountDownLatch doneSignal) {
        try {
            Files.createDirectories(this.folder);
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
            log.warn("Error downloading metadata", e);
        } finally {
            doneSignal.countDown();
        }
    }

    public void downloadFiles(int concurrency)  {
        Semaphore s=new Semaphore(concurrency);
        try(var es= Executors.newVirtualThreadPerTaskExecutor()) {
            for (var fd : fileDownloadList) {
                es.submit(() -> {
                    try {
                        s.acquire();
                        downloadFile(fd.url, fd.fileName);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    } finally {
                        this.downloadsDone += 1;
                        s.release();
                    }
                });
            }
            es.shutdown();
            if(!es.awaitTermination(1, TimeUnit.DAYS)) {
                log.warn("Downloaders did not finish in time");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void downloadFile(String url, String fileName) {
        Path partPath= folder.resolve(fileName + ".part");
        Path finalPath=folder.resolve(fileName);
        if(Files.exists(finalPath)) {
            log.info("Skip (file exists) \"{}\" ({})", finalPath, url);
        }
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();
        log.info("Download  \"{}\" ({})", fileName, url);

        int totalBytesRead = 0;
        long contentLength = -1;
        try (OutputStream outStream = new FileOutputStream(partPath.toFile())) {
            HttpResponse<InputStream> response =
                    client.send(request, HttpResponse.BodyHandlers.ofInputStream());
            contentLength = response.headers().firstValueAsLong("content-length").getAsLong();
            byte[] buffer = new byte[8 * 1024];
            int bytesRead;
            int lastBytesRead = 0;
            while ((bytesRead = response.body().read(buffer)) != -1) {
                outStream.write(buffer, 0, bytesRead);
                this.bytesLoaded.getAndAdd(bytesRead);
                totalBytesRead += bytesRead;
                if (totalBytesRead - lastBytesRead > 1024 * 1024) {
                    lastBytesRead = totalBytesRead;
                }
            }
            log.info("Done \"{}\" ({})", fileName, url);
        } catch (IOException e) {
            if(contentLength > 0 &&
                    totalBytesRead > 0 &&
                    contentLength != totalBytesRead &&
                    Math.abs(contentLength-totalBytesRead) < 4096){ //Allow the file to be 4k off.
                log.warn("Ignoring wrong content-length finishing download \"{}\" as \"{}\": {} bytes received instead of {}", url, fileName, totalBytesRead, contentLength);
            }
            else throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        try {
            Files.move(partPath, finalPath);
        } catch (IOException e) {
            log.warn("Error moving file", e);
        }
    }

    private void downloadBroadcastDay(BroadcastDay broadcastDay) throws JsonProcessingException {
        for (Broadcast bc : broadcastDay.getBroadcasts()) {
            downloadBroadcast(bc);
        }
    }

    private void downloadBroadcast(Broadcast bc) throws JsonProcessingException {
        String detailUri=bc.getHref()+"?items=true";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(detailUri))
                .build();
        HttpResponse<String> response;
        try {
            log.info("Downloading broadcast detail ({})", detailUri);
            response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        ObjectMapper objectMapper = new ObjectMapper();

        String origDetailJSON=response.body();
        Broadcast broadcastDetail = objectMapper.readValue(origDetailJSON, ResponseDetail.class).getBroadcast();
        for(int i=0;i<broadcastDetail.getImages().size();i++) {
            var image=broadcastDetail.getImages().get(i);
            try {
                dlImage(image, i, broadcastDetail);
            }
            catch (Exception e) {
                log.warn("Error downloading image", e);
            }
        }
        for(var item:broadcastDetail.getItems()) {
            try {
                dlItemImage(item, broadcastDetail);
            }
            catch (Exception e) {
                log.warn("Error downloading item image", e);
            }
        }
        dlStreamItem(broadcastDetail, origDetailJSON);
    }

    private void dlItemImage(ItemsItem item, Broadcast broadcastDetail) throws IOException, InterruptedException {
        for(int i=0;i<item.getImages().size();i++) {
            var image=item.getImages().get(i);
            for(var ver:image.getVersions()) {
                Path imageFilePath = folder.resolve(String.format("%d_item_%d_%d_%d.jpg", broadcastDetail.getId(), item.getId(), i, ver.getWidth()));
                if(Files.exists(imageFilePath)) {
                    continue;
                }
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(ver.getPath()))
                        .build();

                HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
                if (response.statusCode() == 200) {
                    byte[] responseBody = response.body();
                    Files.write(imageFilePath, responseBody);
                }
            }
        }
    }

    private void dlImage(ImagesItem image, int i, Broadcast broadcastDetail) throws IOException, InterruptedException {
        for(var version: image.getVersions()) {
            Path imageFilePath = folder.resolve(String.format("%d_%d_%d.jpg", broadcastDetail.getId(), i, version.getWidth()));
            if(Files.exists(imageFilePath)) {
                continue;
            }
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(version.getPath()))
                    .build();
            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() == 200) {
                byte[] responseBody = response.body();
                Files.write(imageFilePath, responseBody);
            }
        }
    }

    private void dlStreamItem(Broadcast broadcastDetail, String origDetailJSON) {
        File jsonOutFile=folder.resolve(String.format("%d.json", broadcastDetail.getId())).toFile();
        if(!jsonOutFile.exists()) {
            log.info("Done \"{}\"", jsonOutFile);
            try (var writer = new BufferedWriter(new FileWriter(jsonOutFile))) {
                writer.write(origDetailJSON);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            log.info("Skip (File exists): \"{}\"", jsonOutFile);
        }

        String lastStreamURL="";
        for (int i = 0; i < broadcastDetail.getStreams().size(); i++) {
            StreamsItem stream=broadcastDetail.getStreams().get(i);
            String fileName = String.format("%d_%d.mp3", broadcastDetail.getId(), i);
            String streamURL=stream.getUrls().getProgressive();
            streamURL=streamURL.substring(0,streamURL.lastIndexOf(".mp3")+".mp3".length()); //cut everything after .mp3
            if (folder.resolve(fileName).toFile().exists()) {
                log.info("Skip (File exists): \"{}\" ({})", fileName, streamURL);
                continue;
            }

            if(streamURL.equals(lastStreamURL)) {
                log.info("Skip (Same URL): \"{}\" ({})", fileName, streamURL);
                continue;
            }
            lastStreamURL=streamURL;
            this.fileDownloadList.add(new FileDownload(streamURL, fileName));
        }
    }

    private static List<BroadcastDay> readJSON(String jsonData) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(jsonData, new TypeReference<Response>() {
        }).getPayload();
    }

    public int getPercent() {
        return (int) (((float)this.downloadsDone/(float)this.fileDownloadList.size())*100);
    }
    public int getDownloadsTotal() {
        return this.fileDownloadList.size();
    }


    public String getFolderName() {
        return folder.toString();
    }

    public static class FileDownload {

        public String url;
        public final String fileName;

        FileDownload(String url, String fileName) {
            this.url=url;
            this.fileName=fileName;
        }
    }
}
