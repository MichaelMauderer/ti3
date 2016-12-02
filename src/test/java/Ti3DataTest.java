import java.io.File;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class Ti3DataTest {

    private Ti3Data testFile;

    @org.junit.Before
    public void setUp() throws Exception {
        testFile = Ti3Data.parseFrom(new File(getClass().getResource("/testData.ti3").getFile()));
    }

    @org.junit.Test
    public void testBasicReading() throws Exception {

        assertEquals(
                "Originator does not match expected value.",
                "Argyll target",
                testFile.getProperty("ORIGINATOR")
        );
        assertEquals(
                "Descriptor does not match expected value.",
                "Argyll Calibration Target chart information 3",
                testFile.getProperty("DESCRIPTOR")
        );
        assertEquals(
                "Does not match expected value.",
                "Thu Nov 03 17:55:21 2016",
                testFile.getProperty("CREATED")
        );
        assertEquals(
                "Does not match expected value.",
                "INPUT",
                testFile.getProperty("DEVICE_CLASS")
        );
        assertEquals(
                "Does not match expected value.",
                "XYZ_RGB",
                testFile.getProperty("COLOR_REP")
        );
        assertEquals(
                "Does not match expected value.",
                "10",
                testFile.getProperty("NUMBER_OF_FIELDS")
        );
        assertArrayEquals(new String[]{
                        "SAMPLE_ID",
                        "XYZ_X",
                        "XYZ_Y",
                        "XYZ_Z",
                        "RGB_R",
                        "RGB_G",
                        "RGB_B",
                        "STDEV_R",
                        "STDEV_G",
                        "STDEV_B",
                },
                testFile.getFields());
    }

    @org.junit.Test
    public void testGet() throws Exception {
        assertEquals(
                "Data does not match expected value for \"A03\", \"RGB_G\" .",
                "41.78148",
                testFile.get("A06", "XYZ_Y")
        );
        assertEquals(
                "Data does not match expected value for (\"A03\", \"RGB_R\") .",
                "8.586318",
                testFile.get("A03", "RGB_R")
        );
        assertEquals(
                "Data does not match expected value for \"D06\", \"STDEV_B\".",
                "0.211168",
                testFile.get("D06", "STDEV_B")
        );


    }


}