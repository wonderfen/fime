package top.someapp.fimesdk.dict;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import com.caucho.hessian.io.AbstractHessianInput;
import com.caucho.hessian.io.AbstractHessianOutput;
import it.unimi.dsi.fastutil.objects.Object2ObjectRBTreeMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayPriorityQueue;
import org.trie4j.patricia.MapPatriciaTrie;
import org.trie4j.patricia.MapPatriciaTrieNode;
import org.trie4j.util.IntArray;
import top.someapp.fimesdk.engine.Converter;
import top.someapp.fimesdk.utils.Serializes;
import top.someapp.fimesdk.utils.Strings;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

    private static H2 h2;   // 用户词词典
    private String name;  // 词典名
    private int size;           // 包含的词条数
    private boolean sealed;     // 词典是否构建完成
    private MapPatriciaTrie<IntArray> mapTrie;        // 词条树
    private transient Map<String, List<Item>> itemMap;  // code -> Item[]
    private transient List<Item> items;

    public Dict(String name) {
        this.name = name;
        mapTrie = new MapPatriciaTrie<>();
    }

    public static Dict loadFromCompiled(InputStream ins) throws IOException {
        AbstractHessianInput input = Serializes.createInput(ins);
        String head = input.readString();
        if (!head.startsWith("FimeDict:")) throw new IOException("Invalid input!");
        String name = head.substring(9)
                          .split("[/]")[0];
        Dict dict = new Dict(name);
        int size = input.readInt();
        for (int i = 1; i <= size; i++) {
            String text = input.readString();
            String code = input.readString();
            int weight = input.readInt();
            dict.put(new Item(text, code, weight));
        }
        dict.build();
        return dict;
    }

    static int compareItems(Item o1, Item o2) {
        String code1 = o1.getCode();
        String code2 = o2.getCode();
        if (code1.equals(code2)) { // 编码相同时，weight 优先
            if (o1.getWeight() >= o2.getWeight()) return -1;
            return o2.getWeight() - o1.getWeight();
        }
        if (o1.getLength() == o2.getLength()) { // 编码不同，词长相同， weight 优先
            if (o1.getWeight() >= o2.getWeight()) return -1;
            return o2.getWeight() - o1.getWeight();
        }
        // 编码不同，词长不同，编码短的优先
        return code1.length() - code2.length();
    }

    public String getName() {
        return name;
    }

    public int getSize() {
        return size;
    }

    public boolean loadFromCsv(File csvFile) throws IOException {
        return loadFromCsv(new FileInputStream(csvFile), new Converter());
    }

    public boolean loadFromCsv(File csvFile, @NonNull Converter converter) throws IOException {
        return loadFromCsv(new FileInputStream(csvFile), converter);
    }

    public boolean loadFromCsv(InputStream ins, @NonNull Converter converter) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(ins));
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("#")) continue;
            String[] parts = line.split("\t");
            String text = parts[0];
            String code = converter.convert(parts[1]);
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
        AbstractHessianOutput output = Serializes.createOutput(out);
        output.writeString(fileHead());
        output.writeInt(items.size());
        for (Item item : items) {
            output.writeString(item.getText());
            output.writeString(item.getCode());
            output.writeInt(item.getWeight());
        }
        output.flush();
        output.close();
        out.close();
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
        // 优先查询用户词
        initH2();
        List<Item> userItems = h2.query(prefix, limit);
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
                    if (item.getLength() == wordLength
                            && item.getCode()
                                   .startsWith(prefix)) {
                        queue.enqueue(item);
                    }
                }
            }
        }
        boolean hit = false;
        int count = 0;
        Iterator<Item> it = userItems.iterator();
        while (it.hasNext() && count < limit) {
            if (!hit) hit = true;
            result.add(it.next());
            it.remove();
            count++;
        }
        while (!queue.isEmpty() && count < limit) {
            if (!hit) hit = true;
            result.add(queue.dequeue());
            count++;
        }
        queue.clear();
        return hit;
    }

    public boolean searchPrefix(@NonNull String prefix, final int extendCodeLength,
            @NonNull List<Item> result, int limit,
            Comparator<Item> comparator) {
        ObjectArrayPriorityQueue<Item> queue = new ObjectArrayPriorityQueue<>(limit * 2,
                                                                              comparator);
        // 前缀预测匹配
        final int maxCodeLength = prefix.length() + extendCodeLength;
        for (Map.Entry<String, IntArray> entry : mapTrie.predictiveSearchEntries(
                prefix)) {
            IntArray value = entry.getValue();
            final int index = value.get(0);
            final int size = value.get(1);
            for (int i = 0; i < size; i++) {
                Item item = items.get(index + i);
                if (item.getCode()
                        .length() <= maxCodeLength) {
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

    public void recordUserWord(Item item) {
        initH2();
        h2.insertOrUpdate(item);
    }

    public void close() {
        if (h2 != null) h2.stop();
    }

    @NonNull @Override public String toString() {
        return "Dict{"
                + name +
                ", size=" + size +
                ", sealed=" + sealed +
                '}';
    }

    @Override public int compare(Item o1, Item o2) {
        return Dict.compareItems(o1, o2);
    }

    @SuppressWarnings("all")
    protected Dict put(@NonNull Item item) {
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

    private void initH2() {
        if (h2 == null) {
            h2 = new H2(name);
            h2.start();
            return;
        }
        if (!name.equals(h2.getId())) {
            h2.stop();
            h2 = new H2(name);
            h2.start();
        }
    }

    private String fileHead() {
        return "FimeDict:" + getName() + "/" + getSize() + "\n";
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
    public static class Item implements Serializable, Comparable<Item> {

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

        @NonNull @Override public String toString() {
            return "Item{" +
                    code +
                    ", " + text +
                    ", weight=" + weight +
                    '}';
        }

        @Override public int compareTo(Item o) {
            return Dict.compareItems(this, o);
        }
    }
}
