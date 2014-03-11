package cz.moskovcak.mediasorter;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MediaSorter {

	// Show | Season | File
	Map<String, HashMap<String, ArrayList<File>>> sortMedia(String path) {

		FileList fl = new FileList();

		Map<String, HashMap<String, ArrayList<File>>> retval = new HashMap<String, HashMap<String, ArrayList<File>>>();

		File[] fileList = fl.listFiles(path);

		if (fileList == null)
			return retval; //returns an empty array

		for (File f: fileList) {
			//System.out.println("Processing: " + f.getName());
			Pattern pattern = Pattern.compile("(.+)\\.s([0-9]+)e([0-9]+)");
			Matcher matcher = pattern.matcher(f.getName().toLowerCase());

			if (matcher.find() && matcher.groupCount() > 2) {
				String showName = matcher.group(1).replace('.', ' ');
				String season = matcher.group(2);

				HashMap<String, ArrayList<File>> showDir = retval.get(showName);
				if (showDir == null) {
					showDir = new HashMap<String, ArrayList<File>>();
					retval.put(showName, showDir);
				}

				if (showDir != null) {
					String seasonStr = "Season " + season;
					ArrayList<File> files = showDir.get(seasonStr);
					if (files == null) {
						files = new ArrayList<File>();
						showDir.put(seasonStr, files);
					}

					files.add(f);
				}

			}
		}

		return retval;
	}
}
