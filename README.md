# ORF Radio archiver
```sh
jdk21+,  maven

mvn install
mvn compile
mvn package
java -jar target/ar4-1.0-SNAPSHOT.jar  archive -s fm4,oe1 folderName  #save fm4,oe1 radios to folder folderName
```


# Usage
```sh
Usage: ar4 [COMMAND]
Commands:
  archive   save streams to directory
  playlist  create m3u playlist from metadata

Usage: ar4 archive [-c=<concurrency>] [-s=stations[,stations...]]... folder
save streams to directory
      folder   archive folder
  -c, --concurrency=<concurrency>
               Concurrent stream downloads per radio station.
                 Default: 4
  -s, --stations=stations[,stations...]
               Possible values: fm4,oe1,oe3,wie,bgl,ktn,noe,ooe,sbg,stm,tir,vbg
                 Default: fm4,oe1,oe3,wie


Usage: ar4 playlist [-a] archiveDir playlistDir
create m3u playlist from metadata
      archiveDir    archive folder
      playlistDir   playlist folder
  -a, --absolute    Create m3u file with absolute paths.
                      Default: false
```

# Example
```sh
#download last 7 days of fm4 music to folder fm4_archive
java -jar target/ar4-1.0-SNAPSHOT.jar  archive -s fm4 fm4_archive

#generate playlists in folder fm4_playlists
java -jar target/ar4-1.0-SNAPSHOT.jar  playlist fm4_archive fm4_playlists



```

