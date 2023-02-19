package top.someapp.fime.pinyin;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * @author zwz
 * Created on 2023-01-30
 */
public class ScoreAndPathQueue implements Comparator<ScoreAndPathQueue.ScoreAndPath>,
        Iterable<ScoreAndPathQueue.ScoreAndPath> {

    private final int capacity;
    private /*final*/ ScoreAndPath[] scoreAndPaths;
    private int index;
    private boolean ordered;

    public ScoreAndPathQueue(int capacity) {
        this.capacity = capacity;
        scoreAndPaths = new ScoreAndPath[capacity];
    }

    public void add(ScoreAndPath scoreAndPath) {
        if (index < capacity) { // has free
            scoreAndPaths[index] = scoreAndPath;
            index++;
            ordered = false;
        }
        else { // full
            ScoreAndPath[] temp = Arrays.copyOf(scoreAndPaths, capacity + 1);
            temp[capacity] = scoreAndPath;
            Arrays.sort(temp, this);
            scoreAndPaths = Arrays.copyOfRange(temp, 0, capacity);
            ordered = true;
        }
    }

    public int size() {
        return index;
    }

    public void sort() {
        if (ordered || index < 2) return;
        Arrays.sort(scoreAndPaths, 0, index, this);
        ordered = true;
    }

    public List<ScoreAndPath> toList() {
        sort();
        if (index == 0) return Collections.EMPTY_LIST;
        return Arrays.asList(scoreAndPaths)
                     .subList(0, index);
    }

    @Override public int compare(ScoreAndPath o1, ScoreAndPath o2) {
        return o2.compareTo(o1);
    }

    @Override public Iterator<ScoreAndPath> iterator() {
        sort();

        return new Iterator<ScoreAndPath>() {
            private int i = 0;

            @Override public boolean hasNext() {
                return i < index;
            }

            @Override public ScoreAndPath next() {
                return scoreAndPaths[i++];
            }
        };
    }

    public static class ScoreAndPath implements Comparable<ScoreAndPath> {

        private final double score;
        private final List<String> path;

        public ScoreAndPath(double score, List<String> path) {
            this.score = score;
            this.path = path;
        }

        public double getScore() {
            return score;
        }

        public List<String> getPath() {
            return path;
        }

        @Override public int compareTo(ScoreAndPath o) {
            return Double.compare(score, o.score);
        }

        @Override public String toString() {
            return score + ": " + path;
        }
    }
}
