package top.someapp.fimesdk.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * @author zwz
 * Created on 2023-01-04
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
public class FileStorage {

    private FileStorage() {
        // no instance
    }

    public static boolean hasFile(@NonNull File dir, @NonNull String name) {
        if (!hasDir(dir)) return false;
        File test = new File(dir, name);
        return test.exists() && test.isFile();
    }

    public static boolean hasFile(@NonNull File file) {
        return file.exists() && file.isFile();
    }

    public static boolean hasDir(@NonNull File dir) {
        return dir.exists() && dir.isDirectory();
    }

    public static File mkdir(@NonNull File parent, String name) {
        File dir = new File(parent, name);
        dir.mkdirs();
        return dir;
    }

    public static boolean writeTextFile(@NonNull File file, @NonNull String text) {
        try (FileOutputStream out = new FileOutputStream(file)) {
            out.write(text.getBytes(StandardCharsets.UTF_8));
            out.flush();
        }
        catch (IOException ignored) {
            return false;
        }
        return true;
    }

    public static void copyIfNotExists(@NonNull InputStream ins, @NonNull File target) {
        copyToFile(ins, target, false);
    }

    public static void copyToFile(@NonNull InputStream ins, @NonNull File target) {
        copyToFile(ins, target, true);
    }

    public static void copyToFile(@NonNull InputStream ins, @NonNull File target,
            boolean overwrite) {
        if (hasFile(target) && !overwrite) return;

        try (FileOutputStream out = new FileOutputStream(target)) {
            byte[] buffer = new byte[1024];
            int n;
            while ((n = ins.read(buffer)) > 0) {
                out.write(buffer, 0, n);
                out.flush();
            }
        }
        catch (IOException e) {
            Logs.e(e.toString());
        }
    }

    public static boolean deleteFile(@NonNull File file) {
        if (file.exists() && file.isFile()) return file.delete();
        return false;
    }

    public static void cleanDir(@NonNull File dir) {
        if (hasDir(dir)) {
            dir.listFiles(f -> {
                if (f.isDirectory()) {
                    cleanDir(f);
                }
                else {
                    f.delete();
                }
                return false;
            });
        }
    }

    public static void readFromUri(Uri uri) {

    }

    public static void writeToUri(Uri uri) {

    }

    /**
     * 通过uri拷贝外部存储的文件到自己应用的沙盒目录
     *
     * @param uri 外部存储文件的uri
     * @param destFile 沙盒文件路径
     */
    public static void readUriToFile(Context context, Uri uri, File destFile) {
        try (InputStream ins = context.getContentResolver()
                                      .openInputStream(uri)) {
            copyToFile(ins, destFile, true);
        }
        catch (Exception e) {
            Logs.e(" copy file uri to inner storage e = " + e.toString());
        }
    }

    /**
     * 拷贝沙盒中的文件到外部存储区域
     *
     * @param filePath 沙盒文件路径
     * @param externalUri 外部存储文件的 uri
     */
    public static boolean copySandFileToExternalUri(Context context, String filePath,
            Uri externalUri) {
        ContentResolver contentResolver = context.getContentResolver();
        InputStream inputStream = null;
        OutputStream outputStream = null;
        boolean ret = false;
        try {
            outputStream = contentResolver.openOutputStream(externalUri);
            File sandFile = new File(filePath);
            if (sandFile.exists()) {
                inputStream = new FileInputStream(sandFile);
                int readCount;
                byte[] buffer = new byte[1024];
                while ((readCount = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, readCount);
                    outputStream.flush();
                }
            }
            ret = true;
        }
        catch (Exception e) {
            Logs.e("copy SandFile To ExternalUri. e = " + e.toString());
            ret = false;
        }
        finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
                Logs.d(" input stream and output stream close successful.");
            }
            catch (Exception e) {
                e.printStackTrace();
                Logs.e(" input stream and output stream close fail. e = " + e.toString());
            }
        }
        return ret;
    }

    public static String hash(File file) {
        if (!hasFile(file)) return null;
        return null;
    }

    public void requestReadExternalStorage(Context context) {

    }
}
