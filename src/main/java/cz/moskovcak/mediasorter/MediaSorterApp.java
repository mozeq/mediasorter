package cz.moskovcak.mediasorter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Service
public class MediaSorterApp {
    private static Logger LOG = LoggerFactory.getLogger(MediaSorter.class);

    private final uTorrentProxy utorrent;

    @Inject
    public MediaSorterApp(uTorrentProxy utorrent) {
        this.utorrent = utorrent;
    }

    private void removeTorrent(String torrentHash) {
        try {
            utorrent.removeTorrent(torrentHash);
        } catch (IOException e) {
            LOG.error("Can't remove torrent", e);
        }
    }

    private static File[] listFiles(String path) {
        FileList fl = new FileList();
        return fl.listFiles(path);
    }

    public String getDestDir(final String mediaDir, final String showName, final String season) {
        return Paths.get(mediaDir).resolve("TV Shows").resolve(showName).resolve("Season " + season).toString();
    }

    public static void main(String[] args) {
        MediaSorterApp mediaSorterApp = null;
        String parentDir = null;
        String destDir = null;

        LOG.info("Run with (" + args.length + ") args:");
        for(String s: args) {
            LOG.info("'{}'", s);
        }

        if (args.length < 2) {
            System.out.println("Usage\n\tMediaSorter PARENTDIR DESTDIR [TORRENTHASH]");
            System.exit(0);
        }

        if (args.length == 3) {
            ApplicationContext context = new ClassPathXmlApplicationContext("spring-config.xml");
            mediaSorterApp = context.getBean(MediaSorterApp.class);
            mediaSorterApp.removeTorrent(args[2]);
        }

        parentDir = args[0];
        destDir = args[1];
        MediaSorter ms = new MediaSorter();

        Map<String, HashMap<String, ArrayList<File>>> showTree = ms.sortFiles(listFiles(parentDir));

        int fileCounter = 0;
        int movedFiles = 0;

        for (Map.Entry<String, HashMap<String, ArrayList<File>>> showEntry: showTree.entrySet()) {
            for (Map.Entry<String, ArrayList<File>> season: showEntry.getValue().entrySet()) {
                LOG.info("\t '{}'", season.getKey());
                String targetDir = mediaSorterApp.getDestDir(destDir, showEntry.getKey(), season.getKey());
                for (File f: season.getValue()) {
                    File destFile = new File(targetDir + "/" +f.getName());
                    LOG.info("\t\t '{}' => '{}'", f.getName(), destFile.getAbsolutePath());
                    //we don't care if this fails, the dirs might already exist, but it's easier than
                    // checking if it exists, if this fails the renameTo() will fail which we'll notice
                    destFile.getParentFile().mkdirs();
                    int tries = 3; //max retries when
                    while(tries > 0) {
                        try {
                            Files.move(f.toPath(), destFile.toPath());
                            movedFiles++;
                            break;
                        } catch (FileAlreadyExistsException e) {
                            LOG.warn("File '{}' already exists, not moving", destFile);
                            break;
                        } catch (IOException e) {
                            try {
								/* should help when the file is still open in utorrent
								 * renameTo() don't give us a reason why it failed, so we can only guess
								 * and retry if it fails
								 * */
                                Thread.sleep(1000);
                                tries--;
                                if (tries > 0)
                                    continue;
                                LOG.warn("Moving of '{}' failed, going to retry", f.toPath());
                            } catch (InterruptedException ie) {
                                //Handle exception
                            }
                            LOG.warn("Moving '{}' to '{}' failed", f.getName(), destFile.getAbsolutePath());
                        }
                    }
                    fileCounter++;
                }

            }

        }
        LOG.info("Found: '{}' moved: '{}'", fileCounter, movedFiles);

    }
}
