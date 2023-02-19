package top.someapp.fimesdk.config;

import android.view.KeyEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * 按键值，大部分参考：java.awt.event.KeyEvent
 *
 * @author zwz
 * Created on 2022-12-23
 * 特殊字符参考：
 * @see
 * <a href="https://www.qqxiuzi.cn/zh/unicode-zifu.php?plane=0&ks=2000&js=2FFF">世界文字大全，Unicode 字符集</a>
 */
public class Keycode {

    // 功能键部分
    public static final int VK_FN_ESC = 0XFF01;
    public static final int VK_FN_TAB = 0XFF02;
    public static final int VK_FN_SHIFT = 0XFF03;
    public static final int VK_FN_CTRL = 0XFF04;
    public static final int VK_FN_ALT = 0XFF05;
    public static final int VK_FN_BACKSPACE = 0XFF06;
    public static final int VK_FN_DELETE = 0XFF07;
    public static final int VK_FN_HOME = 0XFF08;
    public static final int VK_FN_END = 0XFF09;
    public static final int VK_FN_PAGE_UP = 0XFF0A;
    public static final int VK_FN_PAGE_DOWN = 0XFF0B;
    public static final int VK_FN_ENTER = 0XFF0C;

    /**
     * 清除已输入的内容
     */
    public static final int VK_FN_CLEAR = 0XFF0D;

    /**
     * Constant for the CAPS_LOCK virtual key.
     */
    public static final int VK_FN_CAPS_LOCK = 0xFF0E;

    /**
     * Constant for the non-numpad <b>left</b> arrow key.
     */
    public static final int VK_FN_LEFT = 0xFF0F;

    /**
     * Constant for the non-numpad <b>up</b> arrow key.
     */
    public static final int VK_FN_UP = 0xFF10;

    /**
     * Constant for the non-numpad <b>right</b> arrow key.
     */
    public static final int VK_FN_RIGHT = 0xFF11;

    /**
     * Constant for the non-numpad <b>down</b> arrow key.
     */
    public static final int VK_FN_DOWN = 0xFF12;

    /**
     * 切换到字母输入模式
     */
    public static final int VK_FN_ABC = 0XFF13;
    /**
     * 切换到数字输入模式
     */
    public static final int VK_FN_123 = 0XFF14;
    /**
     * 切换到符号输入模式
     */
    public static final int VK_FN_SYMBOL = 0XFF15;
    /**
     * 返回
     */
    public static final int VK_FN_RETURN = 0XFF16;
    public static final int VK_GRAVE = '`';
    public static final int VK_EXCLAM = '!';
    public static final int VK_AT = '@';
    public static final int VK_POUND = '#';
    public static final int VK_DOLLAR = '$';
    public static final int VK_PERCENT = '%';
    public static final int VK_CARET = '^';
    public static final int VK_AMPERSAND = '&';
    public static final int VK_STAR = '*';
    public static final int VK_PAREN_LEFT = '(';
    public static final int VK_PAREN_RIGHT = ')';
    public static final int VK_MINUS = '-';
    public static final int VK_EQUALS = '=';
    public static final int VK_BRACKET_LEFT = '[';
    public static final int VK_BRACKET_RIGHT = ']';
    public static final int VK_BACKSLASH = '\\';
    public static final int VK_SEMICOLON = ';';
    public static final int VK_SINGLE_QUOTE = '\'';
    public static final int VK_COMMA = ',';
    public static final int VK_PERIOD = '.';
    public static final int VK_SLASH = '/';
    /**
     * Constant for the "0" key.
     */
    public static final int VK_0 = 0x30;

    // 数字键，不区分主键盘的数字和小键盘的数字了
    /**
     * Constant for the "1" key.
     */
    public static final int VK_1 = 0x31;
    /**
     * Constant for the "2" key.
     */
    public static final int VK_2 = 0x32;
    /**
     * Constant for the "3" key.
     */
    public static final int VK_3 = 0x33;
    /**
     * Constant for the "4" key.
     */
    public static final int VK_4 = 0x34;
    /**
     * Constant for the "5" key.
     */
    public static final int VK_5 = 0x35;
    /**
     * Constant for the "6" key.
     */
    public static final int VK_6 = 0x36;
    /**
     * Constant for the "7" key.
     */
    public static final int VK_7 = 0x37;
    /**
     * Constant for the "8" key.
     */
    public static final int VK_8 = 0x38;
    /**
     * Constant for the "9" key.
     */
    public static final int VK_9 = 0x39;
    public static final int VK_SPACE = 0x20;    // 32
    public static final int VK_A = 65;
    public static final int VK_B = 66;
    public static final int VK_C = 67;
    public static final int VK_D = 68;
    public static final int VK_E = 69;
    public static final int VK_F = 70;
    public static final int VK_G = 71;
    public static final int VK_H = 72;
    public static final int VK_I = 73;
    public static final int VK_J = 74;
    public static final int VK_K = 75;
    public static final int VK_L = 76;
    public static final int VK_M = 77;
    public static final int VK_N = 78;
    public static final int VK_O = 79;
    public static final int VK_P = 80;
    public static final int VK_Q = 81;
    public static final int VK_R = 82;
    public static final int VK_S = 83;
    public static final int VK_T = 84;
    public static final int VK_U = 85;
    public static final int VK_V = 86;
    public static final int VK_W = 87;
    public static final int VK_X = 88;
    public static final int VK_Y = 89;
    public static final int VK_Z = 90;
    public static final int VK_a = 97;
    public static final int VK_b = 98;
    public static final int VK_c = 99;
    public static final int VK_d = 100;
    public static final int VK_e = 101;
    public static final int VK_f = 102;
    public static final int VK_g = 103;
    public static final int VK_h = 104;
    public static final int VK_i = 105;
    public static final int VK_j = 106;
    public static final int VK_k = 107;
    public static final int VK_l = 108;
    public static final int VK_m = 109;
    public static final int VK_n = 110;
    public static final int VK_o = 111;
    public static final int VK_p = 112;
    public static final int VK_q = 113;
    public static final int VK_r = 114;
    public static final int VK_s = 115;
    public static final int VK_t = 116;
    public static final int VK_u = 117;
    public static final int VK_v = 118;
    public static final int VK_w = 119;
    public static final int VK_x = 120;
    public static final int VK_y = 121;
    public static final int VK_z = 122;
    // 符号按键部分
    private static final String kSymbols = "`!@#$%^&*()-[]\\;',./";
    private static final char TAB_CHAR = 0X21C6;
    private static final char ENTER_CHAR = 0X21b5;
    private static final char SHIFT_CHAR = 0X21EA;
    private static final char BACKSPACE_CHAR = 0X232B;

    private static final Map<String, Keycode> keycodeMap = new HashMap<>(128);
    private static final Map<Integer, String> nameMap = new HashMap<>(128);
    private static final Keycode any = new Keycode(0, "VK_ANY", "");

    public final int code;
    public final String name;
    public final String label;

    private Keycode(int code, String name, String label) {
        this.code = code;
        this.name = name;
        this.label = label;
    }

    public static Keycode getByName(String name) {
        setup();
        if (keycodeMap.containsKey(name)) {
            return keycodeMap.get(name);
        }
        if (keycodeMap.containsKey("VK_FN_" + name)) {
            return keycodeMap.get("VK_FN_" + name);
        }
        if (keycodeMap.containsKey("VK_" + name)) {
            return keycodeMap.get("VK_" + name);
        }
        return any;
    }

    public static Keycode getByCode(int code) {
        setup();
        return nameMap.containsKey(code) ? keycodeMap.get(nameMap.get(code)) : any;
    }

    public static Keycode convertNativeKey(int nativeKeyCode, KeyEvent keyEvent) {
        if (KeyEvent.isModifierKey(nativeKeyCode)) return null;
        if (nativeKeyCode >= KeyEvent.KEYCODE_A && nativeKeyCode <= KeyEvent.KEYCODE_Z) {
            if (keyEvent.isShiftPressed()) {
                return getByCode(nativeKeyCode - KeyEvent.KEYCODE_A + VK_A);
            }
            else {
                return getByCode(nativeKeyCode - KeyEvent.KEYCODE_A + VK_a);
            }
        }
        else if (nativeKeyCode >= KeyEvent.KEYCODE_0 && nativeKeyCode <= KeyEvent.KEYCODE_9) {
            return getByCode(nativeKeyCode - KeyEvent.KEYCODE_0 + VK_0);
        }
        if (nativeKeyCode == KeyEvent.KEYCODE_DEL) return getByCode(VK_FN_BACKSPACE);
        if (nativeKeyCode == KeyEvent.KEYCODE_SPACE) return getByCode(VK_SPACE);
        if (nativeKeyCode == KeyEvent.KEYCODE_ENTER) return getByCode(VK_FN_ENTER);
        return null;
    }

    public static boolean isRepeatable(int code) {
        return code == VK_SPACE || code == VK_FN_ENTER || code == VK_FN_BACKSPACE;
    }

    public static boolean isFnKeyCode(int code) {
        return code > 0xff00;
    }

    public static boolean isAnyKeyCode(int code) {
        return code == any.code;
    }

    public static boolean isLetterCode(int code) {
        return (code >= VK_A && code <= VK_Z) || (code >= VK_a && code <= VK_z);
    }

    public static boolean isLetterUpperCode(int code) {
        return (code >= VK_A && code <= VK_Z);
    }

    public static boolean isLetterLowerCode(int code) {
        return (code >= VK_a && code <= VK_z);
    }

    public static boolean isDecimalCode(int code) {
        return (code >= VK_0 && code <= VK_9);
    }

    public static boolean isEnterCode(int code) {
        return code == VK_FN_ENTER;
    }

    public static boolean isSpaceCode(int code) {
        return code == VK_SPACE;
    }

    private static void setup() {
        if (!keycodeMap.isEmpty()) return;
        keycodeMap.put("VK_FN_ESC", new Keycode(VK_FN_ESC, "VK_FN_ESC", "Esc"));
        nameMap.put(VK_FN_ESC, "VK_FN_ESC");

        keycodeMap.put("VK_FN_TAB",
                       new Keycode(VK_FN_TAB, "VK_FN_TAB", Character.toString(TAB_CHAR)));
        nameMap.put(VK_FN_TAB, "VK_FN_TAB");

        keycodeMap.put("VK_FN_SHIFT",
                       new Keycode(VK_FN_SHIFT, "VK_FN_SHIFT", Character.toString(SHIFT_CHAR)));
        nameMap.put(VK_FN_SHIFT, "VK_FN_SHIFT");

        keycodeMap.put("VK_FN_CTRL", new Keycode(VK_FN_CTRL, "VK_FN_CTRL", "Ctrl"));
        nameMap.put(VK_FN_CTRL, "VK_FN_CTRL");

        keycodeMap.put("VK_FN_ALT", new Keycode(VK_FN_ALT, "VK_FN_ALT", "Alt"));
        nameMap.put(VK_FN_ALT, "VK_FN_ALT");

        keycodeMap.put("VK_FN_BACKSPACE", new Keycode(VK_FN_BACKSPACE, "VK_FN_BACKSPACE",
                                                      Character.toString(BACKSPACE_CHAR)));
        nameMap.put(VK_FN_BACKSPACE, "VK_FN_BACKSPACE");

        keycodeMap.put("VK_FN_DELETE", new Keycode(VK_FN_DELETE, "VK_FN_DELETE", "Del"));
        nameMap.put(VK_FN_DELETE, "VK_FN_DELETE");

        keycodeMap.put("VK_FN_HOME", new Keycode(VK_FN_HOME, "VK_FN_HOME", "Home"));
        nameMap.put(VK_FN_HOME, "VK_FN_HOME");

        keycodeMap.put("VK_FN_END", new Keycode(VK_FN_END, "VK_FN_END", "End"));
        nameMap.put(VK_FN_END, "VK_FN_END");

        keycodeMap.put("VK_FN_PAGE_UP", new Keycode(VK_FN_PAGE_UP, "VK_FN_PAGE_UP", "PgUp"));
        nameMap.put(VK_FN_PAGE_UP, "VK_FN_PAGE_UP");

        keycodeMap.put("VK_FN_PAGE_DOWN", new Keycode(VK_FN_PAGE_DOWN, "VK_FN_PAGE_DOWN", "PgDn"));
        nameMap.put(VK_FN_PAGE_DOWN, "VK_FN_PAGE_DOWN");

        keycodeMap.put("VK_FN_ENTER", new Keycode(VK_FN_ENTER, "VK_FN_ENTER",
                                                  Character.toString(ENTER_CHAR)));
        nameMap.put(VK_FN_ENTER, "VK_FN_ENTER");

        keycodeMap.put("VK_FN_CLEAR", new Keycode(VK_FN_CLEAR, "VK_FN_CLEAR", "Cls"));
        nameMap.put(VK_FN_CLEAR, "VK_FN_CLEAR");

        keycodeMap.put("VK_FN_CAPS_LOCK", new Keycode(VK_FN_CAPS_LOCK, "VK_FN_CAPS_LOCK", "Caps"));
        nameMap.put(VK_FN_CAPS_LOCK, "VK_FN_CAPS_LOCK");

        keycodeMap.put("VK_FN_LEFT", new Keycode(VK_FN_LEFT, "VK_FN_LEFT", "←"));
        nameMap.put(VK_FN_LEFT, "VK_FN_LEFT");

        keycodeMap.put("VK_FN_UP", new Keycode(VK_FN_UP, "VK_FN_UP", "↑"));
        nameMap.put(VK_FN_UP, "VK_FN_UP");

        keycodeMap.put("VK_FN_RIGHT", new Keycode(VK_FN_RIGHT, "VK_FN_RIGHT", "→"));
        nameMap.put(VK_FN_RIGHT, "VK_FN_RIGHT");

        keycodeMap.put("VK_FN_DOWN", new Keycode(VK_FN_DOWN, "VK_FN_DOWN", "↓"));
        nameMap.put(VK_FN_DOWN, "VK_FN_DOWN");

        keycodeMap.put("VK_FN_ABC", new Keycode(VK_FN_ABC, "VK_FN_ABC", "Abc"));
        nameMap.put(VK_FN_ABC, "VK_FN_ABC");

        keycodeMap.put("VK_FN_123", new Keycode(VK_FN_123, "VK_FN_123", "123"));
        nameMap.put(VK_FN_123, "VK_FN_123");

        keycodeMap.put("VK_FN_SYMBOL", new Keycode(VK_FN_SYMBOL, "VK_FN_SYMBOL", "Sym"));
        nameMap.put(VK_FN_SYMBOL, "VK_FN_SYMBOL");

        keycodeMap.put("VK_FN_RETURN", new Keycode(VK_FN_RETURN, "VK_FN_RETURN", "返回"));
        nameMap.put(VK_FN_RETURN, "VK_FN_RETURN");

        int[] symbols = {
                VK_GRAVE, VK_EXCLAM, VK_AT,
                VK_POUND, VK_DOLLAR, VK_PERCENT,
                VK_CARET, VK_AMPERSAND, VK_STAR,
                VK_PAREN_LEFT, VK_PAREN_RIGHT, VK_MINUS,
                VK_EQUALS, VK_BRACKET_LEFT, VK_BRACKET_RIGHT,
                VK_BACKSLASH, VK_SEMICOLON, VK_SINGLE_QUOTE,
                VK_COMMA, VK_PERIOD, VK_SLASH,
        };
        String[] symbolNames = {
                "VK_GRAVE", "VK_EXCLAM", "VK_AT",
                "VK_POUND", "VK_DOLLAR", "VK_PERCENT",
                "VK_CARET", "VK_AMPERSAND", "VK_STAR",
                "VK_PAREN_LEFT", "VK_PAREN_RIGHT", "VK_MINUS",
                "VK_EQUALS", "VK_BRACKET_LEFT", "VK_BRACKET_RIGHT",
                "VK_BACKSLASH", "VK_SEMICOLON", "VK_SINGLE_QUOTE",
                "VK_COMMA", "VK_PERIOD", "VK_SLASH",
        };
        for (int i = 0; i < symbols.length; i++) {
            int code = symbols[i];
            String name = symbolNames[i];
            keycodeMap.put(name, new Keycode(code, name, Character.toString((char) code)));
            nameMap.put(code, name);
        }

        for (int i = VK_0; i <= VK_9; i++) {
            String name = "VK_" + (i - VK_0);
            String label = Character.toString((char) ('0' + i - VK_0));
            keycodeMap.put(name, new Keycode(i, name, label));
            nameMap.put(i, name);
        }

        keycodeMap.put("VK_SPACE", new Keycode(VK_SPACE, "VK_SPACE", "Space"));
        nameMap.put(VK_SPACE, "VK_SPACE");

        for (int i = VK_A; i <= VK_Z; i++) {
            String label = String.valueOf((char) i);
            String name = "VK_" + label;
            keycodeMap.put(name, new Keycode(i, name, label));
            nameMap.put(i, name);
        }

        for (int i = VK_a; i <= VK_z; i++) {
            String label = String.valueOf((char) i);
            String name = "VK_" + label;
            keycodeMap.put(name, new Keycode(i, name, label));
            nameMap.put(i, name);
        }
    }

    public KeyEvent toNativeKeyEvent(int action) {
        int nativeCode = 0;
        if (isFnKeyCode(code)) { // fn keys

        }
        else if (isSymbol()) {

        }
        else if (isDecimalCode(code)) {
            nativeCode = KeyEvent.KEYCODE_0 + code - VK_0;
        }
        else if (isLetterUpperCode(code)) {
            nativeCode = KeyEvent.KEYCODE_A + code - VK_A;
        }
        else if (isLetterLowerCode(code)) {
            nativeCode = KeyEvent.KEYCODE_A + code - VK_a;
        }
        return new KeyEvent(action, nativeCode);
    }

    public boolean isSymbol() {
        return kSymbols.contains(label);
    }

    @Override public String toString() {
        return "Keycode{"
                + name +
                ", code=" + code +
                ", label='" + label + '\'' +
                '}';
    }
}
