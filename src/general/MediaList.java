package general;

import mediatypes.GenericVideo;
import mediatypes.Media;
import mediatypes.Movie;
import mediatypes.TV;
import yjohnson.ConsoleEvent;
import yjohnson.PathFinder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedList;

public class MediaList {
    private Path dir;
    private String ext;
    private LinkedList<Media> mediaList;

    public MediaList(Path dir, String ext, Media.type type) {
        this.mediaList = new LinkedList<>();
        this.dir = dir;
        this.ext = ext;

        for (File f : PathFinder.findFiles(dir.toFile(), ext)) {
            ConsoleEvent.print(
                    "Adding " + f.getName() + " to the list as a " + type.name() + ".",
                    ConsoleEvent.logStatus.DETAIL
            );
            try {
                switch (type) {
                    case TV:
                        mediaList.add(new TV(f.getAbsolutePath()));
                        break;
                    case MOVIE:
                        mediaList.add(new Movie(f.getAbsolutePath()));
                        break;
                    case GENERIC:
                        mediaList.add(new GenericVideo(f.getAbsolutePath()));
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isEmpty() {
        return mediaList.isEmpty();
    }
    public int size() {
        return mediaList.size();
    }
}
