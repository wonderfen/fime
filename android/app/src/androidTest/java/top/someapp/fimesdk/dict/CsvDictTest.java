package top.someapp.fimesdk.dict;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import top.someapp.fimesdk.FimeContext;

import java.io.File;
import java.io.IOException;

/**
 * @author zwz
 * Created on 2023-03-24
 */
public class CsvDictTest {

    @Test
    public void testSort() throws IOException {
        FimeContext fimeContext = FimeContext.getInstance();
        File target = new File(fimeContext.getAppHomeDir(), "sort.csv");
        File source = fimeContext.fileInAppHome("pinyin_dict.csv");
        CsvDict csvDict = new CsvDict(source, target);
        csvDict.normalize(fimeContext.getWorkDir());
        assertTrue(target.exists());
    }
}

