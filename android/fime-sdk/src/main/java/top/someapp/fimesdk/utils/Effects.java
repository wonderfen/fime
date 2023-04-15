/*
 * Copyright (c) 2023 Fime project https://fime.fit
 * Initial author: zelde126@126.com
 */

package top.someapp.fimesdk.utils;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.res.AssetFileDescriptor;
import android.media.SoundPool;
import android.os.Build;
import android.os.Vibrator;
import androidx.annotation.RequiresApi;
import top.someapp.fimesdk.FimeContext;
import top.someapp.fimesdk.Setting;

import java.io.IOException;

/**
 * @author zwz
 * Create on 2023-01-01
 */
public class Effects {

    private static final String kSoundFile = "sounds/Effect_Tick.ogg";
    private static SoundPool soundPool;
    private static int soundId;
    private static Vibrator vibrator;

    private Effects() {
        //no instance
    }

    @SuppressLint("MissingPermission") @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static void playSoundAndVibrateIf() {
        init();
        try {
            Setting setting = Setting.getInstance();
            if (setting.getBoolean(Setting.kKeyboardPlayKeySound)) {
                soundPool.play(soundId, 1f, 1f, 1, 0, 1);
            }
            if (setting.getBoolean(Setting.kKeyboardKeyVibrate)) vibrator.vibrate(50);
        }
        catch (Exception e) {
            e.printStackTrace();
            Logs.w(e.getMessage());
        }
    }

    private static void init() {
        FimeContext fimeContext = FimeContext.getInstance();
        if (soundPool == null) {
            soundPool = new SoundPool.Builder().build();
            try {
                AssetFileDescriptor afd = fimeContext.getAssets()
                                                     .openFd(kSoundFile);
                soundId = soundPool.load(afd, 1);
            }
            catch (IOException e) {
                e.printStackTrace();
                Logs.w(e.getMessage());
            }
        }
        if (vibrator == null) {
            vibrator = (Vibrator) fimeContext.getContext()
                                             .getSystemService(Service.VIBRATOR_SERVICE);
        }
    }
}
