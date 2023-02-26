package top.someapp.fimesdk.api;

import android.os.Message;

/**
 * @author zwz
 * Created on 2022-12-30
 */
public class FimeMessage {

    public static final int MSG_REPAINT = 0x01;
    // public static final int MSG_REPAINT_ACTION_BAR = 0x02;
    // public static final int MSG_REPAINT_KEYBOARD = 0x03;
    public static final int MSG_CANDIDATE_CHANGE = 0x04;
    public static final int MSG_CHECK_LONG_PRESS = 0x05;
    public static final int MSG_SCHEMA_ACTIVE = 0xff01;
    public static final int MSG_REQUEST_SEARCH = 0xff02;
    public static final int MSG_INPUT_CHANGE = 0xff03;

    FimeMessage() {
    }

    public static Message create(int what) {
        Message message = new Message();
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
