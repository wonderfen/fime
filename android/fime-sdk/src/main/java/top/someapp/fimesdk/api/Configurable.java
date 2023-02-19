package top.someapp.fimesdk.api;

import com.typesafe.config.Config;

/**
 * @author zwz
 * Create on 2023-02-06
 */
public interface Configurable {

    Config getConfig();

    void reconfigure(Config config);
}
