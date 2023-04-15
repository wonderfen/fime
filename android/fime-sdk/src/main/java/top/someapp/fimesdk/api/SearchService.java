/*
 * Copyright (c) 2023 Fime project https://fime.fit
 * Initial author: zelde126@126.com
 */

package top.someapp.fimesdk.api;

import android.os.Binder;

import java.util.List;

/**
 * @author zwz
 * Create on 2023-01-03
 */
public interface SearchService {

    boolean isAlive();

    List<Candidate> search(String code, int limit);

    void stop();

    class ServiceBinder extends Binder {

        private final SearchService service;

        public ServiceBinder(SearchService service) {
            this.service = service;
        }

        public SearchService getService() {
            return service;
        }
    }
}
