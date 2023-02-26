package top.someapp.fimesdk.pinyin;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import org.trie4j.Trie;
import org.trie4j.Tries;
import top.someapp.fimesdk.api.Syncopate;

import java.util.List;

/**
 * @author zwz
 * Created on 2023-02-06
 */
@Keep
class PinyinSyncopate implements Syncopate {

    // 合法的音节
    private static final String[] kSyllables = {
            // @formatter:off
            "a","ai","an","ang","ao",
            "ba","bai","ban","bang","bao","bei","ben","beng","bi","bian","biao","bie","bin","bing","bo","bu",
            "ca","cai","can","cang","cao","ce","cei","cen","ceng","ci","cong","cou","cu","cuan","cui","cun","cuo",
            "cha","chai","chan","chang","chao","che","chen","cheng","chi","chong","chou","chu","chua","chuai","chuan","chuang","chui","chun","chuo",
            "da","dai","dan","dang","dao","de","dei","den","deng","di","dia","dian","diao","die","din","ding","diu","dong","dou","du","duan","dui","dun","duo",
            "e","ei","en","eng","er","fa","fan","fang","fei","fen","feng","fiao","fo","fou","fu","fuo",
            "ga","gai","gan","gang","gao","ge","gei","gen","geng","gong","gou","gu","gua","guai","guan","guang","gui","gun","guo",
            "ha","hai","han","hang","hao","he","hei","hen","heng","hong","hou","hu","hua","huai","huan","huang","hui","hun","huo",
            "ji","jia","jian","jiang","jiao","jie","jin","jing","jiong","jiu","ju","juan","jue","jun",
            "ka","kai","kan","kang","kao","ke","kei","ken","keng","kong","kou","ku","kua","kuai","kuan","kuang","kui","kun","kuo",
            "la","lai","lan","lang","lao","le","lei","leng","li","lia","lian","liang","liao","lie","lin","ling","liu","lo","long","lou","lu","luan","lue","lun","luo","lv","lvan","lve",
            "ma","mai","man","mang","mao","me","mei","men","meng","mi","mian","miao","mie","min","ming","miu","mo","mou","mu",
            "na","nai","nan","nang","nao","ne","nei","nen","neng","ng","ni","nia","nian","niang","niao","nie","nin","ning","niu","nong","nou","nu","nuan","nue","nun","nuo","nv","nve",
            "o","ou",
            "pa","pai","pan","pang","pao","pei","pen","peng","pi","pia","pian","piao","pie","pin","ping","po","pou","pu",
            "qi","qia","qian","qiang","qiao","qie","qin","qing","qiong","qiu","qu","quan","que","qun",
            "ran","rang","rao","re","ren","reng","ri","rong","rou","ru","rua","ruan","rui","run","ruo",
            "sa","sai","san","sang","sao","se","sen","seng","si","song","sou","su","suan","sui","sun","suo",
            "sha","shai","shan","shang","shao","she","shei","shen","sheng","shi","shou","shu","shua","shuai","shuan","shuang","shui","shun","shuo",
            "ta","tai","tan","tang","tao","te","tei","teng","ti","tia","tian","tiao","tie","ting","tong","tou","tu","tuan","tui","tun","tuo",
            "wa","wai","wan","wang","wei","wen","weng","wo","wu",
            "xi","xia","xian","xiang","xiao","xie","xin","xing","xiong","xiu","xu","xuan","xue","xun",
            "ya","yai","yan","yang","yao","ye","yi","yin","ying","yo","yong","you","yu","yuan","yue","yun",
            "za","zai","zan","zang","zao","ze","zei","zen","zeng","zi","zong","zou","zu","zuan","zui","zun","zuo",
            "zha","zhai","zhan","zhang","zhao","zhe","zhei","zhen","zheng","zhi","zhong","zhou","zhu","zhua","zhuai","zhuan","zhuang","zhui","zhun","zhuo",
            // @formatter:on
    };
    private Trie trie;

    public PinyinSyncopate() {
        init();
    }

    @Override public boolean isValidCode(@NonNull String code) {
        return trie.contains(code);
    }

    @Override public String segments(@NonNull String input, @NonNull List<String> result) {
        return segmentsLongest(input, 0, result);
    }

    @Override
    public String segments(@NonNull String input, @NonNull List<String> result, char delimiter) {
        return segmentsBy(delimiter, input, 0, result);
    }

    @Override
    public String segments(@NonNull String input, @NonNull List<String> result, char delimiter,
            int from) {
        return segmentsBy(delimiter, input, from, result);
    }

    private String segmentsBy(char delimiter, String input, int from, List<String> result) {
        StringBuilder seg = new StringBuilder(6);
        int i = from;
        int index;
        for (int end = input.length(); i < end; i++) {
            char ch = input.charAt(i);
            if (ch == delimiter) {
                if (seg.length() > 0) {
                    StringBuilder temp = new StringBuilder(6);
                    index = trie.findLongestWord(seg.toString(), 0, seg.length(), temp);
                    if (index >= 0) {
                        result.add(temp.toString());
                        if (temp.length() < seg.length()) i--;   // 回退
                        seg.delete(0, temp.length());
                        continue;
                    }
                    else {
                        break;
                    }
                }
            }
            seg.append(ch);
            if (seg.length() >= 6) {
                if (trie.contains(seg.toString())) {
                    result.add(seg.toString());
                    seg.setLength(0);
                }
                else {
                    StringBuilder temp = new StringBuilder(6);
                    index = trie.findLongestWord(seg.toString(), 0, seg.length(), temp);
                    if (index >= 0) {
                        result.add(temp.toString());
                        i -= (seg.length() - temp.length());
                        seg.setLength(0);
                    }
                }
            }
        }
        if (seg.length() > 0) {
            StringBuilder temp = new StringBuilder(12);
            do {
                index = trie.findLongestWord(seg.toString(), 0, seg.length(), temp);
                if (index == 0) {
                    result.add(temp.toString());
                    seg.delete(0, temp.length());
                    temp.setLength(0);
                }
            } while (index == 0);
        }
        return seg.toString();
    }

    private String segmentsLongest(String input, int from, List<String> result) {
        StringBuilder seg = new StringBuilder(6);
        int i = from;
        final int end = input.length();
        while (i < end) {
            int index = trie.findLongestWord(input, i, end, seg);
            if (index < 0) break;
            i += seg.length();
            result.add(seg.toString());
            seg.setLength(0);
        }
        return input.substring(i);
    }

    private void init() {
        trie = Tries.mutableTrie();
        for (String py : kSyllables) {
            trie.insert(py);
        }
        trie.freeze();
    }
}
