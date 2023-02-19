package top.someapp.fime.pinyin;

import top.someapp.fime.AppDatabase;
import top.someapp.fime.dao.HmmDao;
import top.someapp.fime.entity.HmmEmission;
import top.someapp.fime.entity.HmmStartAndEmission;
import top.someapp.fime.entity.HmmTransition;
import top.someapp.fimesdk.FimeContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author zwz
 * Create on 2023-02-01
 */
public class RoomHmmParams implements HmmParams {

    private static final int kStateLimit = 512;
    private static final int kTransitionLimit = 128;
    private final HmmDao hmmDao;

    public RoomHmmParams() {
        hmmDao = AppDatabase.getInstance(FimeContext.getInstance()
                                                    .getContext())
                            .hmmDao();
    }

    @Override public double start(String state, double fallback) {
        double rtn = fallback;
        try {
            rtn = hmmDao.start(state);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return rtn;
    }

    @Override public double emission(String state, String observation, double fallback) {
        double rtn = fallback;
        try {
            rtn = hmmDao.emission(state, observation);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return rtn;
    }

    @Override
    public List<Transition> transition(List<StartAndEmission> from, List<StartAndEmission> to) {
        Set<String> fromTo = new HashSet<>();
        // avoid  android.database.sqlite.SQLiteException: too many SQL variables
        if (from.size() > 31) from = from.subList(0, 31);
        if (to.size() > 31) to = to.subList(0, 31);
        for (StartAndEmission f : from) {
            for (StartAndEmission t : to) {
                if (fromTo.size() >= 999) break; // max variables is 999?
                String text = f.getText();
                fromTo.add(text.substring(text.length() - 1) + t.getText());
            }
            if (fromTo.size() >= 999) break; // max variables is 999?
        }
        List<HmmTransition> transition = hmmDao.transition(fromTo, kTransitionLimit);
        if (transition == null || transition.isEmpty()) {
            HmmTransition t = new HmmTransition();
            t.setFrom(from.get(0)
                          .getText());
            t.setTo(to.get(0)
                      .getText());
            t.setTransition(-1);
            return Arrays.asList(t);
        }
        return new ArrayList<>(transition);
    }

    @Override public List<StartAndEmission> getStartAndEmission(String observation) {
        List<HmmStartAndEmission> startAndEmission = hmmDao.getStartAndEmission(observation,
                                                                                kStateLimit);
        if (startAndEmission == null || startAndEmission.isEmpty()) {
            StartAndEmission e = new HmmStartAndEmission();
            e.setCode(observation);
            e.setText(observation);
            return Arrays.asList(e);
        }
        return new ArrayList<>(startAndEmission);
    }

    @Override
    public List<StartAndEmission> getStartAndEmission(String observation, boolean prefixMatch) {
        List<HmmStartAndEmission> startAndEmission;
        if (prefixMatch) {
            startAndEmission = hmmDao.getStartAndEmissionLike(observation + "%", kStateLimit);
        }
        else {
            startAndEmission = hmmDao.getStartAndEmission(observation, kStateLimit);
        }
        if (startAndEmission == null || startAndEmission.isEmpty()) {
            return Collections.EMPTY_LIST;
        }
        return new ArrayList<>(startAndEmission);
    }

    public List<HmmEmission> singlesWithPrefix(String prefix, int limit) {
        return hmmDao.codeStartWith(prefix, limit);
    }
}
