/*
 * Copyright (c) 2023 Fime project https://fime.fit
 * Initial author: zelde126@126.com
 */

package top.someapp.fimesdk.api;

import android.os.Message;

/**
 * @author zwz
 * Created on 2022-12-30
 */
@SuppressWarnings("SpellCheckingInspection")
public class FimeMessage {

    public static final int MULTIPLE_HANDLER = 0b1_0000_0000;   // 需要多个 Handler 处理，默认一个 Handler 处理
    public static final int MSG_REPAINT = 0x01;
    // public static final int MSG_REPAINT_ACTION_BAR = 0x02;
    // public static final int MSG_REPAINT_KEYBOARD = 0x03;
    public static final int MSG_CANDIDATE_CHANGE = 0x04;
    public static final int MSG_CHECK_LONG_PRESS = 0x05;
    public static final int MSG_SCHEMA_ACTIVE = MULTIPLE_HANDLER | 0x06;
    public static final int MSG_REQUEST_SEARCH = 0x07;
    public static final int MSG_INPUT_CHANGE = MULTIPLE_HANDLER | 0x08;
    public static final int MSG_APPLY_THEME = 0X09;

    FimeMessage() {
    }

    public static boolean hasMultipleHandlerFlag(int what) {
        return (what & MULTIPLE_HANDLER) == MULTIPLE_HANDLER;
    }

    public static Message create(int what) {
        Message message = Message.obtain();
        message.what = what;
        return message;
    }

    public static Message create(int what, Object obj) {
        Message message = new Message();
        message.what = what;
        message.obj = obj;
        return message;
    }
}
