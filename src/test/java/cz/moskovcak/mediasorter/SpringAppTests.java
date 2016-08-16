package cz.moskovcak.mediasorter;

import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@ContextConfiguration("classpath:spring-config.xml")
public class SpringAppTests {

    private static File[] fileList() {

        return new File[] {
                new File("Vikings.S01E02.HDTV.x264-2HD.mp4"),
                new File("Z.Nation.S01E01.720p.HDTV.x264-KILLERS.mkv"),
                new File("The.Walking.Dead.S04E01.HDTV.x264-ASAP.mp4")
        };
    };

    @Test
    public void sortFilesTest() {
        MediaSorter ms = new MediaSorter();
        Map<String, HashMap<String, ArrayList<File>>> sortedFiles = ms.sortFiles(fileList());

        //sortedFiles.keySet().forEach(System.out::println);
        //sortedFiles.get("z nation").keySet().forEach(System.out::println);
        assertEquals(3, sortedFiles.keySet().size());
        assertNotNull(sortedFiles.get("z nation"));

        assertEquals(sortedFiles.get("z nation").get("01").size(), 1);
    }
}
