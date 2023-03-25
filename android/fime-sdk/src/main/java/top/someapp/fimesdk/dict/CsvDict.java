package top.someapp.fimesdk.dict;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.SequenceWriter;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import top.someapp.fimesdk.engine.Converter;
import top.someapp.fimesdk.utils.Logs;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;

/**
 * @author zwz
 * Created on 2023-03-24
 */
class CsvDict {

    private static final CsvSchema schema = CsvSchema.builder()
                                                     .addColumn("text")
                                                     .addColumn("code")
                                                     .addColumn("weight",
                                                                CsvSchema.ColumnType.NUMBER)
                                                     .setAllowComments(true)
                                                     .setColumnSeparator('\t')
                                                     .setQuoteChar('"')
                                                     .disableQuoteChar()
                                                     .setEscapeChar('\\')
                                                     .build();
    private static final CsvMapper csvMapper = new CsvMapper()
            .enable(CsvParser.Feature.WRAP_AS_ARRAY)
            .enable(CsvParser.Feature.ALLOW_COMMENTS)
            .enable(CsvParser.Feature.SKIP_EMPTY_LINES);
    private static final Comparator<String> comparator = (s1, s2) -> {
        String code1 = s1.split("[\t]")[1];
        String code2 = s2.split("[\t]")[1];
        return code1.compareTo(code2);
    };

    static void convert(File source, Converter converter, File target) throws IOException {
        Logs.d("start convert %s => %s", source.getName(), target.getName());
        ObjectReader reader = csvMapper.readerWithTypedSchemaFor(Dict.Item.class)
                                       .with(schema);
        MappingIterator<Dict.Item> it = reader.readValues(source);
        SequenceWriter writer = csvMapper.writerWithTypedSchemaFor(Dict.Item.class)
                                         .with(schema)
                                         .writeValues(target);
        if (converter.hasRule()) {
            while (it.hasNext()) {
                Dict.Item next = it.next();
                Logs.d(next.toString());
            }
        }
        else {
            while (it.hasNext()) {
                Dict.Item next = it.next();
                writer.write(next);
                writer.flush();
            }
        }
        writer.close();
        //noinspection ResultOfMethodCallIgnored
        source.delete();
        Logs.d("finish convert %s => %s", source.getName(), target.getName());
    }

    static void sort(File source, File target) {
        // split to small files.
        Logs.d("start sort %s => %s", source.getName(), target.getName());
        // int size = 10240;
        // Set<String> records = new TreeSet<>(comparator);
        // int id = 1000;
        // int count = 0;
        // try (BufferedReader reader = new BufferedReader(new FileReader(source))) {
        //     List<File> readerList = new ArrayList<>();
        //     String line;
        //     while ((line = reader.readLine()) != null) {
        //         if (line.isEmpty()) break;
        //         records.add(line);
        //         count++;
        //         if (count == size) {
        //             count = 0;
        //             id++;
        //             readerList.add((writeTempFile(records, id)));
        //             records.clear();
        //         }
        //     }
        //     if (!records.isEmpty()) {
        //         id++;
        //         readerList.add((writeTempFile(records, id)));
        //         records.clear();
        //     }
        //     merge(readerList);
        // }
        // catch (IOException e) {
        //     e.printStackTrace();
        // }
        split();
        sortRange();
        merge();
        Logs.d("finish sort %s => %s", source.getName(), target.getName());
    }

    static void split() {
    }

    static void sortRange() {
    }

    static void merge() {
    }

    // static File writeTempFile(Collection<String> content, int id) throws IOException {
    //     File dir = FimeContext.getInstance()
    //                           .getCacheDir();
    //     File file = new File(dir, id + ".csv");
    //     FileWriter writer = new FileWriter(file);
    //     for (String record : content) {
    //         writer.write(record);
    //         writer.write("\n");
    //         writer.flush();
    //     }
    //     writer.close();
    //     return file;
    // }

    // static void merge(List<File> files) {
    //     if (files.size() < 2) return;
    //
    //     List<File> groups = new ArrayList<>(files.size() / 2);
    //     Set<String> records = new TreeSet<>(comparator);
    //     String line;
    //     for (int i = 0, size = files.size(); i < files.size(); ) {
    //         File first = files.get(i);
    //         File second = i + 1 < size ? files.get(i + 1) : null;
    //         File target = new File(first.getParentFile(), "g" + first.getName());
    //         try (BufferedReader reader1 = new BufferedReader(new FileReader(first));
    //              BufferedReader reader2 = second == null ? null : new BufferedReader(
    //                      new FileReader(second));
    //              FileWriter writer = new FileWriter(target)) {
    //             while ((line = reader1.readLine()) != null) {
    //                 records.add(line);
    //             }
    //             if (second != null) {
    //                 while ((line = reader2.readLine()) != null) {
    //                     records.add(line);
    //                 }
    //             }
    //             for (String record : records) {
    //                 writer.write(record);
    //                 writer.write("\n");
    //                 writer.flush();
    //             }
    //             records.clear();
    //         }
    //         catch (IOException e) {
    //             e.printStackTrace();
    //         }
    //         first.delete();
    //         if (second != null) second.delete();
    //         i += 2;
    //         groups.add(target);
    //     }
    //     files.clear();
    //     merge(groups);
    // }
}
