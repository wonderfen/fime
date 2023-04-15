/*
 * Copyright (c) 2023 Fime project https://fime.fit
 * Initial author: zelde126@126.com
 */

package top.someapp.fimesdk.api;

import java.util.List;

/**
 * @author zwz
 * Created on 2022-12-30
 */
public interface CandidateHandler {

    void handle(List<Candidate> candidateList);
}
