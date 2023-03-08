package top.someapp.fimesdk.utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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
    private static File file;
    private static RandomAccessFile storage;

    private Clipboard() {
        //no instance
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void listener(Context context, File saveTo) {
        if (storage != null) return;
        file = saveTo;
        try {
            if (!saveTo.exists()) saveTo.createNewFile();
            storage = new RandomAccessFile(saveTo, "rw");
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
                                          .toString()
                                          .trim();
                        if (text.length() > 0) {
                            try {
                                storage.seek(0);
                                storage.write(text.getBytes(StandardCharsets.UTF_8));
                                storage.write("\n".getBytes(StandardCharsets.UTF_8));
                            }
                            catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
    }

    @SuppressWarnings("unchecked")
    public static List<String> getClipItems() {
        if (storage == null) return Collections.EMPTY_LIST;
        List<String> content = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file));) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (content.size() >= maxRecords || line.trim()
                                                        .length() < 1) {
                    break;
                }
                content.add(line);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return content;
    }

    public static void clean() {
        if (storage == null) return;
        try {
            storage.seek(0);
            storage.setLength(storage.length());
            storage.writeUTF("\n");
            storage.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
