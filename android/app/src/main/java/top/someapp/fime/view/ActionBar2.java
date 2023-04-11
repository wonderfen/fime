/*
 * Copyright (c) 2023  Fime project https://fime.site
 * Initial author: zelde126@126.com
 */

package top.someapp.fime.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import androidx.annotation.NonNull;
import top.someapp.fimesdk.api.FimeHandler;
import top.someapp.fimesdk.view.Box;
import top.someapp.fimesdk.view.Widget;

/**
 * @author zwz
 * Created on 2023-04-11
 */
public class ActionBar2 extends SurfaceView implements SurfaceHolder.Callback, Widget {

    public ActionBar2(Context context) {
        super(context);
    }

    public ActionBar2(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override public void surfaceCreated(@NonNull SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

    }

    @Override public void surfaceDestroyed(@NonNull SurfaceHolder holder) {

    }

    @Override public Box getContainer() {
        return null;
    }

    @Override public void onDraw(Canvas canvas, Box box, FimeHandler painter) {

    }

    @Override public void onTouchStart(PointF pos) {

    }

    @Override public void onTouchMove(PointF pos) {

    }

    @Override public void onTouchEnd(PointF pos) {

    }

    @Override public void onLongPress(PointF pos, long durations) {

    }

    @Override public void setOnVirtualKeyListener(OnVirtualKeyListener virtualKeyListener) {

    }
}
