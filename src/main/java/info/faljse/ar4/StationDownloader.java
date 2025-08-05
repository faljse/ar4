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
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalLong;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static info.faljse.ar4.UpdateStrategy.ReplaceIfBigger;
import static info.faljse.ar4.UpdateStrategy.SkipExisting;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

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

    public void downloadMetadata(CountDownLatch doneSignal) throws InterruptedException, IOException {
        Files.createDirectories(this.folder);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(broadcastsURL))
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());
        var broadcasts = readJSON(response.body());
        for (var broadcastDay : broadcasts) {
            try {
                downloadBroadcastDay(broadcastDay);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        doneSignal.countDown();
    }

    private Path getBCFileName(Broadcast bc, String fileExtension) throws IOException {
        ZonedDateTime bcDateTime = ZonedDateTime.parse(bc.getStart());
        DateTimeFormatter bcDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm");
        Path broadCastFolder=folder.resolve(bc.getTitle().trim());
        Files.createDirectories(broadCastFolder);
        return broadCastFolder.resolve(String.format("%s_%d%s", bcDateTime.format(bcDateFormatter), bc.getId(), fileExtension));

    }

    public void downloadFiles(int concurrency)  {
        Semaphore sem=new Semaphore(concurrency);
        try(var es= Executors.newVirtualThreadPerTaskExecutor()) {
            for (var fileDownload : fileDownloadList) {
                es.submit(() -> {
                    try {
                        sem.acquire();
                        downloadFile(fileDownload);
                    } catch (IOException e) {
                        log.warn("Error downloading file \"{}\" ({})", fileDownload.path(), fileDownload.url(), e);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    } finally {
                        this.downloadsDone += 1;
                        sem.release();
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

    private void downloadFile(FileDownload fileDownload) throws IOException, InterruptedException {
        if(SkipExisting == fileDownload.updateStrategy() && Files.exists(fileDownload.path())) {
            log.debug("Skip (File exists): \"{}\" ({})", fileDownload.path(), fileDownload.url());
            return;
        }
        Path partPath= fileDownload.path().getParent().resolve(fileDownload.path().getFileName().toString() + ".part");
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(fileDownload.url()))
            .build();
        log.debug("Download  \"{}\" ({})", fileDownload.path().getFileName().toString(), fileDownload.url());
        HttpResponse<InputStream> response =
            client.send(request, HttpResponse.BodyHandlers.ofInputStream());
        OptionalLong cLengthOptional = response.headers().firstValueAsLong("content-length");
        long downloadedBytes;
        try (
            InputStream in = response.body();
            OutputStream out = new FileOutputStream(partPath.toFile())) {
                downloadedBytes=copyStreamWithProgress(in, out, String.format("\"%s\" (%s)", fileDownload.path(), fileDownload.url()), cLengthOptional.orElse(-1));
        }

        boolean fileExists = Files.exists(fileDownload.path());
        if( ReplaceIfBigger == fileDownload.updateStrategy() &&
            fileExists &&
            Files.size(fileDownload.path())!=downloadedBytes) {
                if(downloadedBytes > Files.size(fileDownload.path())) {
                    log.info("Replace with bigger file: \"{}\":{}b \"{}\":{}b", partPath,  Files.size(fileDownload.path()), fileDownload.path().getFileName(), downloadedBytes);
                    Files.move(partPath, fileDownload.path(), REPLACE_EXISTING);
                    log.info("Done \"{}\" ({})", fileDownload.path().getFileName(), fileDownload.url());
                }
        } else if(fileExists) { //File with same or bigger size exists; delete .part file
            log.debug("Delete .part file (same or bigger file size exists): \"{}\" ({})", partPath, fileDownload.url());
            Files.delete(partPath);
        }
        else {
            log.debug("Move \"{}\" \"{}\"", partPath, fileDownload.path().getFileName());
            Files.move(partPath, fileDownload.path());
            log.info("Done \"{}\" ({})", fileDownload.path().getFileName(), fileDownload.url());
        }
    }

    private long copyStreamWithProgress(InputStream in, OutputStream out, String sourceInfo, long contentLength){
        int totalBytesRead = 0;
        byte[] buffer = new byte[4 * 1024];
        int bytesRead;
        try {
            for (;;) {
                bytesRead = in.readNBytes(buffer,0, buffer.length);
                if (bytesRead == 0)
                    break;
                out.write(buffer, 0, bytesRead);
                this.bytesLoaded.getAndAdd(bytesRead);
                totalBytesRead += bytesRead;
            }
        } catch (IOException e) {
            if (contentLength > 0 &&
                totalBytesRead > 0 &&
                contentLength != totalBytesRead &&
                Math.abs(contentLength - totalBytesRead) < 4096) { //Allow the file size to be 4k off. (The server did set the content-length short by 1b for stream downloads)
                    log.warn("Ignoring wrong content-length: {} bytes received instead of {} ({})", totalBytesRead, contentLength, sourceInfo);
            } else throw new RuntimeException(e);
        }
        return totalBytesRead;
    }

    private void downloadBroadcastDay(BroadcastDay broadcastDay) throws IOException, InterruptedException {
        for (Broadcast bc : broadcastDay.getBroadcasts()) {
            try {
                downloadBroadcast(bc);
            } catch (IOException e) {
                log.warn("Error downloading broadcast \"{}\" ({})", bc.getTitle(), bc.getHref(), e);
            }
        }
    }



    private void downloadBroadcast(Broadcast bc) throws IOException, InterruptedException {
        String detailUri=bc.getHref()+"?items=true";
        Path jsonPath= getBCFileName(bc,".json");

        downloadFile(new FileDownload(detailUri, jsonPath,
            ReplaceIfBigger)); //broadcast .json will change during the 7 days it is online. We will use the version with the largest file size.
        Broadcast broadcastDetail = new ObjectMapper().readValue(jsonPath.toFile(), ResponseDetail.class).getBroadcast();
        for(int i=0;i<broadcastDetail.getImages().size();i++) {
            ImagesItem image=broadcastDetail.getImages().get(i);
            queueImage(image, i, broadcastDetail);
        }
        for(var item:broadcastDetail.getItems()) {
            queueItemImage(item, broadcastDetail);
        }
        if(broadcastDetail.getLink()!=null && broadcastDetail.getLink().getUrl()!=null && !broadcastDetail.getLink().getUrl().isEmpty())
            queueDownload(broadcastDetail.getLink().getUrl(),
                    getBCFileName(bc,".html"),
                    ReplaceIfBigger); //The HTML page may change during the 7 days it is online. We will use the version with the largest file size.
        queueStreamItems(broadcastDetail);
    }

    private void queueItemImage(ItemsItem item, Broadcast broadcastDetail) throws IOException {
        for(int i=0;i<item.getImages().size();i++) {
            var image=item.getImages().get(i);
            for(var ver:image.getVersions()) {
                Path imageFilePath = getBCFileName( broadcastDetail,String.format("_item_%d_%d_%d.jpg", item.getId(), i, ver.getWidth()));
                queueDownload(ver.getPath(), imageFilePath, SkipExisting);
            }
        }
    }

    private void queueImage(ImagesItem image, int i, Broadcast broadcastDetail) throws IOException {
        for(var version: image.getVersions()) {
            Path imageFilePath = getBCFileName(broadcastDetail, String.format("_%d_%d.jpg", i, version.getWidth()));
                queueDownload(version.getPath(), imageFilePath, SkipExisting);
        }
    }

    private void queueStreamItems(Broadcast broadcastDetail) throws IOException {
        List<String> fileURLs=new ArrayList<>();
        for (int i = 0; i < broadcastDetail.getStreams().size(); i++) {
            StreamsItem stream=broadcastDetail.getStreams().get(i);
            Path path= getBCFileName(broadcastDetail, String.format("_%d.mp3", i));
            String streamURL=stream.getUrls().getProgressive();
            streamURL = streamURL.substring(0,streamURL.lastIndexOf(".mp3")+".mp3".length()); //cut everything after .mp3 (stream offsets for webplayer)
            if(i > 0 && fileURLs.contains(streamURL)) {
                log.debug("Skip (duplicate stream URL): \"{}\" ({})", path.getFileName(), streamURL);
            } else {
                queueDownload(streamURL, path, SkipExisting);
            }
            fileURLs.add(streamURL);
        }
    }

    private void queueDownload(String url, Path path, UpdateStrategy updateStrategy) {
        if(updateStrategy== SkipExisting && Files.exists(path)) {
            log.debug("Skip queue (File exists): \"{}\"", path);
        } else {
            log.debug("Queue \"{}\" ({})", path.getFileName(), url);
            this.fileDownloadList.add(new FileDownload(url, path, updateStrategy));
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
}
