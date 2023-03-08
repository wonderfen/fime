package top.someapp.fime.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.os.Message;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import androidx.annotation.NonNull;
import com.typesafe.config.Config;
import top.someapp.fime.BuildConfig;
import top.someapp.fimesdk.FimeContext;
import top.someapp.fimesdk.Setting;
import top.someapp.fimesdk.api.DefaultFimeHandler;
import top.someapp.fimesdk.api.FimeHandler;
import top.someapp.fimesdk.api.FimeMessage;
import top.someapp.fimesdk.api.ImeEngine;
import top.someapp.fimesdk.config.Configs;
import top.someapp.fimesdk.config.Keycode;
import top.someapp.fimesdk.utils.Geometry;
import top.someapp.fimesdk.utils.Logs;
import top.someapp.fimesdk.utils.Strings;
import top.someapp.fimesdk.view.Box;
import top.someapp.fimesdk.view.Keyboards;
import top.someapp.fimesdk.view.Theme;
import top.someapp.fimesdk.view.VirtualKey;
import top.someapp.fimesdk.view.Widget;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * InputView 是 Android IMF(Input method framework) 要求提供给用户交互的一个 View。它必须提供的功能有：
 * 展示输入和候选界面，响应用户的操作(实体键盘事件，View上的点击，滑动等)。
 * 本类实现的逻辑有： 创建 ActionBar、Keyboards，提供绘制环境，转发用户操作、重新布局、重绘 View
 *
 * @author zwz
 * Created on 2022-12-29
 */
public class InputView extends SurfaceView implements SurfaceHolder.Callback, View.OnKeyListener {

    private static final String TAG = "InputView";
    private static final int actionBarHeight = Geometry.dp2px(64);
    private static boolean drawPath;
    private final ImeEngine engine;
    private final Set<Theme> themes = new HashSet<>();
    private ActionBar actionBar;
    private Keyboards keyboards;
    private FimeHandler painter;
    private Box container;
    private Canvas canvas;
    private SurfaceHolder surfaceHolder;
    private Path path;
    private Paint pathPaint;
    private MotionEvent lastTouchEvent;
    private PointF touchDown;               // 按下的位置
    private int longPressThreshold = 500;   // 触发长按的阀值
    private boolean inLongPressCheck;

    public InputView(ImeEngine engine) {
        this(engine.getContext(), engine);
    }

    public InputView(Context context, ImeEngine engine) {
        super(context);
        this.engine = engine;
        Logs.d(Strings.simpleFormat("create InputView: 0x%x.", hashCode()));
        init();
        setupPainter();
        applyTheme(Setting.getInstance()
                          .getString(Setting.kTheme));
    }

    public static boolean isDrawPath() {
        return drawPath;
    }

    public static void setDrawPath(boolean drawPath) {
        InputView.drawPath = drawPath;
    }

    public boolean isPainterValid() {
        return surfaceHolder != null && painter != null;
    }

    @Override public void surfaceCreated(@NonNull SurfaceHolder holder) {
        Logs.i("surfaceCreated");
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
        Logs.i("surfaceChanged");
        this.surfaceHolder = holder;
        repaint();
    }

    @Override public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        Logs.i("surfaceDestroyed");
        engine.unregisterHandler(TAG + "-handler");
        painter = null;
    }

    @Override public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        final int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                inLongPressCheck = true;
                Logs.d("touchDown @(" + x + "," + y + ")");
                touchDown = new PointF(x, y);
                lastTouchEvent = event;
                path.moveTo(x, y);
                findWidgetContains(touchDown).onTouchStart(touchDown);
                // painter.sendEmptyMessage(FimeMessage.MSG_CHECK_LONG_PRESS);
                engine.notifyHandlers(FimeMessage.create(FimeMessage.MSG_CHECK_LONG_PRESS));
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
                if (!inLongPressCheck) findWidgetContains(pos).onTouchMove(pos);
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
                    findWidgetContains(center).onTouchEnd(center);
                }
                touchDown = null;
                lastTouchEvent = null;
                performClick();
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_OUTSIDE:
            default:
                Logs.d("Ignored touch event: " + action);
                path.reset();
                touchDown = null;
                lastTouchEvent = null;
                inLongPressCheck = false;
                break;
        }
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

    void applyTheme(String name) {
        if (themes.isEmpty()) {
            File file = FimeContext.getInstance()
                                   .fileInAppHome("default.conf");
            try {
                Config config = Configs.load(file, true)
                                       .getConfig("theme");
                Set<String> names = config.root()
                                          .unwrapped()
                                          .keySet();
                for (String key : names) {
                    themes.add(new Theme(config, key));
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            if (themes.isEmpty()) themes.add(new Theme());  // fallback to default.
        }
        if (name == null) return;

        for (Theme theme : themes) {
            if (name.equals(theme.getName())) {
                Logs.d("applyTheme: %s", name);
                actionBar.applyTheme(theme);
                keyboards.applyTheme(theme);
                repaint();
                break;
            }
        }
    }

    void useSchema(String conf) {
        engine.useSchema(conf);
    }

    void commitText(String text) {
        engine.commitText(text);
    }

    private boolean handle(@NonNull Message msg) {
        switch (msg.what) {
            case FimeMessage.MSG_REPAINT:
            case FimeMessage.MSG_CANDIDATE_CHANGE:
                // painter.post(this::repaint);
                engine.post(this::repaint);
                return true;
            case FimeMessage.MSG_CHECK_LONG_PRESS:
                Logs.d("MSG_CHECK_LONG_PRESS");
                if (inLongPressCheck && touchDown != null && lastTouchEvent != null) {
                    long eventTime = lastTouchEvent.getEventTime();
                    long downTime = lastTouchEvent.getDownTime();
                    Logs.d("eventTime=" + eventTime + ", downTime=" + downTime);
                    final long uptimeMillis = SystemClock.uptimeMillis();
                    if (eventTime == downTime) { // 没有触发移动事件
                        if (uptimeMillis - downTime >= longPressThreshold) {
                            findWidgetContains(touchDown).onLongPress(touchDown,
                                                                      uptimeMillis - downTime);
                        }
                        else {
                            Logs.d("longPress time too short.");
                        }
                    }
                    else {  // 按下并移动了一小段距离
                        if (eventTime - downTime >= longPressThreshold) {
                            findWidgetContains(touchDown).onLongPress(touchDown,
                                                                      uptimeMillis - downTime);
                        }
                        else {
                            Logs.d("longPress time too short.");
                        }
                    }
                    engine.notifyHandlersDelay(FimeMessage.create(msg.what), 100);
                    // painter.sendEmptyMessageDelayed(msg.what, 100);
                }
                return true;
            case FimeMessage.MSG_APPLY_THEME:
                if (msg.obj instanceof String) applyTheme((String) msg.obj);
                return true;
        }
        Logs.d("Ignored message:0x%02x", msg.what);
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
        if (BuildConfig.DEBUG) setDrawPath(true);
    }

    private void setupPainter() {
        engine.unregisterHandler(TAG + "-handler");
        painter = new DefaultFimeHandler(TAG + "-handler") {
            @Override public boolean handleOnce(@NonNull Message msg) {
                return InputView.this.handle(msg);
            }

            @Override public void send(@NonNull Message msg) {
                engine.notifyHandlers(msg);
            }
        };
        engine.registerHandler(painter);
    }

    private void repaint() {
        Logs.d("repaint");
        try {
            canvas = surfaceHolder.lockCanvas();
            if (canvas == null) {
                Logs.w("Canvas is null, can not paint!");
            }
            else {
                actionBar.onDraw(canvas, actionBar.getContainer(), painter);
                keyboards.getCurrent()
                         .onDraw(canvas,
                                 new Box(new PointF(0, actionBarHeight), container.getWidth(),
                                         container.getHeight() - actionBarHeight), painter);
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
}
