package cz.moskovcak.mediasorter;

import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.Assert.assertEquals;

@ContextConfiguration("classpath:spring-config.xml")
public class MediaSorterAppTests {

    @Test
    public void getDestDirTest() {
        MediaSorterApp ms = new MediaSorterApp(null);
        String path = ms.getDestDir("/media", "z nation", "02");
        assertEquals("/media/TV Shows/z nation/Season 02", path);
    }
}
