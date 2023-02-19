package top.someapp.fime.pinyin;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author zwz
 * Created on 2023-01-30
 */
public abstract class DbHmmParams implements HmmParams {

    private static final int kStateLimit = 512;
    private static final int kTransitionLimit = 128;

    public DbHmmParams() {
        DbUtil.connect();
    }

    @Override public double start(String state, double fallback) {
        System.out.println("start(" + state + ")");
        double rtn = fallback;
        try {
            Map<String, Object> map = DbUtil.selectMap(
                    "select power from t_hmm_start where text_ = ?", state);
            rtn = ((Number) map.get("power")).doubleValue();
        }
        catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return rtn;
    }

    @Override public double emission(String state, String observation, double fallback) {
        System.out.println("emission(" + state + ", " + observation + ")");
        double rtn = fallback;
        try {
            Map<String, Object> map = DbUtil.selectMap(
                    "select power_ from t_hmm_emmit where text_ = ? and code = ? order by power_ "
                            + "desc limit ?",
                    state,
                    observation, kStateLimit);
            rtn = ((Number) map.get("power_")).doubleValue();
        }
        catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return rtn;
    }

    @Override
    public List<Transition> transition(List<StartAndEmission> from,
            List<StartAndEmission> to) {
        System.out.println("transition(..., ...)");
        final int capacity = from.size() * to.size();
        Set<String> combo = new HashSet<>(capacity);
        List<Object> params = new ArrayList<>(capacity);
        StringBuilder placeholders = new StringBuilder("(");
        boolean first = true;
        for (StartAndEmission f : from) {
            for (StartAndEmission t : to) {
                String ft = f.getText() + t.getText();
                if (!combo.contains(ft)) {
                    combo.add(ft);
                    params.add(ft);
                    if (first) {
                        placeholders.append("?");
                        first = false;
                    }
                    else {
                        placeholders.append(", ?");
                    }
                }
            }
        }
        placeholders.append(") order by power desc");

        List<Transition> rtn = new ArrayList<>(capacity);
        try {
            List<Map<String, Object>> list = DbUtil.selectList(
                    "select from_, to_, power from t_hmm_trans where from_ || to_ in " + placeholders,
                    params.toArray());
            for (Map<String, Object> map : list) {
                String f = (String) map.get("from_");
                String t = (String) map.get("to_");
                top.someapp.fime.pinyin.Transition transition = new top.someapp.fime.pinyin.Transition();
                transition.setFrom(f);
                transition.setTo(t);
                transition.setTransition(((Number) map.get("power")).doubleValue());
                rtn.add(transition);
            }
        }
        catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return rtn;
    }

    @Override public List<StartAndEmission> getStartAndEmission(String observation) {
        System.out.println("getStateAndEmission(" + observation + ")");
        List<StartAndEmission> result = new ArrayList<>();
        try {
            List<Map<String, Object>> list = DbUtil.selectList(
                    "SELECT\n"
                            + "\tthe.text_,\n"
                            + "\tthe.code,\n"
                            + "\tthe.power_ as emission,\n"
                            + "\tths.power as start_\n"
                            + "from\n"
                            + "\tt_hmm_start ths\n"
                            + "left join t_hmm_emmit the\n"
                            + "\ton\n"
                            + "\tths.text_ = the.text_\n"
                            + "where\n"
                            + "\tthe.code like ?\n"
                            + "order by\n"
                            + "\tthe.power_ desc\n"
                            + "limit ?",
                    observation + "%", kTransitionLimit);
            for (Map<String, Object> map : list) {
                top.someapp.fime.pinyin.StartAndEmission startAndEmission = new top.someapp.fime.pinyin.StartAndEmission();
                startAndEmission.setText((String) map.get("text_"));
                startAndEmission.setCode(observation);
                startAndEmission.setStart(((Number) map.get("start_")).doubleValue());
                startAndEmission.setEmission(((Number) map.get("emission")).doubleValue());
                result.add(startAndEmission);
            }
        }
        catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return result;
    }
}
