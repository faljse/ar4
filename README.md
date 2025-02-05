# ORF Radio archiver
This repository offers a tool for downloading and archiving streams from all 12 ORF (Austrian Public Broadcaster) radio stations.

* Archive full radio shows as MP3 files
* Archive all metadata as JSON
* Archive broadcast and track images in all available resolutions.
* Generate .m3u playlists from metadata for each show
* ORF Broadcast API Version 5.0

# How it works
* Step 1: Download all Metadata (Using One connection per station)
* Step 2: Download streams using -c (default:4) concurrent connections per station.
* The tool automatically skips files that already exist, making it easy to keep your archive up-to-date. For a complete collection, simply run the tool at least once a week.


# Build/test it

```sh
Requires jdk21+, maven

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
java -jar ar4-1.0-SNAPSHOT.jar  archive -s fm4 fm4_archive

#generate playlists in folder fm4_playlists
java -jar ar4-1.0-SNAPSHOT.jar  playlist fm4_archive fm4_playlists
```

