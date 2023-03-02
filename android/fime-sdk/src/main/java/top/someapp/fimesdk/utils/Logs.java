package top.someapp.fimesdk.utils;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import timber.log.Timber;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author zwz
 * Create on 2023-03-02
 */
@Keep
public class Logs {

    /**
     * Priority constant for the println method; use Log.v.
     */
    public static final int VERBOSE = 2;

    /**
     * Priority constant for the println method; use Log.d.
     */
    public static final int DEBUG = 3;

    /**
     * Priority constant for the println method; use Log.i.
     */
    public static final int INFO = 4;

    /**
     * Priority constant for the println method; use Log.w.
     */
    public static final int WARN = 5;

    /**
     * Priority constant for the println method; use Log.e.
     */
    public static final int ERROR = 6;

    /**
     * Priority constant for the println method.
     */
    public static final int ASSERT = 7;

    private static boolean setup_;
    private static int level = ERROR;

    private Logs() {
        // no instance.
    }

    public static void setup(boolean debug, File logFile) {
        if (setup_) return;
        if (debug) {
            level = DEBUG;
            if (logFile == null) {
                Timber.plant(new TimberTree());
            }
            else {
                Timber.plant(new TimberTree(), new LogToFileTree(logFile));
            }
        }
        else {
            if (logFile == null) {
                Timber.plant(new TimberTree());
            }
            else {
                Timber.plant(new LogToFileTree(logFile));
            }
        }
        setup_ = true;
    }

    public static void i(String message, Object... args) {
        if (needRecord(INFO)) Timber.i(message, args);
    }

    public static void d(String message, Object... args) {
        if (needRecord(DEBUG)) Timber.d(message, args);
    }

    public static void w(String message, Object... args) {
        if (needRecord(WARN)) Timber.w(message, args);
    }

    public static void e(String message, Object... args) {
        if (needRecord(ERROR)) Timber.e(message, args);
    }

    private static boolean needRecord(int lv) {
        return lv <= level;
    }

    private static class TimberTree extends Timber.DebugTree {

        @Nullable @Override
        protected String createStackElementTag(@NonNull StackTraceElement element) {
            StackTraceElement[] stackTrace = Thread.currentThread()
                                                   .getStackTrace();
            boolean hit = false;
            for (StackTraceElement el : stackTrace) {
                if (el.equals(element)) {
                    hit = true;
                    continue;
                }
                if (hit) return super.createStackElementTag(el);
            }
            return super.createStackElementTag(element);
        }
    }

    private static class LogToFileTree extends Timber.DebugTree {

        private FileWriter writer;

        private LogToFileTree(File logFile) {
            try {
                if (logFile.exists()) {
                    writer = new FileWriter(logFile);
                }
                else {
                    if (logFile.createNewFile()) {
                        writer = new FileWriter(logFile);
                    }
                }
                log(INFO, "init LogToFileTree, logFile=%s", logFile.getAbsoluteFile());
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void log(int level, @Nullable String tag, @NonNull String message,
                @Nullable Throwable e) {
            if (writer == null) {
                super.log(level, tag, message, e);
            }
            else {
                try {
                    switch (level) {
                        case VERBOSE:
                            writer.append("[VERBOSE] ");
                            break;
                        case DEBUG:
                            writer.append("[DEBUG] ");
                            break;
                        case INFO:
                            writer.append("[INFO] ");
                            break;
                        case WARN:
                            writer.append("[WARN] ");
                            break;
                        case ERROR:
                            writer.append("[ERROR ]");
                            break;
                        case ASSERT:
                            writer.append("[ASSERT] ");
                            break;
                    }
                    writer.append(message);
                    if (e != null) {
                        writer.append("\t");
                        writer.append(e.getMessage());
                    }
                    writer.append("\n");
                    writer.flush();
                }
                catch (IOException err) {
                    err.printStackTrace();
                }
            }
        }
    }
}
