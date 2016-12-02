import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

class DataTable<DataType> {

    final ArrayList<String> rowLabels;
    final ArrayList<String> columnLabels;
    private DataType[][] data;

    public DataTable(String[] rowLabels, String[] columnLabels, DataType[][] data) {

        this.data = data;
        this.rowLabels = new ArrayList<>(Arrays.asList(rowLabels));
        this.columnLabels = new ArrayList<>(Arrays.asList(columnLabels));

        assert (rowLabels.length > 0);
        assert (columnLabels.length > 0);
        assert (data.length == rowLabels.length);
        assert (data[0].length == (columnLabels.length));
    }

    static DataTable<String> parseFromTi3Data(String[] columnLabels, BufferedReader br) throws IOException {
        String line;
        ArrayList<String[]> lines = new ArrayList<>();
        ArrayList<String> rowLabels = new ArrayList<>();

        while ((line = br.readLine()) != null) {
            if (line.equals("END_DATA")) {
                break;
            }
            String[] lineParts = line.split(" +");
            rowLabels.add(lineParts[0]);
            lines.add(Arrays.copyOfRange(lineParts, 1, lineParts.length));

        }

        String[][] parsedData = lines.toArray(new String[lines.size()][]);
        return new DataTable<>(
                rowLabels.toArray(new String[]{}),
                columnLabels,
                parsedData
        );
    }

    public DataType getDataItem(String row, String field) {
        int rowIndex = rowLabels.indexOf(row);
        int columnIndex = columnLabels.indexOf(field);
        return getDataItem(rowIndex, columnIndex);
    }

    public DataType getDataItem(int rowIndex, int columnIndex) {
        return data[rowIndex][columnIndex];
    }

    public int numberOfRows() {
        return rowLabels.size();
    }

    public int numberOfColumns() {
        return columnLabels.size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DataTable<?> dataTable = (DataTable<?>) o;

        return (rowLabels != null ? rowLabels.equals(dataTable.rowLabels) : dataTable.rowLabels == null) && (columnLabels != null ? columnLabels.equals(dataTable.columnLabels) : dataTable.columnLabels == null) && Arrays.deepEquals(data, dataTable.data);

    }

    @Override
    public int hashCode() {
        int result = rowLabels != null ? rowLabels.hashCode() : 0;
        result = 31 * result + (columnLabels != null ? columnLabels.hashCode() : 0);
        result = 31 * result + Arrays.deepHashCode(data);
        return result;
    }

    public void addColumn(String label, DataType[] column) {
        columnLabels.add(label);
        for (int i = 0; i < data.length; ++i) {
            DataType[] oldRow = data[i];
            data[i] = Arrays.copyOf(oldRow, oldRow.length + 1);
            data[i][oldRow.length] = column[i];
        }
    }

    public void addRow(String label, DataType[] row) {
        rowLabels.add(label);
        data = Arrays.copyOf(data, data.length + 1);
        data[data.length - 1] = row;
    }
}
