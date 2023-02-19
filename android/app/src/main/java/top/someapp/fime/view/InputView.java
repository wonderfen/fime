package top.someapp.fime.view;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.os.HandlerThread;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import androidx.annotation.NonNull;
import top.someapp.fimesdk.Fime;
import top.someapp.fimesdk.api.DefaultFimeHandler;
import top.someapp.fimesdk.api.FimeHandler;
import top.someapp.fimesdk.api.FimeMessage;
import top.someapp.fimesdk.api.ImeEngine;
import top.someapp.fimesdk.config.Keycode;
import top.someapp.fimesdk.utils.Geometry;
import top.someapp.fimesdk.utils.Strings;
import top.someapp.fimesdk.view.Box;
import top.someapp.fimesdk.view.Keyboards;
import top.someapp.fimesdk.view.VirtualKey;
import top.someapp.fimesdk.view.Widget;

/**
 * InputView 是 Android IMF(Input method framework) 要求提供给用户交互的一个 View。它必须提供的功能有：
 * 展示输入和候选界面，响应用户的操作(实体键盘事件，View上的点击，滑动等)。
 * 本类实现的逻辑有： 创建 ActionBar、Keyboards，提供绘制环境，转发用户操作、重新布局、重绘 View
 *
 * @author zwz
 * Created on 2022-12-29
 */
public class InputView extends SurfaceView implements SurfaceHolder.Callback, View.OnKeyListener {

    private static final String TAG = Fime.makeTag("InputView");
    private static final int actionBarHeight = Geometry.dp2px(64);
    private static boolean drawPath;
    private final ImeEngine engine;
    private ActionBar actionBar;
    private Keyboards keyboards;
    private HandlerThread workThread;
    private FimeHandler painter;
    private Box container;
    private Canvas canvas;
    private SurfaceHolder surfaceHolder;
    private Path path;
    private Paint pathPaint;
    private MotionEvent lastTouchEvent;
    private PointF touchDown;               // 按下的位置
    private int longPressThreshold = 650;   // 触发长按的阀值
    private boolean touchHasConsumed;

    public InputView(ImeEngine engine) {
        super(engine.getContext());
        Log.d(TAG, Strings.simpleFormat("create InputView: 0x%x.", hashCode()));
        this.engine = engine;
        init();
        setupPainter();
    }

    @Override public void surfaceCreated(@NonNull SurfaceHolder holder) {
        Log.i(TAG, "surfaceCreated");
        this.surfaceHolder = holder;
        if (actionBar != null) {
            actionBar.setInputEditor(engine.getSchema()
                                           .getInputEditor());
        }
        keyboards = engine.getSchema()
                          .getKeyboards();
        setupPainter();
        repaint();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        Log.i(TAG, "surfaceChanged");
        this.surfaceHolder = holder;
        repaint();
    }

    @Override public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        Log.i(TAG, "surfaceDestroyed");
        if (workThread != null) workThread.quit();
        workThread = null;
        painter = null;
    }

    @Override public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        boolean needRepaint = false;
        final int action = event.getAction();
        long eventTime = event.getEventTime();  // 事件发生的时间
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                Log.d(TAG, "touchDown @(" + x + "," + y + ")");
                touchDown = new PointF(x, y);
                lastTouchEvent = event;
                path.moveTo(x, y);
                needRepaint = findWidgetContains(touchDown).onTouchStart(touchDown);
                painter.sendEmptyMessageDelayed(FimeMessage.MSG_CHECK_LONG_PRESS,
                                                longPressThreshold);
                break;
            case MotionEvent.ACTION_MOVE:
                Log.d(TAG, "moveTo @(" + x + "," + y + ")");
                path.lineTo(x, y);
                if (touchHasConsumed) {
                    touchHasConsumed = false;
                }
                else {
                    PointF pos = new PointF(x, y);
                    double distance = Geometry.distanceBetweenPoints(touchDown, pos);
                    Log.d(TAG, "move distance=" + distance);
                    if (distance > 0) {
                        touchHasConsumed = true;
                        if (eventTime - event.getDownTime() >= longPressThreshold && distance <= 10) {
                            needRepaint = findWidgetContains(pos).onLongPress(pos,
                                                                              eventTime - event.getDownTime());
                        }
                        else {
                            needRepaint = findWidgetContains(pos).onTouchMove(pos);
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                Log.d(TAG, "touchUp @(" + x + "," + y + ")");
                PointF touchUp = new PointF(x, y);
                path.reset();
                if (touchHasConsumed) {
                    touchHasConsumed = false;
                }
                else {
                    if (Geometry.distanceBetweenPoints(touchDown, touchUp) <= 36) { // 认为是click
                        PointF center = Geometry.centerOf(touchDown, touchUp);
                        needRepaint = findWidgetContains(center).onTouchEnd(center);
                    }
                }
                touchDown = null;
                lastTouchEvent = null;
                break;
            default:
                Log.d(TAG, "Ignored touch event: " + action);
                break;
        }
        if (needRepaint) repaint();
        return true;
    }

    @Override public boolean onKey(View v, int keyCode, KeyEvent event) {
        Keycode keycode = Keycode.convertNativeKey(keyCode, event);
        if (keycode != null) {
            engine.onTap(new VirtualKey(keycode.code, keycode.label));
        }
        return false;
    }

    @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (container == null) {
            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            container = new Box(displayMetrics.widthPixels,
                                keyboards.getHeightPx() + actionBarHeight);
        }
        setMeasuredDimension((int) container.getWidth(), (int) container.getHeight());
    }

    boolean handle(@NonNull Message msg) {
        switch (msg.what) {
            case FimeMessage.MSG_REPAINT:
            case FimeMessage.MSG_CANDIDATE_CHANGE:
                painter.post(this::repaint);
                return true;
            case FimeMessage.MSG_CHECK_LONG_PRESS:
                Log.d(TAG, "MSG_CHECK_LONG_PRESS");
                if (touchDown != null && lastTouchEvent != null) {
                    long eventTime = lastTouchEvent.getEventTime();
                    long downTime = lastTouchEvent.getDownTime();
                    Log.d(TAG, "eventTime=" + eventTime + ", downTime=" + downTime);
                    if (eventTime == downTime) { // 没有触发移动事件
                        if (msg.getWhen() - downTime >= longPressThreshold) {
                            findWidgetContains(touchDown).onLongPress(touchDown,
                                                                      msg.getWhen() - downTime);
                        }
                    }
                    else {  // 按下并移动了一小段距离
                        if (eventTime - downTime >= longPressThreshold) {
                            findWidgetContains(touchDown).onLongPress(touchDown,
                                                                      msg.getWhen() - downTime);
                        }
                    }
                    painter.sendEmptyMessageDelayed(msg.what, 100);
                }
        }
        return false;
    }

    private Widget findWidgetContains(PointF pos) {
        if (actionBar.getContainer()
                     .surround(pos)) {
            return actionBar;
        }
        return keyboards.getCurrent();
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

        final DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        actionBar = new ActionBar(new Box(displayMetrics.widthPixels, actionBarHeight));
        keyboards = engine.getSchema()
                          .getKeyboards();
    }

    private void setupPainter() {
        engine.unregisterHandler(TAG + "-handler");
        if (workThread == null || !workThread.isAlive()) {
            workThread = new HandlerThread(TAG + "-painter");
            workThread.start();
            painter = new DefaultFimeHandler(workThread.getLooper(), TAG + "-handler",
                                             this::handle);
        }
        engine.registerHandler(painter);
    }

    private void repaint() {
        Log.d(TAG, "repaint");
        try {
            canvas = surfaceHolder.lockCanvas();
            if (canvas == null) {
                Log.w(TAG, "Canvas is null, can not paint!");
            }
            else {
                actionBar.onDraw(canvas, actionBar.getContainer());
                keyboards.getCurrent()
                         .onDraw(canvas,
                                 new Box(new PointF(0, actionBarHeight), container.getWidth(),
                                         container.getHeight() - actionBarHeight));
                if (drawPath) canvas.drawPath(path, pathPaint);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (canvas != null) surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }
}
