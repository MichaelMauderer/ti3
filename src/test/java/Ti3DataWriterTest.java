import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStreamReader;

import static org.junit.Assert.assertEquals;

public class Ti3DataWriterTest {
    @Test
    public void writeTest() throws Exception {

        File ti3TestFile = new File(getClass().getResource("/testData.ti3").getFile());

        Ti3Data ti3Reference = Ti3Data.parseFrom(ti3TestFile);

        Ti3FileWriter testWriter = new Ti3FileWriter(ti3Reference);

        ByteArrayOutputStream testOutputStream = new ByteArrayOutputStream();
        testWriter.write(testOutputStream);

        byte[] testFileContent = testOutputStream.toByteArray();


        Ti3Data testFile = Ti3Data.parseFrom(new InputStreamReader(new ByteArrayInputStream(testFileContent)));
        assertEquals(ti3Reference, testFile);


    }

}