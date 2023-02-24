package top.someapp.fimesdk.view;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.Log;
import androidx.annotation.NonNull;
import com.typesafe.config.Config;
import top.someapp.fimesdk.Fime;
import top.someapp.fimesdk.api.FimeMessage;
import top.someapp.fimesdk.api.ImeEngine;
import top.someapp.fimesdk.api.ImeEngineAware;
import top.someapp.fimesdk.api.Schema;
import top.someapp.fimesdk.config.Configs;
import top.someapp.fimesdk.config.Keycode;
import top.someapp.fimesdk.utils.Effects;
import top.someapp.fimesdk.utils.Fonts;
import top.someapp.fimesdk.utils.Geometry;
import top.someapp.fimesdk.utils.Strings;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @author zwz
 * Created on 2023-02-07
 */
public class Keyboards implements ImeEngineAware, Widget.OnVirtualKeyListener {

    private static final String TAG = Fime.makeTag("Keyboards");
    private static Pattern kOnTapActionReg = Pattern.compile(
            "([A-Za-z][0-9]?)+[(]([A-Za-z0-9]+,?)+[)]");
    private final Map<String, Keyboard> keyboardMap = new LinkedHashMap<>();
    private Config config;
    private ImeEngine engine;
    private int width;  // px
    private String name;
    private Keyboard current;
    private String defaultKeyboardId;

    public Keyboards(File file) {
        this(Configs.load(file, true));
    }

    public Keyboards(Config config) {
        this.config = config;
        this.width = Geometry.getDisplayMetrics().widthPixels;
        load();
    }

    public Config getConfig() {
        return config;
    }

    public boolean hasKeyboard(String name) {
        return keyboardMap.containsKey(name);
    }

    public String getName() {
        return name;
    }

    public Widget getCurrent() {
        return current;
    }

    public void build() {

    }

    public float getHeightPx() {
        return current.container.getHeight();
    }

    @Override public void setup(@NonNull ImeEngine engine) {
        this.engine = engine;
    }

    @Override public boolean onTap(VirtualKey virtualKey) {
        if (engine == null) return false;
        if (Strings.isNullOrEmpty(virtualKey.getOnTap())) {
            engine.onTap(castIfShiftHold(virtualKey));
        }
        else {
            onTapAction(virtualKey.getOnTap());
        }
        return true;
    }

    @Override public boolean onLongPress(VirtualKey virtualKey, long durations) {
        int count = (int) Math.max(2, durations / 200);
        if (count > 10) count = 10;
        VirtualKey key = castIfShiftHold(virtualKey);
        for (int i = 1; i <= count; i++) {
            if (!onTap(key)) return false;
        }
        return true;
    }

    void onTapAction(String action) {
        if (kOnTapActionReg.matcher(action)
                           .matches()) {
            String action_ = action.substring(0, action.indexOf('('));
            String[] args = action.substring(action_.length() + 1, action.length() - 1)
                                  .split("[,]");
            onTapAction(action_, args);
        }
        else {
            Log.w(TAG, "Invalid onTapAction: " + action);
        }
    }

    Keyboard getKeyboard(String name) {
        if (keyboardMap.containsKey(name)) return keyboardMap.get(name);
        return null;
    }

    private VirtualKey castIfShiftHold(VirtualKey virtualKey) {
        VirtualKey key = virtualKey;
        if (current.shiftHold) {
            if (Keycode.isLetterLowerCode(virtualKey.getCode())) {  // lower -> UPPER
                Log.d(TAG, "castIfShiftHold: lower -> UPPER.");
                key = new VirtualKey(virtualKey.getCode() - Keycode.VK_a + Keycode.VK_A);
            }
            else if (Keycode.isLetterUpperCode(virtualKey.getCode())) { // UPPER -> lower
                Log.d(TAG, "castIfShiftHold: UPPER -> lower.");
                key = new VirtualKey(virtualKey.getCode() - Keycode.VK_A + Keycode.VK_a);
            }
        }
        return key;
    }

    private void onTapAction(String action, String... args) {
        if ("useKeyboard".equals(action)) {
            useKeyboard(args);
        }
        else if ("activeOption".equals(action)) {
            activeOption(args);
        }
        else if ("toggleOption".equals(action)) {
            toggleOption(args);
        }
    }

    private void useKeyboard(String... args) {
        String prev = current.id;
        String id = args[0];
        if (hasKeyboard(id)) {
            current = getKeyboard(id);
        }
        if (!prev.equals(current.id)) { // 切换键盘时，启用/停用 中文输入
            if (engine != null) {
                if (current.id.equals(defaultKeyboardId)) {
                    engine.setMode(ImeEngine.CN_MODE);
                }
                else {
                    engine.setMode(ImeEngine.ASCII_MODE);
                }
                engine.notifyHandlers(FimeMessage.createRepaintMessage());
                engine.enterState(ImeEngine.ImeState.READY);
            }
        }
    }

    private void activeOption(String... args) {
        if (engine == null) return;
        Schema schema = engine.getSchema();
        String key = null;
        for (int i = 0, len = args.length; i < len; i++) {
            if (i % 2 == 0) {
                key = args[i];
                continue;
            }
            if (key != null) {
                int index = Integer.decode(args[i]);
                schema.activeOption(key, index);
            }
        }
    }

    private void toggleOption(String... args) {
        if (engine == null) return;
        Schema schema = engine.getSchema();
        String key = args[0];
        schema.toggleOption(key);
    }

    private void load() {
        name = config.getString("name");
        List<String> keyboards = config.getStringList("keyboards");
        for (String id : keyboards) {
            if (config.hasPath(id)) {
                Keyboard keyboard = new Keyboard(width, id, config.getConfig(id));
                keyboard.setOnVirtualKeyListener(this);
                keyboardMap.put(id, keyboard);
            }
            else {
                Log.w(TAG, "Keyboard " + id + " is missing!");
            }
        }
        defaultKeyboardId = keyboards.get(0);
        current = getKeyboard(defaultKeyboardId);
    }

    static class Keyboard implements Widget {

        private static final String TAG = Fime.makeTag("Keyboard");
        private final List<VirtualKey> keyList = new ArrayList<>(32);
        private final Set<Integer> holdOnKeyIndex = new HashSet<>(3);
        private final Paint paint = new Paint();
        private final int width;
        private final Style style;
        private final String id;
        private String name;
        private Box container;
        private OnVirtualKeyListener keyListener;
        private Bitmap bitmap;
        private boolean dirty;
        private boolean shiftHold;  // shift 键是否被按下
        private VirtualKey prevKey;
        private PointF firstTouchAt;

        Keyboard(int width, String id, @NonNull Config config) {
            this.width = width;
            this.id = id;
            this.style = new Style();
            init(config);
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override public Box getContainer() {
            return container;
        }

        @Override public void onDraw(Canvas canvas, Box box) {
            Log.d(TAG, "onDraw, dirty=" + dirty);
            container = box;
            int width = (int) box.getWidth();
            int height = (int) box.getHeight();
            PointF offset = box.getPosition();
            paint.reset();  // 重置使抗锯齿再次生效
            paint.setAntiAlias(true);   // 抗锯齿
            if (bitmap == null || dirty || bitmap.getWidth() != width || bitmap.getHeight() != height) {
                bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                Canvas kbdCanvas = new Canvas(bitmap);
                kbdCanvas.drawColor(style.getBackgroundColor());
                for (VirtualKey key : keyList) {
                    drawKey(kbdCanvas, key);
                }
                kbdCanvas.save();
                dirty = false;
            }
            canvas.drawBitmap(bitmap, offset.x, offset.y, paint);
        }

        @Override public boolean onTouchStart(PointF pos) {
            firstTouchAt = new PointF(pos.x, pos.y);
            releaseKeys();
            VirtualKey key = findKeyAt(pos);
            if (key == null) {
                dirty = false;
                return false;
            }
            pressKeyDown(key);
            if (key.isShift()) shiftHold = !shiftHold;
            dirty = true;
            prevKey = key;
            Effects.playSound();
            return true;
        }

        @Override public boolean onTouchMove(PointF pos) {
            if (firstTouchAt != null) {
                double distance = Geometry.distanceBetweenPoints(firstTouchAt, pos);
                Log.d(TAG, "onTouchMove, distance=" + distance);
                if (holdOnKeyIndex.isEmpty()) {
                    return false;
                }
                if (keyListener != null && distance <= 16) {
                    VirtualKey key = findKeyAt(pos);
                    if (key != null && !key.isShift()) {
                        keyListener.onTap(key);
                    }
                }
                dirty = releaseKeys();
                return dirty;
            }
            return false;
        }

        @Override public boolean onTouchEnd(PointF pos) {
            VirtualKey key = findKeyAt(pos);
            if (key == null || keyList.isEmpty()) {
                dirty = releaseKeys();
                return dirty;
            }

            dirty = true;
            if (keyListener != null) keyListener.onTap(key);
            releaseKeys();
            return true;
        }

        @Override public boolean onLongPress(PointF pos, long durations) {
            Log.d(TAG, "onLongPress, durations=" + durations);
            VirtualKey key = findKeyAt(pos);
            if (key != null && keyListener != null) {
                keyListener.onLongPress(key, durations);
                return true;
            }
            return false;
        }

        @Override public void setOnVirtualKeyListener(OnVirtualKeyListener virtualKeyListener) {
            this.keyListener = virtualKeyListener;
        }

        public VirtualKey findKeyAt(PointF pos) {
            PointF point = new PointF(pos.x - container.getLeft(), pos.y - container.getTop());
            for (VirtualKey key : keyList) {
                if (key.getContainer()
                       .surround(point)) {
                    return key;
                }
            }
            return null;
        }

        private boolean releaseKeys() {
            if (holdOnKeyIndex.isEmpty()) return false;

            Iterator<Integer> it = holdOnKeyIndex.iterator();
            int size = holdOnKeyIndex.size();
            while (it.hasNext()) {
                VirtualKey key = keyList.get(it.next());
                if (key.isShift() && shiftHold) {
                    continue;
                }
                it.remove();
            }
            return size != holdOnKeyIndex.size();
        }

        private boolean pressKeyDown(VirtualKey key) {
            if (holdOnKeyIndex.contains(key.index)) return false;
            holdOnKeyIndex.add(key.index);
            return true;
        }

        private void init(Config config) {
            // 简单的.9图在线生成器 http://inloop.github.io/shadow4android/
            if (config.hasPath("name")) this.name = config.getString("name");
            final float height = Geometry.dp2px(config.getDouble("height"));    // dp -> px
            container = new Box(width, height);
            float keyWidth = config.getNumber("keyWidth")
                                   .floatValue() * width / 100.0f;
            float keyHeight = Geometry.dp2px(config.getDouble("keyHeight"));   // dp -> px
            if (config.hasPath("style")) {
                style.applyFrom(new Style(config.getConfig("style")));
            }
            Config keys = config.getConfig("keys");
            Style keyStyle = new Style();
            if (keys.hasPath("style")) {
                keyStyle.applyFrom(new Style(keys.getConfig("style")));
            }
            List<? extends Config> items = keys.getConfigList("items");
            PointF position = new PointF();
            for (Config item : items) {
                VirtualKey key = null;
                float dx = 0;
                float dy = 0;
                Style style = null;
                if (item.hasPath("name")) {
                    Keycode keycode = Keycode.getByName(item.getString("name"));
                    key = new VirtualKey(keycode.code);
                    if (item.hasPath("label")) key.setLabel(item.getString("label"));
                    if (item.hasPath("text")) key.setText(item.getString("text"));
                    if (item.hasPath("width")) {
                        key.setWidth((float) (item.getDouble("width") * width / 100));
                    }
                    else {
                        key.setWidth(keyWidth);
                    }
                    if (item.hasPath("height")) {
                        key.setHeight(Geometry.dp2px(item.getDouble("height")));    // dp -> px
                    }
                    else {
                        key.setHeight(keyHeight);
                    }
                    if (item.hasPath("style")) {
                        style = new Style(item.getConfig("style"));
                    }
                    else {
                        style = null;
                    }
                    if (item.hasPath("onTap")) {
                        key.setOnTap(item.getString("onTap"));
                    }
                    if (item.hasPath("offset")) {
                        List<Integer> offset = item.getIntList("offset");
                        key.setPosition(new PointF(position.x + Geometry.dp2px(offset.get(0)),
                                                   position.y + Geometry.dp2px(offset.get(1))));
                    }
                    else {
                        key.setPosition(new PointF(position.x, position.y));
                    }
                    dx = key.getWidth();
                }
                else if (item.hasPath("move")) {
                    String move = item.getString("move");
                    if ("toNextRow".equals(move)) {
                        position.x = 0;
                        position.y += keyHeight;
                        continue;
                    }
                    else if ("right".equals(move)) {
                        dx = item.hasPath("value") ? width * (float) item.getDouble(
                                "value") / 100 : keyWidth;
                    }
                    else if ("left".equals(move)) {
                        dx = -(item.hasPath("value") ? width * (float) item.getDouble(
                                "value") / 100 : keyWidth);
                    }
                }
                if (key != null) {
                    key.index = keyList.size();
                    if (style == null) {
                        key.setStyle(new Style().applyFrom(keyStyle));
                    }
                    else {
                        key.setStyle(style);
                    }
                    keyList.add(key);
                }
                position.x += dx;
                position.y += dy;
                if (position.x >= width) {
                    position.x = 0;
                }
            }
        }

        private void drawKey(Canvas canvas, VirtualKey key) {
            Style style = key.getStyle();
            if (holdOnKeyIndex.contains(key.index) || (shiftHold && key.isShift())) {
                style = style.reverseColors();
            }
            key.getContainer()
               .render(canvas, paint, style);
            // 使用矢量图作为按键背景的示例
            //     Rect content = key.getContainer()
            //                       .toRect();
            //     PointF margin = style.getMargin();
            //     content.left += margin.x / 2;
            //     content.top += margin.y / 2;
            //     content.right -= margin.x / 2;
            //     content.bottom -= margin.y / 2;
            //     if (key.isFunctional()) {
            //         Bitmap bitmap = Geometry.drawableToBitmap(fnKeyBackground, content.width(),
            //                                                   content.height());
            //         canvas.drawBitmap(bitmap, content.left, content.top, paint);
            //     }
            //     else {
            //         Bitmap bitmap = Geometry.drawableToBitmap(keyBackground, content.width(),
            //                                                   content.height());
            //         canvas.drawBitmap(bitmap, content.left, content.top, paint);
            //     }
            // paint 使用过后，好像抗锯齿会恢复为默认值!
            paint.reset();
            paint.setAntiAlias(true);
            paint.setColor(style.getColor());
            paint.setStyle(Paint.Style.FILL_AND_STROKE);
            paint.setTextSize(style.getFontSize()); // style already using dp
            String label = shiftHold ? key.getLabel()
                                          .toUpperCase(Locale.US) : key.getLabel();
            Box textBounds = Fonts.getTextBounds(paint, label);
            float dx = Math.max(0, (key.getWidth() - textBounds.getWidth()) / 2);
            float dy = key.getHeight() * 0.20f;
            canvas.drawText(label, key.getPosition().x + dx, key.getCenter().y + dy,
                            paint);
        }
    }
}
