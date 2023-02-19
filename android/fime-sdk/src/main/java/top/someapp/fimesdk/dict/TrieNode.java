package top.someapp.fimesdk.dict;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zwz
 * Created on 2023-02-17
 */
class TrieNode implements Serializable {

    private final byte key;
    private List<TrieNode> children;
    private boolean end;

    TrieNode(byte key) {
        this.key = key;
        this.children = new ArrayList<>();
    }

    public byte getKey() {
        return key;
    }

    public char getChar() {
        return (char) key;
    }

    public boolean isEnd() {
        return end;
    }

    @SuppressWarnings("unchecked")
    public Map<TrieNode, List<TrieNode>> branch() {
        if (isEnd() || children.isEmpty()) return Collections.EMPTY_MAP;
        Map<TrieNode, List<TrieNode>> branchMap = new HashMap<>(128);
        for (TrieNode node : children) {
            branchMap.put(node, node.children);
        }
        return branchMap;
    }

    void freeze() {
        if (children.isEmpty()) end = true;
        for (TrieNode node : children) {
            node.freeze();
        }
    }

    void add(TrieNode child) {
        if (!end) children.add(child);
    }

    TrieNode find(byte key) {
        if (this.key == key) return this;
        for (TrieNode n : children) {
            if (n.key == key) return n;
        }
        return null;
    }
}
