package info.faljse.ar4;

import com.fasterxml.jackson.databind.ObjectMapper;
import info.faljse.ar4.broadcast.*;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j(topic = "StationRenamer")
public class StationRenamer {
    private final Path folder;
    public StationRenamer(Path folder) {
        this.folder = folder;
    }

    public void renameFiles() throws IOException, InterruptedException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(folder, "*.json")) {
            for (Path jsonPath : stream) {
                Broadcast broadcastDetail = new ObjectMapper().readValue(jsonPath.toFile(), ResponseDetail.class).getBroadcast();
                renameBroadcast(broadcastDetail);
            }
        }
    }


    private Path getOldBCFileName(Broadcast bc, String fileExtension) throws IOException {
        return folder.resolve(String.format("%d%s", bc.getId(), fileExtension));

    }

    private Path getBCFileName(Broadcast bc, String fileExtension) throws IOException {
        ZonedDateTime bcDateTime = ZonedDateTime.parse(bc.getStart());
        DateTimeFormatter bcDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm");
        Path broadCastFolder=folder.resolve(bc.getTitle().trim());
        Files.createDirectories(broadCastFolder);
        return broadCastFolder.resolve(String.format("%s_%d%s", bcDateTime.format(bcDateFormatter), bc.getId(), fileExtension));

    }

    private void renameBroadcast(Broadcast bc) throws IOException, InterruptedException {
        Path jsonPath= folder.resolve(String.format("%d.json", bc.getId()));
        Broadcast broadcastDetail = new ObjectMapper().readValue(jsonPath.toFile(), ResponseDetail.class).getBroadcast();
        for(int i=0;i<broadcastDetail.getImages().size();i++) {
            ImagesItem image=broadcastDetail.getImages().get(i);
            renameImage(image, i, broadcastDetail);
        }
        for(var item:broadcastDetail.getItems()) {
            renameItemImage(item, broadcastDetail);
        }
        renameStreamItems(broadcastDetail);
        Path newJsonPath= getBCFileName(bc,".json");
        if(!Files.exists(jsonPath)) {
            log.warn("File {} does not exist, skipping rename", jsonPath);
            return;
        }
        if(Files.exists(newJsonPath)) {
            log.warn("File {} does exist, skipping copy", jsonPath);
            return;
        }
        Files.copy(jsonPath, newJsonPath);
    }

    private void renameItemImage(ItemsItem item, Broadcast broadcastDetail) throws IOException {
        for(int i=0;i<item.getImages().size();i++) {
            var image=item.getImages().get(i);
            for(var ver:image.getVersions()) {
                Path oldImageFilePath = getOldBCFileName(broadcastDetail,String.format("_item_%d_%d_%d.jpg", item.getId(), i, ver.getWidth()));
                Path newImageFilePath = getBCFileName(broadcastDetail,String.format("_item_%d_%d_%d.jpg", item.getId(), i, ver.getWidth()));
                if(!Files.exists(oldImageFilePath)) {
                    log.warn("File {} does not exist, skipping rename", oldImageFilePath);
                    continue;
                }
                Files.move(oldImageFilePath, newImageFilePath);
            }
        }
    }

    private void renameImage(ImagesItem image, int i, Broadcast broadcastDetail) throws IOException {
        for(var version: image.getVersions()) {
            Path oldImageFilePath = getOldBCFileName(broadcastDetail, String.format("_%d_%d.jpg", i, version.getWidth()));
            Path newImageFilePath = getBCFileName(broadcastDetail, String.format("_%d_%d.jpg", i, version.getWidth()));
            if(!Files.exists(oldImageFilePath)) {
                log.warn("File {} does not exist, skipping rename", oldImageFilePath);
                continue;
            }
            Files.move(oldImageFilePath, newImageFilePath);
        }
    }

    private void renameStreamItems(Broadcast broadcastDetail) throws IOException {
        for (int i = 0; i < broadcastDetail.getStreams().size(); i++) {
            Path oldPath= getOldBCFileName(broadcastDetail, String.format("_%d.mp3", i));
            Path newPath= getBCFileName(broadcastDetail, String.format("_%d.mp3", i));
            if(!Files.exists(oldPath)) {
                log.warn("File {} does not exist, skipping rename", oldPath);
                continue;
            }
            Files.move(oldPath, newPath);
        }
    }
}
