/*
 * The MIT License
 *
 * Copyright (c) 2016 Michael Mauderer
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

/**
 * A wrapper for accessing the data of a Ti3 file.
 * <p> The Ti3 file format is specified here: http://argyllcms.com/doc/ti3_format.html
 * <p> WARNING: This wrapper is incomplete and does not support the full
 * specification of Ti3 files.
 */
public class Ti3Data {

    private final HashMap<Keyword, String> properties = new HashMap<>();
    private String fileType;
    private DataTable<String> data;
    private String[] fields;

    public Ti3Data(String fileType, DataTable<String> data, Property[] properties) {
        this.fileType = fileType;

        this.fields = new String[data.columnLabels.size() + 1];

        fields[0] = "SAMPLE_ID";
        for (int i = 0; i < data.columnLabels.size(); ++i) {
            this.fields[i + 1] = data.columnLabels.get(i);
        }

        this.data = data;
        this.addProperties(Arrays.asList(properties));

    }

    private Ti3Data() {

    }

    public static Ti3Data parseFrom(Reader reader) throws IOException {

        Ti3Data resultFile = new Ti3Data();

        try (BufferedReader br = new BufferedReader(reader)) {
            String line;
            resultFile.fileType = br.readLine().trim();

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.charAt(0) == '#') {
                    continue;
                }
                // Split at whitespace, but not whitespace enclosed in double quotes
                // Compare http://stackoverflow.com/a/1757107/1175813
                String[] parts = line.split("\\s+(?=([^\"]*\"[^\"]*\")*[^\"]*$)", -1);
                if (parts.length == 0)
                    continue;

                String keyword = parts[0];

                if (!Keyword.isKeyword(keyword.trim())) {
                    continue;
                }
                if (keyword.equals("BEGIN_DATA_FORMAT")) {
                    resultFile.fields = parseDataFormat(br);
                    continue;
                }
                if (keyword.equals("BEGIN_DATA")) {
                    resultFile.data = DataTable.parseFromTi3Data(Arrays.copyOfRange(resultFile.fields, 1, resultFile.fields.length), br);
                    continue;
                }
                String value = parts[1].replaceAll("\"", "");
                resultFile.addProperty(keyword, value);

            }
            return resultFile;
        }
    }

    public static Ti3Data parseFrom(File inputFile) throws IOException {
        return parseFrom(new FileReader(inputFile));
    }

    private static String[] parseDataFormat(BufferedReader br) throws IOException {
        String[] parsedFields = br.readLine().split(" ");
        br.readLine();
        return parsedFields;
    }

    public static double CGATSNumericMeanSquareDifference(Ti3Data fileA, Ti3Data fileB) {
        String[] rows = fileA.getRowLabels();
        String[] columns = Arrays.copyOfRange(fileA.getFields(), 1, fileA.getFields().length);

        double squareDiffSum = 0;

        for (String row : rows) {
            for (String column : columns) {
                String contentA = fileA.get(row, column);
                String contentB = fileB.get(row, column);
                double diff = Double.parseDouble(contentA) - Double.parseDouble(contentB);
                squareDiffSum = diff * diff;
            }
        }

        return squareDiffSum / (rows.length * columns.length);
    }

    public String[] getFields() {
        return fields;
    }

    public String[] getRowLabels() {
        return data.rowLabels.toArray(new String[0]);
    }

    DataTable<String> getTableData() {
        return data;
    }

    public String getProperty(Keyword property) {
        return properties.get(property);
    }

    public String getProperty(String property) {
        return properties.get(Keyword.getKeyword(property));
    }

    private void addProperty(String keywordName, String value) {
        Keyword keyword = Keyword.getKeyword(keywordName);
        if (keyword == null) {
            throw new IllegalArgumentException("Illegal ti3 keyword: " + keywordName);
        }
        properties.put(keyword, value);
    }

    private void addProperty(Keyword keyword, String value) {
        properties.put(keyword, value);
    }

    private void addProperty(Property property) {
        addProperty(property.keyword, property.value);
    }

    private void addProperties(Collection<Property> properties) {
        for (Property property : properties) {
            addProperty(property);
        }
    }

    public Collection<Property> getProperties() {
        ArrayList<Property> result = new ArrayList<>();
        for (Keyword key : properties.keySet()) {
            result.add(new Property(key, properties.get(key)));
        }
        return result;
    }

    public String get(String row, String field) {
        return data.getDataItem(row, field);
    }

    public void writeFile(File outputFile) throws IOException {
        Ti3FileWriter writer = new Ti3FileWriter(this);
        writer.write(new FileOutputStream(outputFile));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Ti3Data ti3Data = (Ti3Data) o;

        if (!fileType.equals(ti3Data.fileType)) return false;
        if (!properties.equals(ti3Data.properties)) return false;
        if (data != null ? !data.equals(ti3Data.data) : ti3Data.data != null) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        return Arrays.equals(fields, ti3Data.fields);

    }

    @Override
    public int hashCode() {
        int result = fileType.hashCode();
        result = 31 * result + properties.hashCode();
        result = 31 * result + (data != null ? data.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(fields);
        return result;
    }

    public enum Keyword {
        DESCRIPTOR("DESCRIPTOR"),
        ORIGINATOR("ORIGINATOR"),
        CREATED("CREATED"),
        DEVICE_CLASS("DEVICE_CLASS"),
        TOTAL_INK_LIMIT("TOTAL_INK_LIMIT"),
        ILLUMINANT_WHITE_POINT_XYZ("ILLUMINANT_WHITE_POINT_XYZ"),
        LUMINANCE_XYZ_CDM2("LUMINANCE_XYZ_CDM2"),
        NORMALIZED_TO_Y_100("NORMALIZED_TO_Y_100"),
        TARGET_INSTRUMENT("TARGET_INSTRUMENT"),
        INSTRUMENT_TYPE_SPECTRAL("INSTRUMENT_TYPE_SPECTRAL"),
        DISPLAY_TYPE_REFRESH("DISPLAY_TYPE_REFRESH"),
        SINGLE_DIM_STEPS("SINGLE_DIM_STEPS"),
        COLOR_REP("COLOR_REP"),
        SPECTRAL_BANDS("SPECTRAL_BANDS"),
        SPECTRAL_START_NM("SPECTRAL_START_NM"),
        SPECTRAL_END_NM("SPECTRAL_END_NM"),
        NUMBER_OF_FIELDS("NUMBER_OF_FIELDS"),
        BEGIN_DATA_FORMAT("BEGIN_DATA_FORMAT"),
        END_DATA_FORMAT("END_DATA_FORMAT"),
        NUMBER_OF_SETS("NUMBER_OF_SETS"),
        BEGIN_DATA("BEGIN_DATA"),
        END_DATA("END_DATA"),;

        private final String text;

        Keyword(final String text) {
            this.text = text;
        }

        public static boolean isKeyword(String s) {
            for (Keyword k : Keyword.values()) {
                if (k.text.equals(s)) {
                    return true;
                }
            }
            return false;
        }

        public static Keyword getKeyword(String s) {
            for (Keyword k : Keyword.values()) {
                if (k.text.equals(s)) {
                    return k;
                }
            }
            return null;
        }

        /* (non-Javadoc)
         * @see java.lang.Enum#toString()
         */
        @Override
        public String toString() {
            return text;
        }
    }

    public static class Property {
        final Keyword keyword;
        final String value;

        public Property(Keyword keyword, String value) {
            this.keyword = keyword;
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Property property = (Property) o;

            return (keyword != null ? keyword.equals(property.keyword) : property.keyword == null) && (value != null ? value.equals(property.value) : property.value == null);

        }

        @Override
        public int hashCode() {
            int result = keyword != null ? keyword.hashCode() : 0;
            result = 31 * result + (value != null ? value.hashCode() : 0);
            return result;
        }
    }
}
