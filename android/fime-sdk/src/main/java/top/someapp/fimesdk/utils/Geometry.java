/*
 * Copyright (c) 2023 Fime project https://fime.fit
 * Initial author: zelde126@126.com
 */

package top.someapp.fimesdk.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.NinePatch;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import androidx.annotation.NonNull;
import top.someapp.fimesdk.FimeContext;

/**
 * @author zwz
 * Created on 2022-12-29
 */
public class Geometry {

    private Geometry() {
    }

    public static DisplayMetrics getDisplayMetrics() {
        DisplayMetrics displayMetrics;
        displayMetrics = FimeContext.getInstance()
                                    .getResources()
                                    .getDisplayMetrics();
        return displayMetrics;
    }

    public static int dp2px(double dpValue) {
        final float scale = FimeContext.getInstance()
                                       .getResources()
                                       .getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static int px2dp(double px) {
        final float scale = FimeContext.getInstance()
                                       .getResources()
                                       .getDisplayMetrics().density;
        return (int) (px / scale + 0.5f);
    }

    public static PointF centerOf(@NonNull PointF... pointFS) {
        if (pointFS.length == 1) return pointFS[0];

        float sumX = 0;
        float sumY = 0;
        for (PointF p : pointFS) {
            sumX += p.x;
            sumY += p.y;
        }
        return new PointF(sumX / pointFS.length, sumY / pointFS.length);
    }

    public static double distanceBetweenPoints(PointF p1, PointF p2) {
        float dx = p1.x - p2.x;
        float dy = p1.y - p2.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    public static Bitmap bitmapFitToHeight(@NonNull Bitmap source, float newHeight) {
        if (source.getHeight() == newHeight) return source;

        float scale = newHeight / source.getHeight();
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix,
                                   false);
    }

    public static Bitmap bitmapFitToSize(@NonNull Bitmap source, float newWidth, float newHeight) {
        if (source.getWidth() == newWidth && source.getHeight() == newHeight) return source;
        float xScale = newWidth / source.getWidth();
        float yScale = newHeight / source.getHeight();
        Matrix matrix = new Matrix();
        matrix.postScale(xScale, yScale);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix,
                                   false);
    }

    public static Bitmap drawableToBitmap(@NonNull Drawable drawable, float newWidth,
            float newHeight) {
        Bitmap bitmap = Bitmap.createBitmap((int) newWidth, (int) newHeight,
                                            Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public static Bitmap create9pngBitmap(@NonNull Drawable _9png, float newWidth,
            float newHeight) {
        Bitmap bitmap = Bitmap.createBitmap(_9png.getIntrinsicWidth(), _9png.getIntrinsicHeight(),
                                            Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        NinePatch ninePatch = new NinePatch(bitmap, bitmap.getNinePatchChunk(), null);

        return bitmap;
    }
}
