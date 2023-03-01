package top.someapp.fimesdk.syncopate;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import top.someapp.fimesdk.api.Syncopate;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zwz
 * Created on 2023-03-01
 */
public class SyncopatesTest {

    @Test
    public void testWhole() {
        Syncopate syncopate = Syncopates.create("whole");
        String input = "abcxyz";
        List<String> result = new ArrayList<>(1);
        String remains = syncopate.segments(input, result);
        assertEquals("", remains);
        assertEquals(1, result.size());
        assertEquals(input, result.get(0));
    }

    @Test
    public void testLength() {
        Syncopate syncopate = Syncopates.create("length:3");
        String input = "abc123xy";
        List<String> result = new ArrayList<>(3);
        String remains = syncopate.segments(input, result);
        assertEquals("", remains);
        assertEquals(3, result.size());
        assertEquals("abc", result.get(0));
        assertEquals("123", result.get(1));
        assertEquals("xy", result.get(2));
    }

    @Test
    public void testRegex() {
        Syncopate syncopate = Syncopates.create("regex:([^aeiou][aeiou]*)");
        String input = "laleliwoyu";
        List<String> result = new ArrayList<>();
        String remains = syncopate.segments(input, result);
        assertEquals("", remains);
        assertEquals(5, result.size());
        assertEquals("la", result.get(0));
        assertEquals("le", result.get(1));
        assertEquals("li", result.get(2));
        assertEquals("wo", result.get(3));
        assertEquals("yu", result.get(4));
    }
}
