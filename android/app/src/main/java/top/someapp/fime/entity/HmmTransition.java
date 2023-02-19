package top.someapp.fime.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import top.someapp.fime.pinyin.HmmParams;

/**
 * @author zwz
 * Create on 2023-01-31
 */
@Entity(
        tableName = HmmTransition.TABLE_NAME,
        primaryKeys = { "from_", "to_" }
)
public class HmmTransition implements HmmParams.Transition {

    public static final String TABLE_NAME = "t_hmm_transition";
    @ColumnInfo(name = "from_")
    @NonNull
    private String from = "";
    @ColumnInfo(name = "to_")
    @NonNull
    private String to = "";
    @ColumnInfo(name = "power_")
    private double transition;

    @Override public String getFrom() {
        return from;
    }

    @Override public void setFrom(String from) {
        this.from = from;
    }

    @Override public String getTo() {
        return to;
    }

    @Override public void setTo(String to) {
        this.to = to;
    }

    @Override public double getTransition() {
        return transition;
    }

    @Override public void setTransition(double transition) {
        this.transition = transition;
    }
}
