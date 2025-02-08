package info.faljse.ar4;
import java.nio.file.Path;

public record FileDownload(String url,
                           Path path,
                           UpdateStrategy updateStrategy) {
}