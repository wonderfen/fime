/*
 * Copyright (c) 2023 Fime project https://fime.fit
 * Initial author: zelde126@126.com
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Properties;
import java.util.TimeZone;

/**
 * @author zwz
 * Created on 2023-04-24
 */
class Changelog {

    @SuppressWarnings("SpellCheckingInspection")
    public static void main(String[] args) throws IOException {
        Properties versionProp = new Properties();
        // work dir is fime
        versionProp.load(new FileInputStream("android/version.properties"));
        int major = Integer.decode(versionProp.getProperty("versionMajor"));
        int minor = Integer.decode(versionProp.getProperty("versionMinor"));
        int patch = Integer.decode(versionProp.getProperty("versionPatch"));
        FileOutputStream out = new FileOutputStream("android/changelog/CHANGELOG.md");
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+08:00"));
        String version = String.format("# %d.%d.%d ", major, minor, patch);
        String date = String.format("(%04d-%02d-%02d)\n", calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH) + 1,
                                    calendar.get(Calendar.DATE));
        out.write(version.getBytes(StandardCharsets.UTF_8));
        out.write(date.getBytes(StandardCharsets.UTF_8));
        out.flush();

        String os = System.getProperty("os.name")
                          .toLowerCase();
        String command = os.contains(
                "windows") ? "android/changelog/gitlog.bat" : "android/changelog/gitlog.sh";
        Runtime.getRuntime()
               .exec(command);
        InputStream ins = new FileInputStream("android/changelog/log.txt");
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(ins, StandardCharsets.UTF_8));
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.matches("^- (fix|pref|feat|refactor)[^a-z].+")) {
                out.write((line + "\n").getBytes(StandardCharsets.UTF_8));
            }
        }
        out.flush();
        out.close();
        ins.close();

        System.out.println("Done!");
    }
}
