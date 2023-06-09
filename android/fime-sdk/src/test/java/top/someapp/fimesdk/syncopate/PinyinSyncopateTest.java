package top.someapp.fimesdk.syncopate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;
import top.someapp.fimesdk.api.Syncopate;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zwz
 * Created on 2023-02-06
 */
public class PinyinSyncopateTest {

    private static Syncopate syncopate;

    @BeforeClass
    public static void setUp() {
        syncopate = new PinyinSyncopate();
    }

    @Test
    public void testSegment1() {
        List<String> result = new ArrayList<>(2);
        String remains = syncopate.segments("pinyin", result);
        assertTrue(remains == null || remains.isEmpty());
        assertEquals(2, result.size());
        System.out.println("pinyin => ");
        for (String s : result) {
            System.out.println(s);
        }

        result.clear();
        remains = syncopate.segments("xi'an", result, '\'');
        assertTrue(remains == null || remains.isEmpty());
        assertEquals(2, result.size());
        System.out.println("xi'an => ");
        for (String s : result) {
            System.out.println(s);
        }
    }

    @Test
    public void testSegment2() {
        List<String> result = new ArrayList<>(2);
        String remains = syncopate.segments("pinyinsh", result);
        assertEquals("sh", remains);
        assertEquals(2, result.size());

        result.clear();
        remains = syncopate.segments("xi'anr", result, '\'');
        assertEquals("r", remains);
        assertEquals(2, result.size());
        result.clear();
        remains = syncopate.segments("xi'anrenm", result, '\'');
        assertEquals("m", remains);
        assertEquals(3, result.size());
    }

    @Test
    public void testSegment3() {
        List<String> result = new ArrayList<>(2);
        String remains = syncopate.segments("zhuangshuangd", result);
        assertEquals("d", remains);
        assertEquals(2, result.size());

        result.clear();
        remains = syncopate.segments("xi'anninghaoy", result, '\'');
        assertEquals("y", remains);
        assertEquals(4, result.size());
    }

    @Test
    public void testSegment4() {
        List<String> result = new ArrayList<>(3);
        String remains = syncopate.segments("yiger", result, '\'');
        assertEquals("r", remains);
        assertEquals(2, result.size());
    }

    @Test
    public void testSegment5() {
        List<String> result = new ArrayList<>(3);
        String remains = syncopate.segments("yiqu'e", result, '\'');
        assertEquals(3, result.size());
        assertTrue(remains == null || remains.isEmpty());
    }

    @Test
    public void testSegment6() {
        List<String> result = new ArrayList<>(3);
        String remains = syncopate.segments("fe", result, '\'');
        assertEquals(0, result.size());
        assertEquals("fe", remains);
    }
}
