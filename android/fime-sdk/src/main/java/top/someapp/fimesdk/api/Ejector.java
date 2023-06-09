/*
 * Copyright (c) 2023 Fime project https://fime.fit
 * Initial author: zelde126@126.com
 */

package top.someapp.fimesdk.api;

import androidx.annotation.NonNull;

/**
 * 提交器，将候选提交到接收文本的地方
 *
 * @author zwz
 * Create on 2023-01-31
 */
public interface Ejector extends ImeEngineAware, Configurable {

    void manualEject(@NonNull InputEditor editor);

    void ejectOnCandidateChange(@NonNull InputEditor editor);
}
