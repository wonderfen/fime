/*
 * Copyright (c) 2023 Fime project https://fime.fit
 * Initial author: zelde126@126.com
 */

package top.someapp.fimesdk.view;

import android.graphics.Canvas;
import android.graphics.PointF;
import top.someapp.fimesdk.api.FimeHandler;

/**
 * @author zwz
 * Created on 2023-02-07
 */
public interface Widget {

    Box getContainer();

    default void applyTheme(Theme theme) {
    }

    void onDraw(Canvas canvas, Box box, FimeHandler painter);

    void onTouchStart(PointF pos);

    void onTouchMove(PointF pos);

    void onTouchEnd(PointF pos);

    void onLongPress(PointF pos, long durations);

    void setOnVirtualKeyListener(OnVirtualKeyListener virtualKeyListener);

    interface OnVirtualKeyListener {

        boolean onTap(VirtualKey virtualKey);

        boolean onLongPress(VirtualKey virtualKey, long durations);
    }
}
