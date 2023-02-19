package top.someapp.fime.pinyin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zwz
 * Created on 2023-01-30
 */
public class Viterbi {

    private static final double kMinProb = HmmParams.MIN_PROB;
    private static boolean debug;

    public static List<ScoreAndPathQueue.ScoreAndPath> compute(HmmParams hmmParams,
            List<String> observations, int pathNum) {
        List<Map<String, ScoreAndPathQueue>> V = new ArrayList<>();
        String curObs = observations.get(0);
        List<HmmParams.StartAndEmission> prevStates;
        List<HmmParams.StartAndEmission> curStates = hmmParams.getStartAndEmission(curObs);
        double score;
        List<String> path;
        V.add(new HashMap<>());
        for (HmmParams.StartAndEmission state : curStates) {
            String text = state.getText();
            score = Math.max(state.getStart(), kMinProb) + Math.max(state.getEmission(), kMinProb);
            path = new ArrayList<>();
            path.add(text);

            if (!V.get(0)
                  .containsKey(state)) {
                V.get(0)
                 .put(text, new ScoreAndPathQueue(pathNum));
            }
            V.get(0)
             .get(text)
             .add(new ScoreAndPathQueue.ScoreAndPath(score, path));
        }
        printQueue(V.get(0));

        for (int i = 1; i < observations.size(); i++) {
            curObs = observations.get(i);
            if (V.size() == 2) V.remove(0);
            V.add(new HashMap<>());

            prevStates = curStates;
            curStates = hmmParams.getStartAndEmission(curObs);

            // calc transitions
            List<HmmParams.Transition> transitions = hmmParams.transition(prevStates, curStates);
            Map<String, HmmParams.Transition> ftMap = new HashMap<>(transitions.size());
            for (HmmParams.Transition trans : transitions) {
                ftMap.put(trans.getFrom() + trans.getTo(), trans);
            }
            for (HmmParams.StartAndEmission state : curStates) {
                String text = state.getText();
                if (!V.get(1)
                      .containsKey(text)) {
                    V.get(1)
                     .put(text, new ScoreAndPathQueue(pathNum));
                }
                ArrayList<String> newPath;
                for (HmmParams.StartAndEmission prev : prevStates) {
                    String prevText = prev.getText();
                    for (ScoreAndPathQueue.ScoreAndPath scoreAndPath : V.get(0)
                                                                        .get(prevText)) {
                        double transProb = kMinProb;
                        if (ftMap.containsKey(prevText + text)) {
                            transProb = ftMap.get(prevText + text)
                                             .getTransition();
                        }
                        score = scoreAndPath.getScore() + transProb + Math.max(state.getEmission(),
                                                                               kMinProb);
                        newPath = new ArrayList<>();
                        newPath.addAll(scoreAndPath.getPath());
                        newPath.add(text);
                        V.get(1)
                         .get(text)
                         .add(new ScoreAndPathQueue.ScoreAndPath(score, newPath));
                    }
                }
                printQueue(V.get(1));
            }
        }

        ScoreAndPathQueue result = new ScoreAndPathQueue(pathNum);
        Map<String, ScoreAndPathQueue> last = observations.size() > 1 ? V.get(1) : V.get(0);
        for (ScoreAndPathQueue queue : last.values()) {
            for (ScoreAndPathQueue.ScoreAndPath scoreAndPath : queue) {
                result.add(scoreAndPath);
            }
        }
        return result.toList();
    }

    public static void setDebug(boolean enable) {
        debug = enable;
    }

    private static void printQueue(Map<String, ScoreAndPathQueue> queueMap) {
        if (!debug) return;
        for (ScoreAndPathQueue queue : queueMap.values()) {
            for (ScoreAndPathQueue.ScoreAndPath scoreAndPath : queue) {
                System.out.println(scoreAndPath);
            }
        }
        System.out.println(" =========== ");
    }
}
