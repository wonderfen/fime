/*
 * Copyright (c) 2023 Fime project https://fime.fit
 * Initial author: zelde126@126.com
 */

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
