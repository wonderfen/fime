package top.someapp.fimesdk.view;

import android.graphics.PointF;
import top.someapp.fimesdk.config.Keycode;

/**
 * @author zwz
 * Created on 2022-12-21
 */
public class VirtualKey {

    private final int code;
    int index;  // 在键盘中的序号
    private String label;   // 绘制时的文本
    private String text;    // 点击时使用的文本
    private Box container;
    private Style style;
    private boolean repeatable;
    private String onTap;

    public VirtualKey(int code) {
        this(code, Keycode.getByCode(code).label);
    }

    public VirtualKey(int code, String label) {
        this.code = code;
        this.label = label == null ? "" : label;
        this.container = new Box(16, 16);
        this.style = new Style();
        this.repeatable = Keycode.isRepeatable(code);
    }

    public int getIndex() {
        return index;
    }

    public int getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isFunctional() {
        return Keycode.isFnKeyCode(code);
    }

    public Box getContainer() {
        return container;
    }

    public void setContainer(Box container) {
        this.container = container;
    }

    public Style getStyle() {
        return style;
    }

    public void setStyle(Style style) {
        this.style = style;
    }

    public float getWidth() {
        return container.getWidth();
    }

    public void setWidth(float width) {
        container.setWidth(width);
    }

    public float getHeight() {
        return container.getHeight();
    }

    public void setHeight(float height) {
        container.setHeight(height);
    }

    public PointF getPosition() {
        return container.getPosition();
    }

    public void setPosition(PointF position) {
        container.setPosition(position);
    }

    public PointF getCenter() {
        return container.getCenter();
    }

    public boolean isShift() {
        return code == Keycode.VK_FN_SHIFT;
    }

    public boolean isRepeatable() {
        return repeatable;
    }

    public String getOnTap() {
        return onTap;
    }

    public void setOnTap(String onTap) {
        this.onTap = onTap;
    }

    @Override public String toString() {
        return "VirtualKey{" +
                "code=" + code +
                ", label='" + label + '\'' +
                ", center=" + getCenter() +
                '}';
    }
}
