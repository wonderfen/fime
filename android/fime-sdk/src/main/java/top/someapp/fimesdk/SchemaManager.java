package top.someapp.fimesdk;

import android.util.Pair;
import com.typesafe.config.Config;
import top.someapp.fimesdk.config.Configs;
import top.someapp.fimesdk.utils.FileStorage;
import top.someapp.fimesdk.utils.Strings;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author zwz
 * Create on 2023-02-09
 */
public class SchemaManager {

    private SchemaManager() {
        // no instance.
    }

    public static List<SchemaInfo> scan() {
        FimeContext fimeContext = FimeContext.getInstance();
        File appHomeDir = fimeContext.getAppHomeDir();
        File buildTxt = fimeContext.fileInCacheDir("build.txt");
        Map<String, SchemaInfo> info = new LinkedHashMap<>();
        Map<String, Pair<String, String>> compiledInfo = parseBuildTxt(buildTxt);
        for (Map.Entry<String, Pair<String, String>> entry : compiledInfo.entrySet()) {
            Pair<String, String> pair = entry.getValue();
            info.put(entry.getKey(),
                     SchemaInfo.precompiled(entry.getKey(), pair.second, pair.first));
        }

        File[] files = appHomeDir.listFiles(f -> {
            if (f.isFile()) {
                String name = f.getName();
                return !info.containsKey(name) && name.endsWith("_schema.conf");
            }
            return false;
        });
        for (File f : files) {
            SchemaInfo original = SchemaInfo.original(f.getName());
            info.put(original.getName(), original);
        }
        return new ArrayList<>(info.values());
    }

    public static SchemaInfo find(String conf) {
        FimeContext fimeContext = FimeContext.getInstance();
        File buildInfo = fimeContext.fileInCacheDir("build.txt");
        SchemaInfo info = null;
        if (FileStorage.hasFile(buildInfo)) {
            try (BufferedReader reader = new BufferedReader(new FileReader(buildInfo))) {
                String line = null;
                while ((line = reader.readLine()) != null) {
                    if (Strings.isNullOrEmpty(line)) break;
                    // fime_pinyin_schema.conf=123456.s/汉语拼音
                    String[] segments = line.split("[=/]");
                    if (conf.equals(segments[0])) {
                        info = SchemaInfo.precompiled(segments[0], segments[2], segments[1]);
                        break;
                    }
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (info == null) info = SchemaInfo.original(conf);
        return info;
    }

    public static boolean build(String conf) {
        boolean ok = false;
        SchemaInfo schemaInfo = find(conf);
        if (!schemaInfo.precompiled) {
            try {
                ok = build(schemaInfo, schemaInfo.loadConfig());
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        return ok;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static boolean build(SchemaInfo info, Config config) {
        if (info == null || info.precompiled || config == null) return false;

        boolean ok = false;
        FimeContext fimeContext = FimeContext.getInstance();
        File buildDir = fimeContext.getCacheDir();
        File buildTxt = fimeContext.fileInCacheDir("build.txt");
        if (!FileStorage.hasFile(buildTxt)) {
            try {
                buildTxt.createNewFile();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        Map<String, Pair<String, String>> buildInfo = parseBuildTxt(buildTxt);
        File compiledFile = new File(buildDir, Strings.simpleFormat("%s.s", info.conf));
        if (buildInfo.containsKey(info.conf)) {
            FileStorage.deleteFile(
                    fimeContext.fileInCacheDir(
                            Objects.requireNonNull(buildInfo.get(info.conf)).first));
        }
        try {
            Configs.serialize(config, new FileOutputStream(compiledFile));
            String keyboards = config.getString("keyboards");
            if (!FileStorage.hasFile(fimeContext.fileInCacheDir(keyboards + ".s"))) {
                Configs.serialize(Configs.load(fimeContext.fileInAppHome(keyboards), true),
                                  new FileOutputStream(new File(buildDir, keyboards + ".s")));
            }
            buildInfo.put(info.conf, new Pair<>(compiledFile.getName(), info.getName()));
            updateBuildTxt(buildTxt, buildInfo);
            ok = true;
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return ok;
    }

    public static void clearBuild() {
        FimeContext fimeContext = FimeContext.getInstance();
        File buildTxt = fimeContext.fileInCacheDir("build.txt");
        FileStorage.deleteFile(buildTxt);
    }

    public static boolean validate(String conf) {
        FimeContext fimeContext = FimeContext.getInstance();
        File buildTxt = fimeContext.fileInCacheDir("build.txt");
        Map<String, Pair<String, String>> compiledInfo = parseBuildTxt(buildTxt);
        if (compiledInfo.containsKey(conf)) return true;
        if (!FileStorage.hasFile(fimeContext.fileInAppHome(conf))) return false;

        Config config = null;
        try {
            config = Configs.load(fimeContext.fileInAppHome(conf), false);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        if (config == null) return false;
        return config.hasPath("name")
                && config.hasPath("keyboards")
                && config.hasPath("inputEditor")
                && config.hasPath("translator")
                && config.hasPath("ejector");
    }

    public static void delete(String conf) {
        FimeContext fimeContext = FimeContext.getInstance();
        FileStorage.deleteFile(fimeContext.fileInAppHome(conf));

        File buildDir = fimeContext.getCacheDir();
        File buildTxt = fimeContext.fileInCacheDir("build.txt");
        Map<String, Pair<String, String>> compiledInfo = parseBuildTxt(buildTxt);
        if (compiledInfo.containsKey(conf)) {
            FileStorage.deleteFile(new File(buildDir, compiledInfo.get(conf).first));
            compiledInfo.remove(conf);
            updateBuildTxt(buildTxt, compiledInfo);
        }
    }

    private static Map<String, Pair<String, String>> parseBuildTxt(File buildTxt) {
        if (!FileStorage.hasFile(buildTxt)) return Collections.EMPTY_MAP;

        Map<String, Pair<String, String>> buildInfo = new LinkedHashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(buildTxt))) {
            String line = null;
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty()) break;
                // line: fime_pinyin_schema.conf=123456.s/汉语拼音
                String[] segments = line.split("[=/]");
                String conf = segments[0];
                String compiled = segments[1];
                String name = segments[2];
                buildInfo.put(conf, new Pair<>(compiled, name));
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return buildInfo;
    }

    private static void updateBuildTxt(File buildTxt, Map<String, Pair<String, String>> buildInfo) {
        try (FileWriter writer = new FileWriter(buildTxt)) {
            for (Map.Entry<String, Pair<String, String>> entry : buildInfo.entrySet()) {
                Pair<String, String> pair = entry.getValue();
                writer.write(Strings.simpleFormat("%s=%s/%s\n", entry.getKey(), pair.first,
                                                  pair.second));
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static class SchemaInfo {

        public final String conf;
        public final boolean precompiled;
        public final String compiledName;
        private String name;
        private transient Config config;

        SchemaInfo(String conf, boolean precompiled, String name, String compiledName) {
            this.conf = conf;
            this.precompiled = precompiled;
            this.name = name;
            this.compiledName = compiledName;
        }

        static SchemaInfo precompiled(String conf, String name, String compiledName) {
            return new SchemaInfo(conf, true, name, compiledName);
        }

        static SchemaInfo original(String conf) {
            return new SchemaInfo(conf, false, null, null);
        }

        public String getName() {
            if (name == null && !precompiled) {
                try {
                    Config config = loadConfig();
                    name = config.getString("name");
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return name;
        }

        public Config loadConfig() throws IOException {
            if (config == null) {
                FimeContext fimeContext = FimeContext.getInstance();
                if (precompiled) {
                    config = Configs.deserialize(fimeContext.fileInCacheDir(compiledName));
                }
                else {
                    config = Configs.load(FimeContext.getInstance()
                                                     .fileInAppHome(conf), true);
                }
            }
            return config;
        }
    }
}
