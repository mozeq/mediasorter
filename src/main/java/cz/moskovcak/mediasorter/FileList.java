package cz.moskovcak.mediasorter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;

class MediaFileFilter implements FileFilter
{
  private final String[] mediaExtensions =
    new String[] {"mp3", "mkv", "mp4", "srt", "avi"};

  public boolean accept(File file)
  {
    for (String extension : mediaExtensions)
    {
      if (file.getName().toLowerCase().endsWith(extension))
      {
        return true;
      }
    }
    return false;
  }
}


public class FileList {
    private static final Logger LOG = LoggerFactory.getLogger(FileList.class);

	File[] listFiles(String dirname) {
		File dir = new File(dirname);
		File[] mediaFiles = dir.listFiles(new MediaFileFilter());
        LOG.info("Found '{}' files", mediaFiles.length);
		return mediaFiles;
	}

	public static void main(String[] args) {
		FileList fl = new FileList();
		for (File f: fl.listFiles("/mnt/Downloads"))
			System.out.println(f.getName());
	}
}
