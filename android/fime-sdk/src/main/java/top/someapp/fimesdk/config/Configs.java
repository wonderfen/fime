package top.someapp.fimesdk.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import top.someapp.fimesdk.utils.Serializes;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.util.Map;

/**
 * @author zwz
 * Created on 2022-12-23
 */
public class Configs {

    private Configs() {
        // no instance
    }

    public static Config load(File file, boolean resolve) {
        Config config = ConfigFactory.parseFile(file);
        return resolve ? config.resolve() : config;
    }

    public static Config load(InputStream ins, boolean resolve) {
        return load(new InputStreamReader(ins), resolve);
    }

    public static Config load(Reader reader, boolean resolve) {
        Config config = ConfigFactory.parseReader(reader);
        try {
            reader.close();
        }
        catch (IOException ignored) {
        }
        return resolve ? config.resolve() : config;
    }

    public static Config fromMap(Map<String, Object> map) {
        return ConfigFactory.parseMap(map);
    }

    public static void serialize(Config config, OutputStream out) throws IOException {
        Serializes.serialize(config.root()
                                   .unwrapped(), out);
    }

    public static Config deserialize(File source) throws IOException {
        if (source.exists() && source.isFile()) return deserialize(new FileInputStream(source));
        return null;
    }

    public static Config deserialize(InputStream ins) throws IOException {
        Map<String, Object> map = Serializes.deserialize(ins);
        return fromMap(map);
    }
}
