package top.someapp.fime.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;

/**
 * Hmm 模型的发射概率
 *
 * @author zwz
 * Created on 2023-02-01
 */
@Entity(
        tableName = HmmEmission.TABLE_NAME,
        primaryKeys = { "text_", "code" })
public class HmmEmission {

    public static final String TABLE_NAME = "t_hmm_emission";
    @ColumnInfo(name = "text_")
    @NonNull
    private String text = "";
    @ColumnInfo
    @NonNull
    private String code = "";
    @ColumnInfo(name = "power_")
    private double power;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public double getPower() {
        return power;
    }

    public void setPower(double power) {
        this.power = power;
    }
}
