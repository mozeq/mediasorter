package cz.moskovcak.mediasorter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MediaSorterApp {
    private static Logger LOG = LoggerFactory.getLogger(MediaSorter.class);
    public static void main(String[] args) {
        String parentDir = null;
        String torrentHash = null;
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

            //System.out.println("HASH: " + args[1]);
            torrentHash = args[2];

            uTorrentProxy utorrent = (uTorrentProxy) context.getBean("uTorrentProxy");
            try {
                utorrent.removeTorrent(torrentHash);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        parentDir = args[0];
        destDir = args[1];
        //System.out.println(parentDir);
        MediaSorter ms = new MediaSorter();
        Map<String, HashMap<String, ArrayList<File>>> showTree = ms.sortMedia(parentDir);

        int fileCounter = 0;
        int movedFiles = 0;

        for (Map.Entry<String, HashMap<String, ArrayList<File>>> showEntry: showTree.entrySet()) {
            //System.out.println(showEntry.getKey());
            for (Map.Entry<String, ArrayList<File>> season: showEntry.getValue().entrySet()) {
                LOG.info("\t '{}'", season.getKey());
                String targetDir = destDir + File.separator + "TV Shows" + File.separator + showEntry.getKey()+ File.separator +season.getKey();
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
