package top.someapp.fimesdk.dict;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import top.someapp.fimesdk.FimeContext;
import top.someapp.fimesdk.engine.Converter;

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
        CsvDict.convert(fimeContext.fileInAppHome("pinyin_dict.csv"), new Converter(), target);
        CsvDict.sort(target, fimeContext.fileInAppHome("ok.csv"));
        assertTrue(target.exists());
    }
}

