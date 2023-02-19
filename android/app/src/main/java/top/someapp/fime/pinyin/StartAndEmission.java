package top.someapp.fime.pinyin;

/**
 * @author zwz
 * Created on 2023-01-30
 */
public class StartAndEmission implements HmmParams.StartAndEmission {

    private String text;
    private String code;
    private double start;   // 初始概率
    private double emission;    // 发射概率

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

    public double getStart() {
        return start;
    }

    public void setStart(double start) {
        this.start = start;
    }

    public double getEmission() {
        return emission;
    }

    public void setEmission(double emission) {
        this.emission = emission;
    }
}
