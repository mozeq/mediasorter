package cz.moskovcak.mediasorter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


class MediaSorter {
	private static final Logger LOG = LoggerFactory.getLogger(MediaSorter.class);
    private static final Pattern pattern = Pattern.compile("(.+)\\.s([0-9]+)e([0-9]+)");


	// Show | Season | File
	Map<String, HashMap<String, ArrayList<File>>> sortFiles(final File[] fileList) {

		Map<String, HashMap<String, ArrayList<File>>> retval = new HashMap<>();

		if (fileList == null)
			return retval; //returns an empty array

		for (File f: fileList) {
            String filename = f.getName().toLowerCase();
            LOG.info("Processing '{}'", filename);
			Matcher matcher = pattern.matcher(filename);

			if (matcher.find() && matcher.groupCount() > 2) {
				String showName = matcher.group(1).replace('.', ' ');
				String season = matcher.group(2);

				HashMap<String, ArrayList<File>> showDir = retval.get(showName);
				if (showDir == null) {
					showDir = new HashMap<>();
					retval.put(showName, showDir);
				}

                ArrayList<File> files = showDir.get(season);
                if (files == null) {
                    files = new ArrayList<>();
                    showDir.put(season, files);
                }
                files.add(f);

            } else {
			    LOG.info("'{}' doesn't match show name pattern", filename);
            }
		}

		return retval;
	}
}
