# ORF Radio archiver
```bash
jdk21+,  maven

mvn install
mvn package
java -jar target/ar4-1.0-SNAPSHOT.jar  archive folderName  #save wie,fm4,oe3,oe1 radios to folder folderName
```


# Usage
```
Usage: ar4 [COMMAND]
Commands:
  archive   save streams to directory
  playlist  create m3u playlist from metadata

Usage: ar4 archive [-c=<concurrency>] folder
save streams to directory
      folder   archive folder
  -c, --concurrency=<concurrency>
               Concurrent stream downloads per radio station.
                 Default: 4

Usage: ar4 playlist [-a] archiveDir playlistDir
create m3u playlist from metadata
      archiveDir    archive folder
      playlistDir   playlist folder
  -a, --absolute    Create m3u file with absolute paths.
                      Default: false
```
