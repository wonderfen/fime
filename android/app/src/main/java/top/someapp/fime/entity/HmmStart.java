package top.someapp.fime.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Hmm 模型的初始概率
 *
 * @author zwz
 * Create on 2023-02-01
 */
@Entity(
        tableName = HmmStart.TABLE_NAME
)
public class HmmStart {

    public static final String TABLE_NAME = "t_hmm_start";
    @PrimaryKey
    @ColumnInfo(name = "text_")
    @NonNull
    private String text = "";
    @ColumnInfo(name = "power_")
    private double power;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public double getPower() {
        return power;
    }

    public void setPower(double power) {
        this.power = power;
    }
}
