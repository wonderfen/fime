package top.someapp.fime.pinyin;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import top.someapp.fimesdk.utils.Logs;

import java.util.List;

/**
 * @author zwz
 * Create on 2023-02-01
 */
public class RoomHmmParamsTest {

    private static final String TAG = "Fime/Test";
    private HmmParams hmmParams;

    @Before
    public void setUp() {
        hmmParams = new RoomHmmParams();
    }

    @Test
    public void testStart() {
        double start = hmmParams.start("一", -1);
        assertTrue(start != -1);
        Logs.i("一, start=" + start);
    }

    @Test
    public void testEmission() {
        double emission = hmmParams.emission("和", "he", -1);
        assertTrue(emission != -1);
        Logs.i("和(he), emission=" + emission);
    }

    @Test
    public void testTransition() {
        List<HmmParams.StartAndEmission> from = hmmParams.getStartAndEmission("yi");
        List<HmmParams.StartAndEmission> to = hmmParams.getStartAndEmission("ge");
        List<HmmParams.Transition> transition = hmmParams.transition(from, to);
        assertFalse(transition == null || transition.isEmpty());
        for (int i = 0, len = transition.size(); i < Math.min(len, 3); i++) {
            HmmParams.Transition t = transition.get(i);
            Logs.i(t.getFrom() + " -> " + t.getTo() + ", transition=" + t.getTransition());
        }
    }

    @Test
    public void testGetStartAndEmission() {
        List<HmmParams.StartAndEmission> yi = hmmParams.getStartAndEmission("yi");
        assertFalse(yi == null || yi.isEmpty());
    }
}
