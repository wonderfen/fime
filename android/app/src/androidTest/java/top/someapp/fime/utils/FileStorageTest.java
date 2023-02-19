package top.someapp.fime.utils;

import android.content.Context;
import org.junit.Test;
import top.someapp.fimesdk.FimeContext;

/**
 * @author zwz
 * Created on 2023-01-04
 */
public class FileStorageTest {

    @Test
    public void testCopyToInnerStorage() {
        Context context = FimeContext.getInstance()
                                     .getContext();
        // context.getExternalFilesDir()
        // FileStorage.copyFieUriToInnerStorage(context, Uri.F);
    }
}
