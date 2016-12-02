import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DataTableTest {
    @Test
    public void addColumnTest() throws Exception {
        DataTable<String> testTable = new DataTable<>(
                new String[]{"RowA"},
                new String[]{"ColA"},
                new String[][]{{"AA"}}
        );

        testTable.addColumn("ColB", new String[]{"AB"});
        assertEquals("AA", testTable.getDataItem("RowA", "ColA"));
        assertEquals("AB", testTable.getDataItem("RowA", "ColB"));


    }

    @Test
    public void addRowTest() throws Exception {
        DataTable<String> testTable = new DataTable<>(
                new String[]{"RowA"},
                new String[]{"ColA"},
                new String[][]{{"AA"}}
        );

        testTable.addRow("RowB", new String[]{"BA"});
        assertEquals("AA", testTable.getDataItem("RowA", "ColA"));
        assertEquals("BA", testTable.getDataItem("RowB", "ColA"));


    }

}