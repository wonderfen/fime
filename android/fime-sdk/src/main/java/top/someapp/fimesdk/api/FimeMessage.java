package top.someapp.fimesdk.api;

import android.os.Message;

/**
 * @author zwz
 * Created on 2022-12-30
 */
public class FimeMessage {

    public static final int MSG_REPAINT = 0x01;
    public static final int MSG_CANDIDATE_CHANGE = 0x01 << 1;
    public static final int MSG_CHECK_LONG_PRESS = 0x01 << 2;
    public static final int MSG_SCHEMA_ACTIVE = 0xff01;

    FimeMessage() {
    }

    public static Message createRepaintMessage() {
        return create(MSG_REPAINT, null);
    }

    public static Message create(int what, Object obj) {
        Message message = new Message();
        message.what = what;
        message.obj = obj;
        return message;
    }
}
