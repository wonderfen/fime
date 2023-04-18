/*
 * Copyright (c) 2023 Fime project https://fime.fit
 * Initial author: zelde126@126.com
 */

package top.someapp.fimesdk.api;

import android.os.Message;
import androidx.annotation.NonNull;

/**
 * @author zwz
 * Created on 2022-12-31
 */
@SuppressWarnings("SpellCheckingInspection")
public interface FimeHandler {

    @NonNull String getName();

    void handle(@NonNull Message msg);

    /**
     * 处理消息 msg
     *
     * @param msg 待处理的消息
     * @return true：已处理；false：未处理
     */
    boolean handleOnce(@NonNull Message msg);

    /**
     * 通知其他 Handler
     *
     * @param msg 消息
     */
    void send(@NonNull Message msg);
}
