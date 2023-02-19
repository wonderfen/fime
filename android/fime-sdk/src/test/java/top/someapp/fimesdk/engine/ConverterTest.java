package top.someapp.fimesdk.engine;

import static org.junit.Assert.assertEquals;

import com.typesafe.config.Config;
import org.junit.BeforeClass;
import org.junit.Test;
import top.someapp.fimesdk.config.Configs;
import top.someapp.fimesdk.utils.Strings;

/**
 * @author zwz
 * Create on 2023-02-13
 */
public class ConverterTest {

    private static final String letters = "abcdefghijklmnopqrstuvwxyz";
    private static Config zrmConfig;
    private static Converter converter;

    @BeforeClass
    public static void beforeClass() throws Exception {
        converter = new Converter();
        zrmConfig = Configs.load(ConverterTest.class.getResourceAsStream("/zrm_schema.conf"),
                                 false);
    }

    @Test
    public void testConvert() {
        // spellRule.addRule("U:");
        converter.addRule("R:^v(.*)=ZH$1");
        converter.addRule("R:^i(.*)=CH$1");
        converter.addRule("R:^u(.*)=SH$1");
        converter.addRule("R:^([aoe])\\1=$1"); // aoe 重复两次
        converter.addRule("R:^(.)q=$1iu");
        converter.addRule("R:^([bpdnljqx])w=$1ia");
        converter.addRule("R:^([gkhr])w=$1ua");
        converter.addRule("R:^([ZCS]H)w=$1ua");
        converter.addRule("R:^([^e]H?)r=$1uan");
        converter.addRule("R:^(.H?)t=$1ue");
        converter.addRule("R:^([bpmdtnljqxy])y=$1ing");
        converter.addRule("R:^([gkh])y=$1uai");
        converter.addRule("R:^([ZCS]H)y=$1uai");
        converter.addRule("R:^([dtnlgkhzcsr])o=$1uo");
        converter.addRule("R:^([ZCS]H)o=$1uo");
        converter.addRule("R:^(.H?)p=$1un");
        converter.addRule("R:^([jqx])s=$1iong");
        converter.addRule("R:^([^jqx]H?)s=$1ong");
        converter.addRule("R:^([nljqx])d=$1iang");
        converter.addRule("R:^([^nljqx]H?)d=$1uang");
        converter.addRule("R:^(.H?)f=$1en");
        converter.addRule("R:^(.H?)g=$1eng");
        converter.addRule("R:^(.H?)h=$1ang");
        converter.addRule("R:^(.H?)j=$1an");
        converter.addRule("R:^(.H?)k=$1ao");
        converter.addRule("R:^(.H?)l=$1ai");
        converter.addRule("R:^(.H?)z=$1ei");
        converter.addRule("R:^(.H?)x=$1ie");
        converter.addRule("R:^(.H?)c=$1iao");
        converter.addRule("R:^([dtgkhzcsr])v=$1ui");
        converter.addRule("R:^([ZCS]H)v=$1ui");
        converter.addRule("R:^(.H?)b=$1ou");
        converter.addRule("R:^([^ae])n=$1in");
        converter.addRule("R:^(.H?)m=$1ian");
        converter.addRule("R:^([ae])\\1ng=$1ng");    // ang|eng
        converter.addRule("M:ZCSH=zcsh");
        for (char first : letters.toCharArray()) {
            for (char tail : letters.toCharArray()) {
                String input = new String(new char[] { first, tail });
                String output = converter.convert(input);
                System.out.println(Strings.simpleFormat("%s => %s", input, output));
                if (zrmConfig.hasPath(input)) {
                    assertEquals(zrmConfig.getString(input), output);
                }
                // else {
                //     assertEquals(input, output);
                // }
            }
        }
    }
}
