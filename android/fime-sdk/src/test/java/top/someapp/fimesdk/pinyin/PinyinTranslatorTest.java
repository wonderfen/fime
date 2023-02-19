package top.someapp.fimesdk.pinyin;

import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import top.someapp.fimesdk.api.Candidate;
import top.someapp.fimesdk.api.Translator;

import java.util.Arrays;
import java.util.List;

/**
 * @author zwz
 * Created on 2023-02-06
 */
public class PinyinTranslatorTest {

    private Translator translator;

    @Before
    public void setUp() throws Exception {
        translator = new PinyinTranslator();
    }

    @Test
    public void testSearch() {
        List<String> codes = Arrays.asList("pin yin shu ru".split(" "));
        for (int i = 1, len = codes.size(); i <= len; i++) {
            List<Candidate> candidates = translator.translate(codes.subList(0, i), 3);
            assertNotNull(candidates);
            System.out.println(" == search result: ");
            for (Candidate c : candidates) {
                System.out.println(c);
            }
        }
    }
}
