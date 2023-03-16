package top.someapp.fimesdk.view;

import android.graphics.Color;
import android.graphics.PointF;
import com.typesafe.config.Config;
import top.someapp.fimesdk.utils.Geometry;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 部分支持的样式
 *
 * @author zwz
 * Created on 2022-12-29
 */
public class Style {

    private final Set<String> properties = new HashSet<>();
    private String background;
    private float borderWidth = 1.0f;
    private float borderRadius = 6.0f;
    private int borderColor = 0xff9c9ca0;
    private int backgroundColor = 0xffe4e5ea;
    private int color = Color.BLACK;
    private PointF margin = new PointF();
    private float fontSize = 16.5f;

    public Style() {
    }

    public Style(Config config) {
        // background: 0xB8BCC3
        // border-width: 1 // dp
        // border-color: 0xA0A8B6
        // border-radius: 6  // dp
        // margin: [8, 8] // dp 上下, 左右
        // color: 0x000000
        // font-size: 36 // dp
        if (config.hasPath("background")) {
            properties.add("background");
            backgroundColor = parseColor(config.getString("background"), Color.BLACK);
        }
        if (config.hasPath("border-width")) {
            properties.add("border-width");
            borderWidth = Geometry.dp2px(config.getDouble("border-width"));
        }
        if (config.hasPath("border-color")) {
            properties.add("border-color");
            borderColor = parseColor(config.getString("border-color"), Color.BLACK);
        }
        if (config.hasPath("border-radius")) {
            properties.add("border-radius");
            borderRadius = Geometry.dp2px(config.getDouble("border-radius"));
        }
        if (config.hasPath("margin")) {
            properties.add("margin");
            List<Double> margin = config.getDoubleList("margin");
            this.margin.x = Geometry.dp2px(margin.get(1));
            this.margin.y = Geometry.dp2px(margin.get(0));
        }
        if (config.hasPath("color")) {
            properties.add("color");
            color = parseColor(config.getString("color"), Color.BLACK);
        }
        if (config.hasPath("font-size")) {
            properties.add("font-size");
            fontSize = Geometry.dp2px(config.getDouble("font-size"));
        }
    }

    private static int parseColor(String value, int defaultColor) {
        int color = -1;
        if (value == null || value.length() == 0 || "none".equals(
                value) || "transparent".equals(value)) {
            color = Color.TRANSPARENT;
        }
        else {
            try {
                color = Color.parseColor(value);
            }
            catch (Exception e) {
                // ignored
            }
            if (color < 0) {
                try {
                    color = Integer.decode(value);
                }
                catch (NumberFormatException e) {
                    color = Color.BLACK;
                }
            }
        }
        return color < 0 ? defaultColor : color | 0xff000000;
    }

    private static int reverseColor(int color) {
        return (~color) | 0xff000000;
    }

    public Style applyFrom(Style other) {
        background = other.background;
        setBackgroundColor(other.getBackgroundColor());
        setBorderWidth(other.getBorderWidth());
        setBorderRadius(other.getBorderRadius());
        setBorderColor(other.getBorderColor());
        setMargin(other.getMargin());
        setColor(other.getColor());
        setFontSize(other.getFontSize());
        return this;
    }

    public Style applyTo(Style other) {
        other.applyFrom(this);
        return other;
    }

    public Style with(Theme theme) {
        if (!properties.contains("background")) setBackgroundColor(theme.getKeyBackground());
        if (!properties.contains("border-width")) setBorderWidth(theme.getBorderWidth());
        if (!properties.contains("border-radius")) setBorderRadius(theme.getBorderRadius());
        if (!properties.contains("border-color")) setBorderColor(theme.getBorderColor());
        if (!properties.contains("color")) setColor(theme.getText());
        if (!properties.contains("margin")) setMargin(theme.getMargin());
        if (!properties.contains("font-size")) setFontSize(theme.getTextSize());
        return this;
    }

    public Style reverseColors() {
        Style style = new Style().applyFrom(this);
        style.setColor(reverseColor(getColor()));
        style.setBackgroundColor(reverseColor(getBackgroundColor()));
        style.setBorderColor(reverseColor(getBorderColor()));
        return style;
    }

    public int getBorderColor() {
        return borderColor;
    }

    public void setBorderColor(int borderColor) {
        this.borderColor = borderColor;
    }

    public float getBorderWidth() {
        return borderWidth;
    }

    public void setBorderWidth(float borderWidth) {
        this.borderWidth = borderWidth;
    }

    public float getBorderRadius() {
        return borderRadius;
    }

    public void setBorderRadius(float borderRadius) {
        this.borderRadius = borderRadius;
    }

    public String getBackground() {
        return background;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public PointF getMargin() {
        return margin;
    }

    public void setMargin(PointF margin) {
        this.margin = margin;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public float getFontSize() {
        return fontSize;
    }

    public void setFontSize(float fontSize) {
        this.fontSize = fontSize;
    }

    @Override public String toString() {
        return "Style{" +
                "background='" + background + '\'' +
                ", borderWidth=" + borderWidth +
                ", borderRadius=" + borderRadius +
                ", borderColor=" + Integer.toHexString(borderColor) +
                ", backgroundColor=" + Integer.toHexString(backgroundColor) +
                ", color=" + Integer.toHexString(color) +
                '}';
    }
}
