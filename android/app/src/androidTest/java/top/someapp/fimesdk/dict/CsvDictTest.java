package top.someapp.fimesdk.dict;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import top.someapp.fimesdk.FimeContext;

import java.io.File;

/**
 * @author zwz
 * Created on 2023-03-24
 */
public class CsvDictTest {

    @Test
    public void testSort() {
        FimeContext fimeContext = FimeContext.getInstance();
        File target = new File(fimeContext.getAppHomeDir(), "sort.csv");
        CsvDict.sort(fimeContext.fileInAppHome("pinyin_dict.csv"), target);
        assertTrue(target.exists());
    }
}

