package top.someapp.fimesdk.view;

import android.graphics.PointF;
import androidx.annotation.NonNull;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;

import java.util.List;

/**
 * 主题
 *
 * @author zwz
 * Created on 2023-03-08
 */
public class Theme implements Cloneable {

    private static final int kColorMask = 0xff000000;
    private String name = "light";   // prevent NPE!
    private int background = 0xffd5d7dd;
    private int text = 0xff161616;
    private int secondaryText = 0xffb5b5b5;
    private int keyBackground = 0xfffafafa;
    private int borderColor = 0xfffafafa;
    private int fnBackground = 0xffb5bdc6;
    private int inputCode = 0xff161616;
    private int activeBackground = 0xfffafafa;
    private int activeText = 0xff50a96c;

    private float textSize = 14;
    private float keyLabelSize = 16.5f;
    private int borderWidth = 0;
    private int borderRadius = 6;

    private PointF margin = new PointF();

    public Theme() {
    }

    public Theme(@NonNull Config config, @NonNull String name) {
        this.name = name;
        parse(config);
    }

    private static int reverseColor(int color) {
        return (~color) | kColorMask;
    }

    public Theme reverseColors() {
        Theme reverse = new Theme();
        reverse.setBackground(reverseColor(getBackground()));
        reverse.setBorderColor(reverseColor(getBorderColor()));
        reverse.setText(reverseColor(getText()));
        reverse.setSecondaryText(reverseColor(getSecondaryText()));
        reverse.setKeyBackground(reverseColor(getKeyBackground()));
        reverse.setFnBackground(reverseColor(getFnBackground()));
        reverse.setInputCode(reverseColor(getInputCode()));
        reverse.setActiveBackground(reverseColor(getActiveBackground()));
        reverse.setActiveText(reverseColor(getActiveText()));
        return reverse;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getBackground() {
        return background;
    }

    public void setBackground(int background) {
        this.background = kColorMask | background;
    }

    public int getText() {
        return text;
    }

    public void setText(int text) {
        this.text = kColorMask | text;
    }

    public int getSecondaryText() {
        return secondaryText;
    }

    public void setSecondaryText(int secondaryText) {
        this.secondaryText = kColorMask | secondaryText;
    }

    public int getKeyBackground() {
        return keyBackground;
    }

    public void setKeyBackground(int keyBackground) {
        this.keyBackground = kColorMask | keyBackground;
    }

    public int getBorderColor() {
        return borderColor;
    }

    public void setBorderColor(int borderColor) {
        this.borderColor = kColorMask | borderColor;
    }

    public int getFnBackground() {
        return fnBackground;
    }

    public void setFnBackground(int fnBackground) {
        this.fnBackground = kColorMask | fnBackground;
    }

    public int getInputCode() {
        return inputCode;
    }

    public void setInputCode(int inputCode) {
        this.inputCode = kColorMask | inputCode;
    }

    public int getActiveBackground() {
        return activeBackground;
    }

    public void setActiveBackground(int activeBackground) {
        this.activeBackground = kColorMask | activeBackground;
    }

    public int getActiveText() {
        return activeText;
    }

    public void setActiveText(int activeText) {
        this.activeText = kColorMask | activeText;
    }

    public float getTextSize() {
        return textSize;
    }

    public void setTextSize(float textSize) {
        this.textSize = textSize;
    }

    public float getKeyLabelSize() {
        return keyLabelSize;
    }

    public void setKeyLabelSize(float keyLabelSize) {
        this.keyLabelSize = keyLabelSize;
    }

    public int getBorderWidth() {
        return borderWidth;
    }

    public void setBorderWidth(int borderWidth) {
        this.borderWidth = borderWidth;
    }

    public int getBorderRadius() {
        return borderRadius;
    }

    public void setBorderRadius(int borderRadius) {
        this.borderRadius = borderRadius;
    }

    public PointF getMargin() {
        return margin;
    }

    public void setMargin(PointF margin) {
        this.margin = margin;
    }

    public Theme copy() {
        Theme backup = null;
        try {
            backup = (Theme) clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return backup;
    }

    private void parse(Config config) {
        if (!config.hasPath(name)) throw new ConfigException.Missing("theme." + name);
        Config c = config.getConfig(name);
        setBackground(Integer.decode(c.getString("background")));
        setText(Integer.decode(c.getString("text")));
        setSecondaryText(Integer.decode(c.getString("secondary-text")));
        setKeyBackground(Integer.decode(c.getString("key-background")));
        setBorderColor(Integer.decode(c.getString("border-color")));
        setFnBackground(Integer.decode(c.getString("fn-background")));
        setInputCode(Integer.decode(c.getString("input-code")));
        setActiveBackground(Integer.decode(c.getString("active-background")));
        setActiveText(Integer.decode(c.getString("active-text")));

        textSize = c.getNumber("text-size")
                    .floatValue();
        keyLabelSize = c.getNumber("key-label-size")
                        .floatValue();
        borderWidth = c.getInt("border-width");
        borderRadius = c.getInt("border-radius");
        if (c.hasPath("margin")) {
            List<Integer> margin = c.getIntList("margin");
            this.margin = new PointF(margin.get(0), margin.get(1));
        }
    }
}
