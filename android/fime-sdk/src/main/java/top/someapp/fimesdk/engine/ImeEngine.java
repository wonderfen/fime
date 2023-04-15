/*
 * Copyright (c) 2023 Fime project https://fime.fit
 * Initial author: zelde126@126.com
 */

package top.someapp.fimesdk.engine;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import androidx.annotation.Keep;
import top.someapp.fimesdk.FimeContext;
import top.someapp.fimesdk.api.Candidate;
import top.someapp.fimesdk.api.CandidateHandler;
import top.someapp.fimesdk.api.SearchService;

import java.util.List;

/**
 * @author zwz
 * Created on 2022-12-27
 */
@Keep
public class ImeEngine implements ServiceConnection {

    private static final String TAG = "ImeEngine";
    private final HandlerThread workThread;
    private final Handler handler;
    private SearchService searchService;

    public ImeEngine() {
        this.workThread = new HandlerThread(TAG);
        this.workThread.start();
        this.handler = new Handler(workThread.getLooper());
        Context context = FimeContext.getInstance()
                                     .getContext();
        Intent bindIntent = new Intent(context, PinyinService.class);
        context.bindService(bindIntent, this, Context.BIND_AUTO_CREATE);
    }

    public void search(String code, CandidateHandler handler) {
        this.handler.post(() -> {
            if (searchService != null && searchService.isAlive()) {
                List<Candidate> result = searchService.search(code, 9);
                if (handler != null) handler.handle(result);
            }
            else {
                // HmmEmmitDao dao = AppDatabase.getInstance(FimeContext.getInstance()
                //                                                      .getContext())
                //                              .hmmEmmitDao();
                // List<HmmEmmit> hmmEmmits = dao.findByCode(code);
                // Log.i(TAG, "code=" + code + ", hmmEmmits.size=" + hmmEmmits.size());
                // if (handler != null) {
                //     List<Candidate> candidateList = new ArrayList<>(hmmEmmits.size());
                //     for (HmmEmmit e : hmmEmmits) {
                //         candidateList.add(new Candidate(code, e.getText()));
                //     }
                //     handler.handle(candidateList);
                // }
            }
        });
    }

    public void stop() {
        workThread.quit();
        if (searchService != null) searchService.stop();
    }

    @Override public void onServiceConnected(ComponentName name, IBinder binder) {
        searchService = ((PinyinService.ServiceBinder) binder).getService();
    }

    @Override public void onServiceDisconnected(ComponentName name) {
        searchService = null;
    }
}
