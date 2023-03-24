package top.someapp.fimesdk.dict;

import com.github.davidmoten.bigsorter.Serializer;
import com.github.davidmoten.bigsorter.Sorter;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import top.someapp.fimesdk.engine.Converter;
import top.someapp.fimesdk.utils.FileStorage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;

/**
 * @author zwz
 * Created on 2023-03-24
 */
class CsvDict {

    private static final CSVFormat format = CSVFormat.Builder.create()
                                                             .setAllowMissingColumnNames(true)
                                                             .setCommentMarker('#')
                                                             .setDelimiter('\t')
                                                             .setQuote('"')
                                                             .setIgnoreEmptyLines(true)
                                                             .build();
    private static final Serializer<CSVRecord> serializer = Serializer.csv(
            format, StandardCharsets.UTF_8);
    ;
    private static final Comparator<CSVRecord> comparator = (o1, o2) -> {
        String code1 = o1.get(1);
        String code2 = o2.get(1);
        return code1.compareTo(code2);
    };

    static void convert(File source, Converter converter, File target) throws IOException {
        if (!converter.hasRule()) {
            FileStorage.copyToFile(new FileInputStream(source), target);
            return;
        }

        Writer writer = new FileWriter(target);
        BufferedReader reader = new BufferedReader(new FileReader(source));
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("#") || line.isEmpty()) continue;
            String[] segments = line.split("[\t]");
            String text = segments[0];
            String oldCode = segments[1];
            int weight = segments.length > 2 ? Integer.decode(segments[2]) : 0;
            StringBuilder code = new StringBuilder();
            for (String seg : oldCode.split("[ ]")) {
                code.append(" ")
                    .append(converter.convert(seg));
            }
            writer.write(text);
            writer.write('\t');
            writer.write(code.substring(1));
            writer.write(String.valueOf(weight));
            writer.write('\n');
            writer.flush();
        }
        writer.flush();
        writer.close();
        reader.close();
        //noinspection ResultOfMethodCallIgnored
        source.delete();
    }

    static void sort(File source, File target) {
        Sorter.serializer(serializer)
              .comparator(comparator)
              .input(source)
              .filter(record -> record.size() >= 2)
              .output(target)
              // .maxItemsPerFile(10000) // default is 10 w
              .sort();
    }
}
