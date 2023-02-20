package top.someapp.fimesdk.dict;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import it.unimi.dsi.fastutil.objects.Object2ObjectRBTreeMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayPriorityQueue;
import org.trie4j.patricia.MapPatriciaTrie;
import org.trie4j.patricia.MapPatriciaTrieNode;
import org.trie4j.util.IntArray;
import top.someapp.fimesdk.utils.Serializes;
import top.someapp.fimesdk.utils.Strings;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InvalidObjectException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 字典
 *
 * @author zwz
 * Created on 2023-02-04
 */
@Keep
public class Dict implements Comparator<Dict.Item> {

    private final String name;  // 词典名
    private int size;           // 包含的词条数
    private boolean sealed;     // 词典是否构建完成
    private MapPatriciaTrie<IntArray> mapTrie;        // 词条树
    private transient Map<String, List<Item>> itemMap;  // code -> Item[]
    private transient List<Item> items;

    public Dict(String name) {
        this.name = name;
        mapTrie = new MapPatriciaTrie<>();
    }

    @SuppressWarnings("unchecked")
    public static Dict loadFromCompiled(InputStream ins) throws IOException {
        Object[] data = Serializes.deserialize(ins, "FimeDict:.+\\d\n$");
        if (data == null || data.length != 2) {
            throw new InvalidObjectException("Load dict failed!!");
        }
        Dict dict = new Dict((String) data[0]);
        List<Item> items = (List<Item>) data[1];
        dict.size = 0;
        for (Item item : items) {
            dict.put(item);
        }
        dict.build();
        return dict;
    }

    public String getName() {
        return name;
    }

    public int getSize() {
        return size;
    }

    public boolean loadFromCsv(File csvFile) throws IOException {
        return loadFromCsv(new FileInputStream(csvFile));
    }

    public boolean loadFromCsv(InputStream ins) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(ins));
        String line = null;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("#")) continue;
            String[] parts = line.split("\t");
            String text = parts[0];
            String code = parts[1];
            if (parts.length > 2 && !Strings.isNullOrEmpty(parts[2])) {
                put(new Item(text, code, Integer.decode(parts[2])));
            }
            else {
                put(new Item(text, code));
            }
        }
        ins.close();
        return build();
    }

    public void compileTo(OutputStream out) throws IOException {
        String head = "FimeDict:" + getName() + "/" + getSize() + "\n";
        if (items != null) {
            Object[] obj = {
                    getName(),
                    items
            };
            Serializes.serialize(obj, out, head);
        }
    }

    public boolean search(@NonNull String prefix, @NonNull List<Item> result, int limit) {
        return search(prefix, -1, result, limit);
    }

    public boolean search(@NonNull String prefix, final int wordLength, @NonNull List<Item> result,
            int limit) {
        return search(prefix, wordLength, result, limit, this);
    }

    public boolean search(@NonNull String prefix, final int wordLength, @NonNull List<Item> result,
            int limit, Comparator<Item> comparator) {
        ObjectArrayPriorityQueue<Item> queue = new ObjectArrayPriorityQueue<>(limit * 2,
                                                                              comparator);
        if (mapTrie.contains(prefix)) { // 全部匹配
            MapPatriciaTrieNode<IntArray> node = mapTrie.getNode(prefix);
            IntArray value = node.getValue();
            final int index = value.get(0);
            final int size = value.get(1);
            for (int i = 0; i < size; i++) {
                queue.enqueue(items.get(index + i));
            }
        }
        else {    // 前缀预测匹配
            for (Map.Entry<String, IntArray> entry : mapTrie.predictiveSearchEntries(
                    prefix)) {
                IntArray value = entry.getValue();
                final int index = value.get(0);
                final int size = value.get(1);
                for (int i = 0; i < size; i++) {
                    Item item = items.get(index + i);
                    if (wordLength > 1) { // 词组查询
                        queue.enqueue(item);
                    }
                    else if (item.getLength() == wordLength) {  // 单字查询
                        queue.enqueue(item);
                    }
                }
            }
        }
        boolean hit = false;
        int count = 0;
        while (!queue.isEmpty() && count < limit) {
            if (!hit) hit = true;
            result.add(queue.dequeue());
            count++;
        }
        queue.clear();
        return hit;
    }

    public boolean searchPrefix(@NonNull String prefix, final int wordLength,
            @NonNull List<Item> result,
            int limit, Comparator<Item> comparator) {
        ObjectArrayPriorityQueue<Item> queue = new ObjectArrayPriorityQueue<>(limit * 2,
                                                                              comparator);
        // 前缀预测匹配
        for (Map.Entry<String, IntArray> entry : mapTrie.predictiveSearchEntries(
                prefix)) {
            IntArray value = entry.getValue();
            final int index = value.get(0);
            final int size = value.get(1);
            for (int i = 0; i < size; i++) {
                Item item = items.get(index + i);
                if (wordLength > 1) { // 词组查询
                    queue.enqueue(item);
                }
                else if (item.getLength() == wordLength) {  // 单字查询
                    queue.enqueue(item);
                }
            }
        }
        boolean hit = false;
        int count = 0;
        while (!queue.isEmpty() && count < limit) {
            if (!hit) hit = true;
            result.add(queue.dequeue());
            count++;
        }
        queue.clear();
        return hit;
    }

    @Override public int compare(Item o1, Item o2) {
        String code1 = o1.getCode();
        String code2 = o2.getCode();
        if (code1.equals(code2)) { // 编码相同时，weight 优先
            return o2.getWeight() - o1.getWeight();
        }
        if (o1.getLength() == o2.getLength()) { // 编码不同，词长相同， weight 优先
            return o2.getWeight() - o1.getWeight();
        }
        // 编码不同，词长不同，编码短的优先
        return code1.length() - code2.length();
    }

    @Override public String toString() {
        return "Dict{"
                + name +
                ", size=" + size +
                ", sealed=" + sealed +
                '}';
    }

    protected Dict put(Item item) {
        if (sealed) return this;
        if (itemMap == null) itemMap = new Object2ObjectRBTreeMap<>();
        if (!itemMap.containsKey(item.getCode())) {
            itemMap.put(item.getCode(), new ArrayList<>());
        }
        itemMap.get(item.getCode())
               .add(item);
        size++;
        return this;
    }

    private boolean build() {
        if (sealed) return false;

        items = new ArrayList<>(size);
        Iterator<Map.Entry<String, List<Item>>> it = itemMap.entrySet()
                                                            .iterator();
        int i = 0;
        while (it.hasNext()) {
            Map.Entry<String, List<Item>> next = it.next();
            List<Item> value = next.getValue();
            items.addAll(value);
            mapTrie.insert(next.getKey(), new IntArray(new int[] { i, value.size() }, 2));
            i += value.size();
            it.remove();
        }
        size = items.size();
        mapTrie.trimToSize();
        mapTrie.freeze();
        itemMap = null;
        sealed = true;
        return true;
    }

    @Keep
    public static class Item implements Serializable {

        private /*final*/ String text;  // for Serializable
        private /*final*/ String code;  // for Serializable
        private int weight;

        @Keep
        public Item() { // for Serializable
        }

        public Item(String text, String code) {
            this(text, code, 0);
        }

        public Item(String text, String code, int weight) {
            this.text = text;
            this.code = code;
            this.weight = weight;
        }

        public String getText() {
            return text;
        }

        public String getCode() {
            return code;
        }

        public int getWeight() {
            return weight;
        }

        public void setWeight(int weight) {
            this.weight = weight;
        }

        public int getLength() {
            return getText().length();
        }

        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Item item = (Item) o;
            return Objects.equals(code, item.code) && Objects.equals(text, item.text);
        }

        @Override public int hashCode() {
            return Objects.hash(code, text);
        }

        @Override public String toString() {
            return "Item{" +
                    code +
                    ", " + text +
                    ", weight=" + weight +
                    '}';
        }
    }
}
