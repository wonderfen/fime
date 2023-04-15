/*
 * Copyright (c) 2023 Fime project https://fime.fit
 * Initial author: zelde126@126.com
 */

package top.someapp.fimesdk.view;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import androidx.annotation.NonNull;
import top.someapp.fimesdk.utils.Geometry;

/**
 * @author zwz
 * Created on 2023-02-07
 */
public class Box {

    private float[] margin = { 0, 0, 0, 0 };
    private float[] padding = { 0, 0, 0, 0 };
    private float width;
    private float height;
    private PointF position = new PointF();

    public Box(float width, float height) {
        setWidth(width);
        setHeight(height);
    }

    public Box(PointF position, float width, float height) {
        this.position = position;
        setWidth(width);
        setHeight(height);
    }

    public float[] getMargin() {
        return margin;
    }

    public void setMargin(float[] margin) {
        this.margin = margin;
    }

    public float[] getPadding() {
        return padding;
    }

    public void setPadding(float[] padding) {
        this.padding = padding;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = Math.max(width, 0.5f); // ensure width > 0
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = Math.max(height, 0.5f);  // ensure height > 0
    }

    public PointF getPosition() {
        return position;
    }

    public void setPosition(PointF position) {
        this.position = position;
    }

    public void render(Canvas canvas, Paint paint, Style style) {
        paint.setColor(style.getBackgroundColor());
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        PointF margin = style.getMargin();
        RectF content = toRectF();
        content.left += Geometry.dp2px(margin.x / 2);
        content.top += Geometry.dp2px(margin.y / 2);
        content.right -= Geometry.dp2px(margin.x / 2);
        content.bottom -= Geometry.dp2px(margin.y / 2);
        final int borderRadius = Geometry.dp2px(style.getBorderRadius());
        canvas.drawRoundRect(content, borderRadius, borderRadius, paint);
        if (style.getBorderWidth() > 0) {
            final int strokeWidth = Geometry.dp2px(style.getBorderWidth());
            paint.setStrokeWidth(strokeWidth);
            paint.setColor(strokeWidth);
            paint.setStyle(Paint.Style.STROKE);
            content.left -= strokeWidth;
            content.top -= strokeWidth;
            content.right += strokeWidth;
            content.bottom += strokeWidth;
            canvas.drawRoundRect(content, borderRadius + strokeWidth, borderRadius + strokeWidth,
                                 paint);
        }
    }

    public void render(Canvas canvas, Paint paint, Theme theme) {
        paint.setColor(theme.getKeyBackground());
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        PointF margin = theme.getMargin();
        RectF content = toRectF();
        content.left += Geometry.dp2px(margin.x / 2);
        content.top += Geometry.dp2px(margin.y / 2);
        content.right -= Geometry.dp2px(margin.x / 2);
        content.bottom -= Geometry.dp2px(margin.y / 2);
        final int borderRadius = Geometry.dp2px(theme.getBorderRadius());
        canvas.drawRoundRect(content, borderRadius, borderRadius, paint);
        if (theme.getBorderWidth() > 0) {
            final int strokeWidth = Geometry.dp2px(theme.getBorderWidth());
            paint.setStrokeWidth(strokeWidth);
            paint.setColor(theme.getBorderColor());
            paint.setStyle(Paint.Style.STROKE);
            content.left -= strokeWidth;
            content.top -= strokeWidth;
            content.right += strokeWidth;
            content.bottom += strokeWidth;
            canvas.drawRoundRect(content, borderRadius + strokeWidth, borderRadius + strokeWidth,
                                 paint);
        }
    }

    public float getLeft() {
        return position.x;
    }

    public float getTop() {
        return position.y;
    }

    public float getRight() {
        return position.x + width;
    }

    public float getBottom() {
        return position.y + height;
    }

    public PointF getCenter() {
        return Geometry.centerOf(position, new PointF(getRight(), getBottom()));
    }

    public boolean in(@NonNull Box container) {
        return getLeft() >= container.getLeft() &&
                getTop() >= container.getTop() &&
                getWidth() <= container.getWidth() &&
                getHeight() <= container.getHeight() &&
                getRight() <= container.getRight() &&
                getBottom() <= container.getBottom();
    }

    public boolean surround(@NonNull Box child) {
        return child.in(this);
    }

    public boolean surround(@NonNull PointF pointF) {
        RectF rectF = toRectF();
        return pointF.x >= rectF.left && pointF.x <= rectF.right &&
                pointF.y >= rectF.top && pointF.y <= rectF.bottom;
    }

    public Rect toRect() {
        return new Rect((int) getLeft(), (int) getTop(), (int) getRight(), (int) getBottom());
    }

    public RectF toRectF() {
        return new RectF(getLeft(), getTop(), getRight(), getBottom());
    }

    @Override public String toString() {
        return "Box{" +
                "width=" + width +
                ", height=" + height +
                ", position=" + position +
                '}';
    }
}

