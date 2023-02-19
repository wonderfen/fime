package top.someapp.fime.pinyin;

/**
 * @author zwz
 * Created on 2023-01-30
 */
public class Transition implements HmmParams.Transition {

    private String from;
    private String to;
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
