package top.someapp.fimesdk.utils;

import android.content.res.AssetManager;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import top.someapp.fimesdk.FimeContext;
import top.someapp.fimesdk.view.Box;

/**
 * @author zwz
 * Created on 2022-12-23
 */
public class Fonts {

    private static Typeface smileySans; // 得意黑字体
    private static Typeface jetBrainMono; // JetBrains Mono 字体

    public static Typeface defaultKeyLabelFont() {
        if (jetBrainMono == null) {
            AssetManager assets = FimeContext.getInstance()
                                             .getAssets();
            jetBrainMono = Typeface.createFromAsset(assets, "fonts/JetBrainsMono-Regular.ttf");
        }
        return jetBrainMono;
    }

    public static Typeface defaultSystemFont() {
        return Typeface.defaultFromStyle(Typeface.NORMAL);
    }

    public static Box getTextBounds(Paint paint, String text) {
        Rect rect = new Rect();
        paint.getTextBounds(text, 0, text.length(), rect);
        return new Box(rect.width(), rect.height());
    }
}
