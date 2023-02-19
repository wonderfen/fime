package top.someapp.fimesdk.utils;

import android.content.res.AssetFileDescriptor;
import android.media.SoundPool;
import android.os.Build;
import androidx.annotation.RequiresApi;
import top.someapp.fimesdk.FimeContext;

import java.io.IOException;

/**
 * @author zwz
 * Create on 2023-01-01
 */
public class Effects {

    private static SoundPool soundPool;
    private static int soundId;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP) public static void playSound() {
        if (soundPool == null) {
            soundPool = new SoundPool.Builder().build();
            try {
                FimeContext fimeContext = FimeContext.getInstance();
                AssetFileDescriptor afd = fimeContext.getAssets()
                                                     .openFd(
                                                             "sounds/Effect_Tick.ogg");
                soundId = soundPool.load(afd, 1);
                soundPool.setOnLoadCompleteListener((soundPool, sampleId, status) -> {
                    soundPool.play(soundId, 1f, 1f, 1, 0, 1);
                });
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        soundPool.play(soundId, 1f, 1f, 1, 0, 1);
    }
}
