package top.someapp.fime.entity;

import androidx.room.ColumnInfo;
import androidx.room.DatabaseView;
import top.someapp.fime.pinyin.HmmParams;

/**
 * 包含 Hmm 模型的初始概率和发射概率
 *
 * @author zwz
 * Create on 2023-01-31
 */
@DatabaseView(value = "SELECT "
        + " s.text_, "
        + " e.code, "
        + " s.power_ as start_,"
        + " e.power_ as emission "
        + "from " + HmmStart.TABLE_NAME + " s "
        + "left join " + HmmEmission.TABLE_NAME + " e "
        + " on s.text_ = e.text_ ", viewName = HmmStartAndEmission.VIEW_NAME)
public class HmmStartAndEmission implements HmmParams.StartAndEmission {

    public static final String VIEW_NAME = "v_start_and_emission";
    @ColumnInfo(name = "text_")
    private String text;
    @ColumnInfo
    private String code;
    @ColumnInfo(name = "start_")
    private double start;   // 初始概率
    @ColumnInfo
    private double emission;    // 发射概率

    @Override public String getText() {
        return text;
    }

    @Override public void setText(String text) {
        this.text = text;
    }

    @Override public String getCode() {
        return code;
    }

    @Override public void setCode(String code) {
        this.code = code;
    }

    @Override public double getStart() {
        return start;
    }

    @Override public void setStart(double start) {
        this.start = start;
    }

    @Override public double getEmission() {
        return emission;
    }

    @Override public void setEmission(double emission) {
        this.emission = emission;
    }
}
