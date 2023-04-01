package top.someapp.fime;

import android.app.Activity;
import android.app.Application;
import top.someapp.fimesdk.FimeContext;

/**
 * @author zwz
 * Created on 2022-12-20
 */
public class FimeApp extends Application {

    private Activity activity;

    @Override
    public void onCreate() {
        super.onCreate();
        new FimeContext(this);
        // AppDatabase.getInstance(this);
    }

    public Activity getActivity() {
        return activity;
    }

    void setActivity(Activity activity) {
        this.activity = activity;
    }
}

