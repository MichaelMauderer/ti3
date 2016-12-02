import java.io.*;

class Ti3FileWriter {

    private final Ti3Data fileToWrite;

    Ti3FileWriter(Ti3Data fileToWrite) {
        this.fileToWrite = fileToWrite;
    }

    public void write(OutputStream outFile) throws IOException {
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(outFile, "utf-8"))) {
            writeKeywords(writer);
            writer.write("\n");
            writeFields(writer);
            writer.write("\n");
            writeData(writer);
        }
    }

    private void writeKeyValue(Writer writer, String key, String value) throws IOException {
        writer.write(key);
        writer.write(" ");
        writer.write("\"");
        writer.write(value);
        writer.write("\"");
        writer.write("\n");
    }

    private void writeFields(Writer writer) throws IOException {
        String[] fields = fileToWrite.getFields();

        writer.write("NUMBER_OF_FIELDS ");
        writer.write(Integer.toString(fields.length));
        writer.write("\n");

        writer.write("BEGIN_DATA_FORMAT\n");
        for (String field : fields) {
            writer.write(field);
            writer.write(" ");
        }
        writer.write("\n");
        writer.write("END_DATA_FORMAT\n");

    }

    private void writeKeywords(Writer writer) throws IOException {
        writer.write("CTI3\n");
        writer.write("\n");

        for (Ti3Data.Property property : fileToWrite.getProperties()) {
            writeKeyValue(writer, property.keyword.toString(), property.value);
        }
    }

    private void writeData(Writer writer) throws IOException {
        DataTable<String> table = fileToWrite.getTableData();
        String[] rowLabels = fileToWrite.getRowLabels();

        writer.write("NUMBER_OF_SETS ");
        writer.write(Integer.toString(table.numberOfRows()));
        writer.write("\n");

        writer.write("BEGIN_DATA\n");
        for (int row = 0; row < table.numberOfRows(); ++row) {
            writer.write(rowLabels[row]);
            for (int column = 0; column < (table.numberOfColumns()); ++column) {
                writer.write(" ");
                writer.write(table.getDataItem(row, column));
            }
            writer.write("\n");
        }
        writer.write("END_DATA\n");

    }

}
