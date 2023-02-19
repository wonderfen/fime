package top.someapp.fimesdk.dict;

import org.junit.Test;

/**
 * @author zwz
 * Created on 2023-02-17
 */
public class TrieTest {

    @Test
    public void testSearch() {
        Trie trie = new Trie();
        trie.add("a fe");
        trie.add("a fei");
        trie.add("a fei zheng zhuan");
        trie.freeze();
        System.out.println(trie.prefixSearch("a f"));
        System.out.println(trie.fullMatchSearch("a fe"));
        System.out.println(trie.fullMatchSearch("a fei"));
    }
}
