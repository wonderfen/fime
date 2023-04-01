package top.someapp.fimesdk.dict;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.SequenceWriter;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.sort.DataReader;
import com.fasterxml.sort.DataReaderFactory;
import com.fasterxml.sort.DataWriter;
import com.fasterxml.sort.DataWriterFactory;
import com.fasterxml.sort.SortConfig;
import com.fasterxml.sort.Sorter;
import com.fasterxml.sort.TempFileProvider;
import top.someapp.fimesdk.engine.Converter;
import top.someapp.fimesdk.utils.FileStorage;
import top.someapp.fimesdk.utils.Logs;
import top.someapp.fimesdk.utils.Strings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
    private static final Comparator<Dict.Item> itemComparator = (o1, o2) -> {
        if (o1.getCode()
              .equals(o2.getCode())) {
            return o2.getWeight() - o1.getWeight();
        }
        return o1.getCode()
                 .compareTo(o2.getCode());
    };

    private final File source;
    private final File target;

    CsvDict(File source, File target) {
        this.source = source;
        this.target = target;
    }

    static MappingIterator<Dict.Item> load(File csv) throws IOException {
        ObjectReader reader = csvMapper.readerWithTypedSchemaFor(Dict.Item.class)
                                       .with(schema);
        return reader.readValues(csv);
    }

    void normalize(File workDir) throws IOException {
        normalize(workDir, null, '\0');
    }

    void normalize(File workDir, Converter converter, char delimiter) throws IOException {
        Logs.d("start convert %s => %s", source.getName(), target.getName());
        int[] start = { 10000 };
        TempFileProvider tempFileProvider = () -> {
            start[0]++;
            return new File(workDir, start[0] + ".csv");
        };
        SortConfig config = new SortConfig().withTempFileProvider(tempFileProvider);
        Sorter<Dict.Item> sorter = new Sorter<>(config, new ReaderFactory(converter, delimiter),
                                                new WriterFactory(),
                                                itemComparator);
        sorter.sort(new FileInputStream(source), new FileOutputStream(target));
        FileStorage.cleanDir(workDir);
        Logs.d("finish convert %s => %s", source.getName(), target.getName());
    }

    static class ReaderFactory extends DataReaderFactory<Dict.Item> {

        private final Converter converter;
        private final char delimiter;
        private final String splitReg;
        private boolean converted;

        ReaderFactory(Converter converter, char delimiter) {
            this.converter = converter;
            this.delimiter = delimiter;
            this.splitReg = delimiter > 0 ? Strings.simpleFormat("[\\u%04x]",
                                                                 (int) delimiter) : null;
        }

        @Override public DataReader<Dict.Item> constructReader(InputStream in) throws IOException {
            ObjectReader reader = csvMapper.readerWithTypedSchemaFor(Dict.Item.class)
                                           .with(schema);
            MappingIterator<Dict.Item> it = reader.readValues(in);
            if (!converted && converter != null && converter.hasRule()) {
                converted = true;   // 有隐患!!
                return new DataReader<Dict.Item>() {
                    @Override public Dict.Item readNext() {
                        if (it.hasNext()) {
                            Dict.Item next = it.next();
                            if (splitReg == null) {
                                next.setCode(converter.convert(next.getCode()));
                            }
                            else {
                                String[] segments = next.getCode()
                                                        .split(splitReg);
                                StringBuilder code = new StringBuilder();
                                for (String seg : segments) {
                                    code.append(delimiter)
                                        .append(converter.convert(seg));
                                }
                                next.setCode(code.substring(1));
                            }
                            return next;
                        }
                        return null;
                    }

                    @Override public int estimateSizeInBytes(Dict.Item item) {
                        return it.hasNext() ? 1024 : 0;
                    }

                    @Override public void close() {
                    }
                };
            }
            return new DataReader<Dict.Item>() {
                @Override public Dict.Item readNext() {
                    return it.hasNext() ? it.next() : null;
                }

                @Override public int estimateSizeInBytes(Dict.Item item) {
                    return it.hasNext() ? 1024 : 0;
                }

                @Override public void close() {
                }
            };
        }
    }

    static class WriterFactory extends DataWriterFactory<Dict.Item> {

        @Override
        public DataWriter<Dict.Item> constructWriter(OutputStream out) throws IOException {
            SequenceWriter writer = csvMapper.writerWithTypedSchemaFor(
                    Dict.Item.class)
                                             .with(schema)
                                             .writeValues(out);
            return new DataWriter<Dict.Item>() {
                @Override public void writeEntry(Dict.Item item) throws IOException {
                    writer.write(item);
                }

                @Override public void close() throws IOException {
                    writer.close();
                }
            };
        }
    }
}
