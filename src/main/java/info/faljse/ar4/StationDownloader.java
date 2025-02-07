package info.faljse.ar4;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import info.faljse.ar4.broadcast.*;
import lombok.AllArgsConstructor;
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
import static java.nio.file.StandardCopyOption.*;

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
            log.warn("Error download metadata", e);
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
                        downloadFile(fd.url, fd.path);
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

    private void downloadFile(String url, Path finalPath) {
        Path partPath= finalPath.getParent().resolve(finalPath.getFileName().toString()+".part");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();
        log.debug("Download  \"{}\" ({})", finalPath.getFileName().toString(), url);
        int totalBytesRead = 0;
        long contentLength = -1;
        try (OutputStream outStream = new FileOutputStream(partPath.toFile())) {
            HttpResponse<InputStream> response =
                    client.send(request, HttpResponse.BodyHandlers.ofInputStream());
            var clengthOptional = response.headers().firstValueAsLong("content-length");
            if(clengthOptional.isPresent())
                contentLength = clengthOptional.getAsLong();
            byte[] buffer = new byte[64 * 1024];
            int bytesRead;
            try(var bodyStream=response.body()) {
                for(;;) {
                    bytesRead = bodyStream.read(buffer);
                    if (bytesRead == -1) {
                        break;
                    }
                    outStream.write(buffer, 0, bytesRead);
                    this.bytesLoaded.getAndAdd(bytesRead);
                    totalBytesRead += bytesRead;
                }
            }

        } catch (IOException e) {
            if(contentLength > 0 &&
                    totalBytesRead > 0 &&
                    contentLength != totalBytesRead &&
                    Math.abs(contentLength-totalBytesRead) < 4096){ //Allow the file to be 4k off.
                log.warn("Ignoring wrong content-length finishing download \"{}\" as \"{}\": {} bytes received instead of {}", url, finalPath.getFileName(), totalBytesRead, contentLength);
            }
            else throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        try {
            log.debug("Move \"{}\" \"{}\"", partPath, finalPath.getFileName());
            Files.move(partPath, finalPath);
            log.info("Done \"{}\" ({})", finalPath.getFileName(), url);
        } catch (IOException e) {
            log.warn("Error moving file", e);
        }
    }

    private void downloadBroadcastDay(BroadcastDay broadcastDay) throws IOException {
        for (Broadcast bc : broadcastDay.getBroadcasts()) {
            downloadBroadcast(bc);
        }
    }

    private void downloadBroadcast(Broadcast bc) throws IOException {
        String detailUri=bc.getHref()+"?items=true";
        Path jsonPath=folder.resolve(String.format("%d.json", bc.getId()));
        downloadFile(detailUri, jsonPath);
        Broadcast broadcastDetail = new ObjectMapper().readValue(jsonPath.toFile(), ResponseDetail.class).getBroadcast();
        for(int i=0;i<broadcastDetail.getImages().size();i++) {
            ImagesItem image=broadcastDetail.getImages().get(i);
            queueImage(image, i, broadcastDetail);
        }
        for(var item:broadcastDetail.getItems()) {
            queueItemImage(item, broadcastDetail);
        }
        queueStreamItems(broadcastDetail);
    }

    private void queueItemImage(ItemsItem item, Broadcast broadcastDetail) {
        for(int i=0;i<item.getImages().size();i++) {
            var image=item.getImages().get(i);
            for(var ver:image.getVersions()) {
                Path imageFilePath = folder.resolve(String.format("%d_item_%d_%d_%d.jpg", broadcastDetail.getId(), item.getId(), i, ver.getWidth()));
                queueDownload(ver.getPath(), imageFilePath);
            }
        }
    }

    private void queueImage(ImagesItem image, int i, Broadcast broadcastDetail) {
        for(var version: image.getVersions()) {
            Path imageFilePath = folder.resolve(String.format("%d_%d_%d.jpg", broadcastDetail.getId(), i, version.getWidth()));
                queueDownload(version.getPath(), imageFilePath);
        }
    }

    private void queueStreamItems(Broadcast broadcastDetail) {
        List<String> fileURLs=new ArrayList<>();
        for (int i = 0; i < broadcastDetail.getStreams().size(); i++) {
            StreamsItem stream=broadcastDetail.getStreams().get(i);
            String fileName = String.format("%d_%d.mp3", broadcastDetail.getId(), i);
            String streamURL=stream.getUrls().getProgressive();
            streamURL = streamURL.substring(0,streamURL.lastIndexOf(".mp3")+".mp3".length()); //cut everything after .mp3
            if(i > 0 && fileURLs.contains(streamURL)) {
                log.debug("Skip (Same URL): \"{}\" ({})", fileName, streamURL);
            } else {
                queueDownload(streamURL, folder.resolve(fileName));
            }
            fileURLs.add(streamURL);
        }
    }

    private void queueDownload(String url, Path path) {
        if(Files.exists(path)) {
            log.debug("Skip (File exists): \"{}\"", path);
        } else {
            log.info("Queue \"{}\" ({})", path.getFileName(), url);
            this.fileDownloadList.add(new FileDownload(url, path));
        }
    }

    private static List<BroadcastDay> readJSON(String jsonData) throws JsonProcessingException {
        return new ObjectMapper()
            .readValue(jsonData, new TypeReference<Response>() {})
            .getPayload();
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

    @AllArgsConstructor
    public static class FileDownload {
        public String url;
        public final Path path;
    }
}
