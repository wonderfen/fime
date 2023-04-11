/*
 * Copyright (c) 2023  Fime project https://fime.site
 * Initial author: zelde126@126.com
 */

package top.someapp.fime.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import androidx.annotation.NonNull;
import top.someapp.fime.BuildConfig;
import top.someapp.fimesdk.utils.Geometry;
import top.someapp.fimesdk.utils.Logs;
import top.someapp.fimesdk.view.Box;

/**
 * @author zwz
 * Created on 2023-04-11
 */
public class ActionBarView extends SurfaceView implements SurfaceHolder.Callback {

    private static boolean drawPath;
    private Box container;
    private ActionBar actionBar;
    private Canvas canvas;
    private SurfaceHolder surfaceHolder;
    private Path path;
    private Paint pathPaint;
    private PointF touchDown;               // 按下的位置
    private boolean inLongPressCheck;
    private InputView2 inputView;

    public ActionBarView(Context context) {
        super(context);
        init();
    }

    public ActionBarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    static void setDrawPath(boolean drawPath) {
        ActionBarView.drawPath = drawPath;
    }

    @Override public void surfaceCreated(@NonNull SurfaceHolder holder) {
        Logs.i("surfaceCreated");
        this.surfaceHolder = holder;
        actionBar = new ActionBar(getContainer());
        repaint();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        Logs.i("surfaceChanged");
        this.surfaceHolder = holder;
        if (actionBar != null) actionBar.setInputEditor(inputView.getInputEditor());
    }

    @Override public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        Logs.i("surfaceDestroyed");
        setDrawPath(false);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (actionBar == null) return false;

        float x = event.getX();
        float y = event.getY();
        final int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                inLongPressCheck = true;
                Logs.d("touchDown @(" + x + "," + y + ")");
                touchDown = new PointF(x, y);
                path.moveTo(x, y);
                actionBar.onTouchStart(touchDown);
                break;
            case MotionEvent.ACTION_MOVE:
                Logs.d(
                        "inLongPressCheck: " + inLongPressCheck + ", moveTo @(" + x + "," + y +
                                ")");
                path.lineTo(x, y);
                PointF pos = new PointF(x, y);
                double distance = Geometry.distanceBetweenPoints(touchDown, pos);
                Logs.d("move distance=" + distance);
                if (inLongPressCheck && distance >= 32) { // 打断长按！
                    inLongPressCheck = false;
                }
                if (!inLongPressCheck) actionBar.onTouchMove(pos);
                break;
            case MotionEvent.ACTION_UP:
                Logs.d(
                        "inLongPressCheck: " + inLongPressCheck + ", touchUp @(" + x + "," + y +
                                ")");
                PointF touchUp = new PointF(x, y);
                path.reset();
                inLongPressCheck = false;
                if (Geometry.distanceBetweenPoints(touchDown, touchUp) <= 36) { // 认为是click
                    PointF center = Geometry.centerOf(touchDown, touchUp);
                    actionBar.onTouchEnd(center);
                }
                touchDown = null;
                performClick();
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_OUTSIDE:
            default:
                Logs.d("Ignored touch event: " + action);
                path.reset();
                touchDown = null;
                inLongPressCheck = false;
                break;
        }
        return true;
    }

    @Override protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed || container == null) container = new Box(right - left, bottom - top);
    }

    void setInputView(InputView2 inputView) {
        this.inputView = inputView;
    }

    void repaint() {
        if (actionBar == null) return;

        Logs.d("repaint");
        try {
            canvas = surfaceHolder.lockCanvas();
            if (canvas == null) {
                Logs.w("Canvas is null, can not paint!");
            }
            else {
                actionBar.onDraw(canvas, actionBar.getContainer(), inputView.getPainter());
                if (drawPath) canvas.drawPath(path, pathPaint);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            Logs.e("repaint error:%s", e.getMessage());
        }
        finally {
            if (canvas != null) surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    private Box getContainer() {
        if (container == null) container = new Box(getWidth(), getHeight());
        return container;
    }

    private void init() {
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);

        path = new Path();
        pathPaint = new Paint();
        pathPaint.setColor(Color.BLUE);
        pathPaint.setStyle(Paint.Style.STROKE);
        pathPaint.setStrokeWidth(5);
        pathPaint.setAntiAlias(true);

        if (BuildConfig.DEBUG) setDrawPath(true);
    }
}
