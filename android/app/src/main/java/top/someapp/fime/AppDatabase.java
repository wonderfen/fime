package top.someapp.fime;

import android.content.Context;
import android.util.Log;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import top.someapp.fime.dao.HmmDao;
import top.someapp.fime.entity.HmmEmission;
import top.someapp.fime.entity.HmmStart;
import top.someapp.fime.entity.HmmStartAndEmission;
import top.someapp.fime.entity.HmmTransition;

/**
 * @author zwz
 * Created on 2022-12-27
 */
@Database(
        entities = {
                HmmEmission.class,
                HmmStart.class,
                HmmTransition.class
        },
        views = { HmmStartAndEmission.class },
        version = AppDatabase.version,
        exportSchema = true)
public abstract class AppDatabase extends RoomDatabase {

    private static final String TAG = "AppDatabase";
    public static final int version = 108;
    private static final int initVersion = 99; // 0.0.99
    private static final String fileName = "fime.db";
    private static final byte[] lock = new byte[0];
    private static AppDatabase INSTANCE;

    public static AppDatabase getInstance(Context context) {
        synchronized (lock) {
            if (INSTANCE == null) {
                Log.i(TAG, "create AppDatabase instance.");
                // 从应用资源预填充数据库
                Builder<AppDatabase> builder = Room
                        .databaseBuilder(context, AppDatabase.class, fileName)
                        .createFromAsset(fileName)
                        .fallbackToDestructiveMigrationFrom(initVersion);
                INSTANCE = builder.build();
            }
            return INSTANCE;
        }
    }

    public abstract HmmDao hmmDao();
}
