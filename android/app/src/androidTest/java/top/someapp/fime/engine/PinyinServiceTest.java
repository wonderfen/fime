package top.someapp.fime.engine;

import static org.junit.Assert.assertNotNull;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import org.junit.Test;
import top.someapp.fimesdk.FimeContext;

import java.io.IOException;

/**
 * @author zwz
 * Created on 2023-01-03
 */
public class PinyinServiceTest {

    @Test
    public void testReadRes() throws IOException {
        Context context = FimeContext.getInstance()
                                     .getContext();
        // AssetFileDescriptor afd = context.getResources()
        //                                  .openRawResourceFd(
        //                                          R.raw.keyboard_qwerty);
        // Reader reader = FimeContext.getInstance()
        //                            .getResourcesAsReader(R.raw.dict_pinyin);
        AssetFileDescriptor afd = context.getAssets()
                                         .openFd("dict_pinyin.dat");
        assertNotNull(afd);
    }
}
