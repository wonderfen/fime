/*
 * Copyright (c) 2023 Fime project https://fime.fit
 * Initial author: zelde126@126.com
 */

package top.someapp.fimesdk.dict;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zwz
 * Created on 2023-02-17
 */
public class Trie {

    private final TrieNode root;

    public Trie() {
        this.root = new TrieNode((byte) 0);
    }

    public void add(String key) {
        char[] letters = key.toCharArray();
        TrieNode node = root;
        for (int i = 0, len = letters.length; i < len; i++) {
            byte b = (byte) letters[i];
            TrieNode target = node.find(b);
            if (target == null) {
                target = new TrieNode(b);
                node.add(target);
            }
            node = target;
        }
    }

    public void freeze() {
        root.freeze();
    }

    public String prefixSearch(String key) {
        StringBuilder value = new StringBuilder(key.length());
        List<TrieNode> nodes = prefixMatchNode(key);
        for (TrieNode n : nodes) {
            value.append(n.getChar());
        }
        return value.toString();
    }

    public boolean contains(String key) {
        List<TrieNode> nodes = prefixMatchNode(key);
        return nodes.size() == key.length();
    }

    public String fullMatchSearch(String key) {
        List<TrieNode> nodes = prefixMatchNode(key);
        if (nodes.size() == key.length()) {
            StringBuilder value = new StringBuilder(nodes.size());
            for (TrieNode n : nodes) {
                value.append(n.getChar());
            }
            return value.toString();
        }
        return null;
    }

    List<TrieNode> prefixMatchNode(String key) {
        char[] letters = key.toCharArray();
        List<TrieNode> nodes = new ArrayList<>(letters.length);
        TrieNode node = root;
        TrieNode temp;
        for (char ch : letters) {
            byte b = (byte) ch;
            temp = node.find(b);
            if (temp == null) break;
            nodes.add(temp);
            node = temp;
        }
        return nodes;
    }
}
