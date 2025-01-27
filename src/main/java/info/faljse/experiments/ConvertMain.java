package info.faljse.experiments;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import info.faljse.broadcast.Broadcast;
import info.faljse.broadcast.ResponseDetail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;

public class ConvertMain {
    public static void main(String[] args) {
        ConvertMain m=new ConvertMain();
        try {
            m.convert(Path.of("fm4"), "fm4");
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void convert(Path dir, String publisher) throws InterruptedException, IOException {
        try (var ex = Executors.newVirtualThreadPerTaskExecutor();
             var files = Files.newDirectoryStream(dir);) {

            for (Path file : files) {
                System.out.println(file.getFileName());
                if (file.toString().endsWith(".json")) {
                    ex.submit(() -> {
                        try {
                            convertFile(file, publisher);
                        } catch (IOException | InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    });

                }
            }
        }
    }

    private void convertFile(Path file, String publisher) throws IOException, InterruptedException {
        ObjectMapper mapper=new ObjectMapper();
        Broadcast broadcast =
                mapper.readValue(Files.readAllBytes(file), new TypeReference<ResponseDetail>() {}).getBroadcast();
        Path mp3File= Path.of(file.toString().substring(0,file.toString().lastIndexOf(".json"))+"_0.mp3");
        boolean ex=Files.exists(mp3File);

        for(var item:broadcast.getItems()) {
            //ffmpeg -i file.mkv -ss 00:00:20 -to 00:00:40 -c copy file-2.mkv
            var date=ZonedDateTime.parse(item.getStart());
            String dateString=date.format( DateTimeFormatter.ofPattern("yyyyMMdd-HHmm"));

            int offsetStart=item.getStream().getOffsetStart()/1000;
            int offsetEnd=item.getStream().getOffsetEnd()/1000;
            int duration=offsetEnd-offsetStart;
            if(duration<30)
                continue;
            String artist=normalize(item.getInterpreter());
            if(artist.isBlank())
                artist=normalize(broadcast.getTitle());
            if(artist.isBlank())
                artist=normalize(String.valueOf(broadcast.getId()));

            String title=normalize(item.getInterpreter());
            if(title.isBlank())
                title=normalize(item.getTitle());
            if(title.isBlank())
                title=normalize(String.valueOf(item.getId()));

            Path outFileDir=Path.of("mp3",artist.toLowerCase(), artist.toLowerCase()+"("+publisher.toLowerCase()+")");
            Files.createDirectories(outFileDir);
            String outFileName=String.format("%s-%s-(%s).mp3", artist, title, dateString);

            ProcessBuilder b = new ProcessBuilder("ffmpeg", "-i", mp3File.toString(),
                    //"-ss", String.valueOf(offsetStart),
                    //"-to", String.valueOf(offsetEnd),
                    "-c", "copy",
                    "-metadata",
                    "artist=" + artist,
                    "-metadata",
                    "album_artist=" + artist,
                    "-metadata",
                    "title=" + title,
                    "-metadata",
                    "publisher=" + publisher,
                    outFileDir.resolve(outFileName).toString()).inheritIO();
            Process process = b.start();
            String result = new String(process.getInputStream().readAllBytes());
            process.waitFor();
        }
    }

    private static String normalize(String input) {
        return input == null ? null : Normalizer.normalize(input, Normalizer.Form.NFKD).replace(':','_');

    }
}