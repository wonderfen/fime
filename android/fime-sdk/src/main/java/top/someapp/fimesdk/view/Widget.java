package top.someapp.fimesdk.view;

import android.graphics.Canvas;
import android.graphics.PointF;

/**
 * @author zwz
 * Created on 2023-02-07
 */
public interface Widget {

    Box getContainer();

    void onDraw(Canvas canvas, Box box);

    boolean onTouchStart(PointF pos);

    boolean onTouchMove(PointF pos);

    boolean onTouchEnd(PointF pos);

    boolean onLongPress(PointF pos, long durations);

    void setOnVirtualKeyListener(OnVirtualKeyListener virtualKeyListener);

    interface OnVirtualKeyListener {

        boolean onTap(VirtualKey virtualKey);

        boolean onLongPress(VirtualKey virtualKey, long durations);
    }
}
