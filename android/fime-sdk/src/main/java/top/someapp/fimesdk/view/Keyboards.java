package top.someapp.fimesdk.view;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.Message;
import androidx.annotation.NonNull;
import com.typesafe.config.Config;
import top.someapp.fimesdk.api.DefaultFimeHandler;
import top.someapp.fimesdk.api.FimeHandler;
import top.someapp.fimesdk.api.FimeMessage;
import top.someapp.fimesdk.api.ImeEngine;
import top.someapp.fimesdk.api.ImeEngineAware;
import top.someapp.fimesdk.api.Schema;
import top.someapp.fimesdk.config.Configs;
import top.someapp.fimesdk.config.Keycode;
import top.someapp.fimesdk.utils.Effects;
import top.someapp.fimesdk.utils.Fonts;
import top.someapp.fimesdk.utils.Geometry;
import top.someapp.fimesdk.utils.Logs;
import top.someapp.fimesdk.utils.Strings;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
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

    private static final Pattern kOnTapActionReg = Pattern.compile(
            "([A-Za-z][0-9]?)+[(]([A-Za-z0-9]+,?)+[)]");
    private final Map<String, Keyboard> keyboardMap = new LinkedHashMap<>();
    private final Config config;
    private ImeEngine engine;
    private int width;  // px
    private String name;
    private Keyboard current;
    private Theme theme;
    private String defaultKeyboardId;

    public Keyboards(File file) {
        this(Configs.load(file, true));
    }

    public Keyboards(Config config) {
        this.config = config;
        this.width = Geometry.getDisplayMetrics().widthPixels;
        theme = new Theme();    // ensure theme is not null!
        theme.setName("");
        theme.setBackground(0xe4e5ea);
        theme.setText(0);
        theme.setSecondaryText(0x979797);
        theme.setKeyBackground(0xfcfcfe);
        theme.setBorderColor(0x9c9ca0);
        theme.setFnBackground(0xb8bcc3);
        theme.setKeyLabelSize(16.5f);
        theme.setBorderWidth(1);
        theme.setBorderRadius(6);
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
        engine.unregisterHandler("KeyLabelUpdater");
        engine.registerHandler(new DefaultFimeHandler("KeyLabelUpdater") {
                                   @Override public boolean handleOnce(@NonNull Message msg) {
                                       return Keyboards.this.handle(msg);
                                   }
                               }
        );
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
        for (int i = 1; i <= count; i++) {
            if (!onTap(virtualKey)) return false;
        }
        return true;
    }

    public void applyTheme(Theme theme) {
        this.theme = theme;
        for (Keyboard kbd : keyboardMap.values()) {
            kbd.applyTheme(theme);
        }
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
            Logs.w("Invalid onTapAction: ", action);
        }
    }

    Keyboard getKeyboard(String name) {
        if (keyboardMap.containsKey(name)) return keyboardMap.get(name);
        return null;
    }

    private boolean handle(@NonNull Message msg) {
        if (msg.what == FimeMessage.MSG_INPUT_CHANGE) {
            if (current.dynamicLabel == null) {
                current.requestRepaint();
            }
            else {
                Logs.d("handle MSG_INPUT_CHANGE.");
                String code = engine.getSchema()
                                    .getInputEditor()
                                    .getLastSegment();
                current.updateKeyLabels(code);
            }
            return true;
        }
        return false;
    }

    private VirtualKey castIfShiftHold(VirtualKey virtualKey) {
        VirtualKey key = virtualKey;
        if (current.shiftHold) {
            if (Keycode.isLetterLowerCode(virtualKey.getCode())) {  // lower -> UPPER
                Logs.d("castIfShiftHold: lower -> UPPER.");
                key = new VirtualKey(virtualKey.getCode() - Keycode.VK_a + Keycode.VK_A);
            }
            else if (Keycode.isLetterUpperCode(virtualKey.getCode())) { // UPPER -> lower
                Logs.d("castIfShiftHold: UPPER -> lower.");
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
                    current.updateKeyLabels("");
                }
                else {
                    engine.setMode(ImeEngine.ASCII_MODE);
                }
                engine.notifyHandlers(FimeMessage.create(FimeMessage.MSG_REPAINT));
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
                Keyboard keyboard = new Keyboard(width, id, config.getConfig(id), theme);
                keyboard.setOnVirtualKeyListener(this);
                keyboardMap.put(id, keyboard);
            }
            else {
                Logs.w("Keyboard " + id + " is missing!");
            }
        }
        defaultKeyboardId = keyboards.get(0);
        current = getKeyboard(defaultKeyboardId);
    }

    static class Keyboard implements Widget {

        private final List<VirtualKey> keyList = new ArrayList<>(32);
        private final Set<Integer> holdOnKeyIndex = new HashSet<>(3);
        private final Paint paint = new Paint();
        private final int width;
        private final String id;
        // private Style style;
        private Theme theme;
        private FimeHandler painter;
        private String name;
        private Box container;
        private OnVirtualKeyListener keyListener;
        private Bitmap bitmap;
        private Canvas canvas;
        private boolean dirty;
        private boolean shiftHold;  // shift 键是否被按下
        @SuppressWarnings("unused")
        private VirtualKey prevKey;
        private PointF firstTouchAt;
        private Config dynamicLabel;
        private Set<VirtualKey> dynamicLabelKeys;

        Keyboard(int width, String id, @NonNull Config config, @NonNull Theme theme) {
            this.width = width;
            this.id = id;
            this.theme = theme;
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

        @Override public void applyTheme(Theme theme) {
            if (!this.theme.getName()
                           .equals(theme.getName())) {
                dirty = true;
            }
            this.theme = theme;
        }

        @Override public void onDraw(Canvas canvas, Box box, FimeHandler painter) {
            Logs.d("onDraw, dirty=" + dirty);
            container = box;
            this.painter = painter;
            int width = (int) box.getWidth();
            int height = (int) box.getHeight();
            PointF offset = box.getPosition();
            paint.reset();  // 重置使抗锯齿再次生效
            paint.setAntiAlias(true);   // 抗锯齿
            this.canvas = canvas;
            if (bitmap == null || dirty || bitmap.getWidth() != width || bitmap.getHeight() != height) {
                bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                drawKeyboard();
                dirty = false;
            }
            else {
                canvas.drawBitmap(bitmap, offset.x, offset.y, paint);
            }
        }

        @Override public void onTouchStart(PointF pos) {
            firstTouchAt = new PointF(pos.x, pos.y);
            releaseKeys();
            VirtualKey key = findKeyAt(pos);
            if (key == null) {
                return;
            }
            pressKeyDown(key);
            if (key.isShift()) shiftHold = !shiftHold;
            prevKey = key;
            Effects.playSound();
        }

        @Override public void onTouchMove(PointF pos) {
            if (firstTouchAt != null) {
                double distance = Geometry.distanceBetweenPoints(firstTouchAt, pos);
                Logs.d("onTouchMove, distance=" + distance);
                if (holdOnKeyIndex.isEmpty()) {
                    return;
                }
                if (keyListener != null && distance <= 16) {
                    VirtualKey key = findKeyAt(pos);
                    if (key != null && !key.isShift()) {
                        keyListener.onTap(key);
                    }
                }
                releaseKeys();
            }
        }

        @Override public void onTouchEnd(PointF pos) {
            VirtualKey key = findKeyAt(pos);
            if (key == null || keyList.isEmpty()) {
                releaseKeys();
                return;
            }

            if (keyListener != null) keyListener.onTap(key);
            releaseKeys();
        }

        @Override public void onLongPress(PointF pos, long durations) {
            Logs.d("onLongPress, durations=" + durations);
            VirtualKey key = findKeyAt(pos);
            if (key != null && keyListener != null) {
                keyListener.onLongPress(key, durations);
            }
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

        private void init(Config config) {
            // 简单的.9图在线生成器 http://inloop.github.io/shadow4android/
            if (config.hasPath("name")) this.name = config.getString("name");
            final float height = Geometry.dp2px(config.getDouble("height"));    // dp -> px
            container = new Box(width, height);
            float keyWidth = config.getNumber("keyWidth")
                                   .floatValue() * width / 100.0f;
            float keyHeight = Geometry.dp2px(config.getDouble("keyHeight"));   // dp -> px
            dynamicLabel = config.hasPath("dynamic-label") ? config.getConfig(
                    "dynamic-label") : null;
            List<String> dynamicLabelNames = new ArrayList<>();
            if (config.hasPath("dynamic-label")) {
                dynamicLabelKeys = new HashSet<>();
                dynamicLabelNames = dynamicLabel.getStringList("names");
            }
            Config keys = config.getConfig("keys");
            final boolean ceilOn = keys.hasPath("ceil") && keys.getBoolean("ceil");
            final boolean floorOn = keys.hasPath("floor") && keys.getBoolean("floor");
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
                    if (dynamicLabel != null && dynamicLabelNames.contains(keycode.name)) {
                        dynamicLabelKeys.add(key);
                    }
                    if (item.hasPath("label")) key.setLabel(item.getString("label"));
                    if (item.hasPath("text")) key.setText(item.getString("text"));
                    if (ceilOn && item.hasPath("ceil")) key.setCeil(item.getString("ceil"));
                    if (floorOn && item.hasPath("floor")) key.setFloor(item.getString("floor"));
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
                    if (item.hasPath("onTap")) {
                        key.setOnTap(item.getString("onTap"));
                    }
                    if (item.hasPath("style")) style = new Style(item.getConfig("style"));
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
                    key.setTheme(theme);
                    key.setStyle(style);
                    keyList.add(key);
                }
                position.x += dx;
                position.y += dy;
                if (position.x >= width) {
                    position.x = 0;
                }
            }
        }

        private void updateKeyLabels(final String input) {
            if (dynamicLabel == null || dynamicLabelKeys == null || dynamicLabelKeys.isEmpty()) {
                requestRepaint();
                return;
            }

            Map<Integer, String> labelMap = new HashMap<>();
            Config labels = dynamicLabel.getConfig("labels");
            List<String> names = dynamicLabel.getStringList("names");
            final Keycode first = Keycode.getByName(names.get(0));
            List<String> newLabels = dynamicLabel.getStringList("init");
            if (!Strings.isNullOrEmpty(input) && labels.hasPath(input)) {
                newLabels = labels.getStringList(input);
            }
            for (int i = 0; i < newLabels.size(); i++) {
                labelMap.put(i + first.code, newLabels.get(i));
            }
            for (VirtualKey key : dynamicLabelKeys) {
                if (labelMap.containsKey(key.getCode())) {
                    key.setLabel(labelMap.get(key.getCode()));
                }
            }
            Logs.d(labelMap.toString());
            if (!labelMap.isEmpty()) requestRepaint();
        }

        private void releaseKeys() {
            if (holdOnKeyIndex.isEmpty()) return;

            Iterator<Integer> it = holdOnKeyIndex.iterator();
            int size = holdOnKeyIndex.size();
            while (it.hasNext()) {
                VirtualKey key = keyList.get(it.next());
                if (key.isShift() && shiftHold) {
                    continue;
                }
                it.remove();
            }
            if (size != holdOnKeyIndex.size()) requestRepaint();
        }

        private void pressKeyDown(VirtualKey key) {
            if (holdOnKeyIndex.contains(key.index)) return;
            holdOnKeyIndex.add(key.index);
            requestRepaint();
        }

        private void requestRepaint() {
            dirty = true;
            if (painter != null) painter.send(FimeMessage.create(FimeMessage.MSG_REPAINT));
        }

        private void drawKeyboard() {
            PointF offset = container.getPosition();
            Canvas kbdCanvas = new Canvas(bitmap);
            kbdCanvas.drawColor(theme.getBackground());
            for (VirtualKey key : keyList) {
                Theme keyTheme = theme.copy();
                if (key.isFunctional()) keyTheme.setKeyBackground(theme.getFnBackground());
                if (holdOnKeyIndex.contains(key.index) || (shiftHold && key.isShift())) {
                    keyTheme = theme.reverseColors();
                }
                if (key.getStyle() == null) {
                    key.getContainer()
                       .render(kbdCanvas, paint, keyTheme);
                }
                else {
                    key.getContainer()
                       .render(kbdCanvas, paint, key.getStyle()
                                                    .with(keyTheme));
                }
                // paint 使用过后，好像抗锯齿会恢复为默认值!
                paint.reset();
                paint.setAntiAlias(true);
                paint.setColor(keyTheme.getText());
                paint.setStyle(Paint.Style.FILL_AND_STROKE);
                paint.setTextSize(Geometry.dp2px(
                        theme.getKeyLabelSize())); // config value using dp, draw using px
                String label = shiftHold ? key.getLabel()
                                              .toUpperCase(Locale.US) : key.getLabel();
                Box textBounds = Fonts.getTextBounds(paint, label);
                float dx = Math.max(0, (key.getWidth() - textBounds.getWidth()) / 2);
                float dy = key.getHeight() * 0.15f;
                kbdCanvas.drawText(label, key.getPosition().x + dx, key.getCenter().y + dy,
                                   paint);
                if (!Strings.isNullOrEmpty(key.getCeil())) {
                    paint.setColor(keyTheme.getSecondaryText());
                    paint.setTextSize(Geometry.dp2px(2 * theme.getKeyLabelSize() / 3));
                    kbdCanvas.drawText(key.getCeil(),
                                       key.getContainer()
                                          .getLeft() + 24,
                                       key.getContainer()
                                          .getTop() + 32,
                                       paint);
                }
                if (!Strings.isNullOrEmpty(key.getFloor())) {
                    paint.setColor(keyTheme.getSecondaryText());
                    paint.setTextSize(Geometry.dp2px(2 * theme.getKeyLabelSize() / 3));
                    kbdCanvas.drawText(key.getFloor(),
                                       key.getContainer()
                                          .getLeft() + 24,
                                       key.getContainer()
                                          .getBottom() - 16,
                                       paint);
                }
            }
            canvas.drawBitmap(bitmap, offset.x, offset.y, paint);
            kbdCanvas.save();
        }
    }
}
