package top.someapp.fimesdk.utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.util.Base64;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author zwz
 * Create on 2023-03-08
 */
public class Clipboard {

    private static final int maxRecords = 100;
    private static final String fileName = "clipboard.log";
    private static final List<Long> posList = new ArrayList<>();
    private static boolean start;
    private static File dir;
    private static RandomAccessFile writer;
    private static long pos;

    private Clipboard() {
        //no instance
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void listener(Context context, File clipboardDataDir) {
        if (start) return;
        dir = clipboardDataDir;
        try {
            if (!dir.exists()) dir.mkdirs();
            File history = new File(clipboardDataDir, fileName);
            if (!history.exists()) history.createNewFile();
            writer = new RandomAccessFile(history, "rwd");
            start = true;
            getClipItems();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(
                Context.CLIPBOARD_SERVICE);
        clipboardManager.addPrimaryClipChangedListener(
                () -> {
                    ClipData primaryClip = clipboardManager.getPrimaryClip();
                    if (primaryClip.getItemCount() > 0) {
                        ClipData.Item item = primaryClip.getItemAt(0);
                        String text = item.coerceToText(context)
                                          .toString();
                        append(text);
                    }
                });
    }

    @SuppressWarnings("unchecked")
    public static List<String> getClipItems() {
        if (!FileStorage.hasFile(dir, fileName)) return Collections.EMPTY_LIST;

        List<String> content = new ArrayList<>();
        String line;
        if (posList.isEmpty()) {
            try {
                RandomAccessFile reader = new RandomAccessFile(new File(dir, fileName), "r");
                Logs.d("init clip data.");
                reader.seek(0);
                final long len = reader.length();
                while (true) {
                    line = reader.readLine();
                    line = new String(Base64.decode(line, Base64.NO_WRAP));
                    if (line.length() > 0) content.add(line);
                    if (reader.getFilePointer() >= len) break;
                    posList.add(0, reader.getFilePointer());
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        else {
            try {
                RandomAccessFile reader = new RandomAccessFile(new File(dir, fileName), "r");
                Logs.d("read clip data.");
                for (long pos : posList) {
                    reader.seek(pos);
                    line = new String(Base64.decode(reader.readLine(), Base64.NO_WRAP));
                    if (line.length() > 0) content.add(line);
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        return content;
    }

    public static void clean() {
        if (writer == null) return;
        Logs.d("clean clip data.");
        try {
            writer.seek(0);
            writer.setLength(0);
            pos = 0;
            posList.clear();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void append(String text) {
        if (text.length() > 0) {
            Logs.d("write clip data.");
            if (posList.size() > maxRecords) clean();
            try {
                pos = writer.length();
                writer.seek(pos);
                byte[] bytes = Base64.encode(text.getBytes(StandardCharsets.UTF_8),
                                             Base64.NO_WRAP);
                writer.write(bytes);
                writer.writeUTF("\n");
                posList.add(0, pos);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
