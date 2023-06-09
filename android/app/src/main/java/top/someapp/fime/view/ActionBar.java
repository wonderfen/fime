/*
 * Copyright (c) 2023 Fime project https://fime.fit
 * Initial author: zelde126@126.com
 */

package top.someapp.fime.view;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import top.someapp.fime.R;
import top.someapp.fimesdk.FimeContext;
import top.someapp.fimesdk.api.Candidate;
import top.someapp.fimesdk.api.FimeHandler;
import top.someapp.fimesdk.api.FimeMessage;
import top.someapp.fimesdk.api.InputEditor;
import top.someapp.fimesdk.utils.Fonts;
import top.someapp.fimesdk.utils.Geometry;
import top.someapp.fimesdk.utils.Logs;
import top.someapp.fimesdk.view.Box;
import top.someapp.fimesdk.view.Theme;
import top.someapp.fimesdk.view.Widget;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zwz
 * Created on 2022-12-29
 */
class ActionBar implements Widget {

    private final List<Float> candidatePos = new ArrayList<>();
    private final Paint paint = new Paint();
    private final Bitmap icon;
    private final float gutter;               // 候选之间的间隔
    private int backgroundColor = 0xffd5d7dd;
    private int textColor = 0xff161616;
    private int activeBackgroundColor = 0xfffafafa;
    private int activeTextColor = 0xff50a96c;
    private Box container;
    private FimeHandler painter;
    private float candidateOffset;      // 滑动产生的偏移量
    private PointF moveStartAt;
    private InputEditor inputEditor;
    private FimePopup window;

    ActionBar(Box container) {
        this.container = container;
        paint.setAntiAlias(true);
        icon = Geometry.drawableToBitmap(FimeContext.getInstance()
                                                    .getContext()
                                                    .getDrawable(R.drawable.ic_fime),
                                         container.getHeight() * .625f,
                                         container.getHeight() * .625f);
        gutter = container.getWidth() / 16;
    }

    @Override public Box getContainer() {
        return container;
    }

    @Override public void applyTheme(Theme theme) {
        backgroundColor = theme.getBackground();
        textColor = theme.getText();
        activeBackgroundColor = theme.getActiveBackground();
        activeTextColor = theme.getActiveText();
    }

    @Override public void onDraw(Canvas canvas, Box box, FimeHandler painter) {
        Logs.d("onDraw, candidateOffset=" + candidateOffset);
        container = box;
        this.painter = painter;
        paint.setColor(backgroundColor);
        canvas.drawRect(box.toRectF(), paint);
        candidatePos.clear();
        if (inputEditor == null || !inputEditor.hasInput()) {
            Logs.d("no input code!");
            canvas.drawBitmap(icon, 0.5f * gutter, 0.5f * (box.getHeight() - icon.getHeight()),
                              paint);
            candidateOffset = 0;
            return;
        }

        paint.setColor(textColor);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setTextSize(0.25f * box.getHeight());
        canvas.drawText(inputEditor.getPrompt(), box.getLeft() + 16, paint.getTextSize(),
                        paint);
        if (inputEditor.hasCandidate()) {
            Logs.d("draw candidate.");
            paint.setTextSize(0.33f * box.getHeight());
            float x = box.getLeft() + candidateOffset + 10.0f;
            float y = 0.8f * box.getHeight();
            List<Candidate> candidateList = inputEditor.getCandidateList();
            int activeIndex = inputEditor.getActiveIndex();
            for (int i = Math.max(activeIndex, 0), len = candidateList.size(); i < len; i++) {
                String text = candidateList.get(i).text;
                Box textBox = Fonts.getTextBounds(paint, text);
                float width = Math.max(textBox.getWidth(), paint.getTextSize());
                if (i == activeIndex) {
                    inputEditor.setActiveIndex(i);
                    paint.setColor(activeBackgroundColor);
                    RectF rect = new RectF(x, 0.36f * box.getHeight(), x + width + gutter / 3,
                                           0.95f * box.getHeight());
                    canvas.drawRoundRect(rect, 12, 12, paint);
                    paint.setColor(activeTextColor);
                    canvas.drawText(text, (x + rect.right - width) / 2, y, paint);
                    paint.setColor(Color.BLACK);
                }
                else {
                    paint.setColor(textColor);
                    canvas.drawText(text, x, y, paint);
                }
                candidatePos.add(x + width + gutter / 2);
                x += (width + gutter);
                if (x >= box.getWidth()) {
                    break;
                }
            }
            // candidatePos.add(x);
        }
        else {
            Logs.d("no candidate!");
            candidateOffset = 0;
        }
    }

    @Override public void onTouchStart(PointF pos) {
        Logs.d("onTouchStart");
        moveStartAt = new PointF(pos.x, pos.y);
    }

    @Override public void onTouchMove(PointF pos) {
        if (inputEditor == null || !inputEditor.hasCandidate() || candidatePos.isEmpty() ||
                moveStartAt == null) {
            return;
        }
        float dx = pos.x - moveStartAt.x;
        Logs.d("onTouchMove, dx=%f", dx);
        if (Math.abs(dx) < 10) return; // 这个叫消抖？
        Logs.d("candidatePos.size=%d", candidatePos.size());
        if (candidatePos.isEmpty()) return;  // 为什么还要检查一次呢，不理解!!

        int activeIndex = inputEditor.getActiveIndex();
        float first = candidatePos.get(0);
        float last = candidatePos.get(candidatePos.size() - 1);
        if (dx > 0) { // 手指从左往右滑, activeIndex--
            if (activeIndex == 0) {
                candidateOffset = 0;
            }
            else {
                int move = 0;
                if (candidateOffset + dx >= 0) {
                    move++;
                    candidateOffset += dx;
                    candidateOffset -= first;
                }
                else {
                    candidateOffset += dx;
                }
                activeIndex -= move;
                if (activeIndex >= 0) inputEditor.setActiveIndex(activeIndex);
            }
        }
        else { // 手指从右往左滑, activeIndex++
            if (last + gutter < container.getWidth()) {
                candidateOffset = 0;
            }
            else {
                int move = 0;
                if (candidateOffset + dx <= 0) {
                    move++;
                    candidateOffset += dx;
                    candidateOffset += first;
                }
                else {
                    candidateOffset += dx;
                }
                if (move > 0) inputEditor.setActiveIndex(activeIndex + move);
            }
        }
        moveStartAt = new PointF(pos.x, pos.y);
        requestRepaint();   // TODO: 2023/2/26 可以再优化，减少重绘的次数
    }

    @Override public void onTouchEnd(PointF pos) {
        moveStartAt = null;
        if (inputEditor != null && inputEditor.hasInput() && inputEditor.hasCandidate() &&
                pos.x < candidatePos.get(candidatePos.size() - 1)) {
            int min = 0;
            int max = candidatePos.size() - 1;
            while (min < max) {
                int mid = (min + max) / 2;
                Float x = candidatePos.get(mid);
                if (pos.x > x) {
                    min = mid + 1;
                }
                else if (pos.x < x) {
                    max = mid;
                }
                else {
                    max = mid - 1;
                }
            }
            inputEditor.select(inputEditor.getActiveIndex() + (min + max) / 2);
            candidateOffset = 0;
            requestRepaint();
            return;
        }
        if (inputEditor != null && !inputEditor.hasInput() && pos.x <= 1.5f * icon.getWidth()) {
            if (window == null) window = new FimePopup();
            window.show();
        }
    }

    @Override public void onLongPress(PointF pos, long durations) {
        Logs.d("onLongPress");
    }

    @Override public void setOnVirtualKeyListener(OnVirtualKeyListener virtualKeyListener) {
    }

    void setInputEditor(InputEditor inputEditor) {
        Logs.d("setInputEditor: 0x%x.", inputEditor.hashCode());
        this.inputEditor = inputEditor;
    }

    private void requestRepaint() {
        if (painter != null) painter.send(FimeMessage.create(FimeMessage.MSG_REPAINT));
    }
}
