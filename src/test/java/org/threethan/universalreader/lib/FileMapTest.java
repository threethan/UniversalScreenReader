package org.threethan.universalreader.lib;

import org.junit.jupiter.api.*;

import java.io.File;
import java.io.Serializable;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class FileMapTest {
    FileMap<Serializable, Serializable> testFileMap;
    File testFile;
    @BeforeEach
    void setUp() {
        testFile = new File("./fileMapTestFile");
        testFileMap = new FileMap<>(testFile);
    }

    @AfterEach
    void tearDown() {
        if (testFile.exists()) {
            //noinspection ResultOfMethodCallIgnored
            testFile.delete();
        }
    }

    @Test
    @DisplayName("Test that changes remain on reload")
    void testPersistenceOnReload() {
        final String KEY = "TestK";
        final String VAL = "TestV";
        testFileMap.put(KEY, VAL);
        testFileMap.reloadFromFile();
        //noinspection deprecation
        assertEquals(testFileMap.get(KEY), VAL);
    }

    @Test
    @DisplayName("Test that changes are propagated (only) on reload")
    void testPropagatedOnReload() {
        final String KEY = "TestK";
        final String VAL1 = "TestV1";
        final String VAL2 = "TestV2";
        testFileMap.put(KEY, VAL1);
        FileMap<Serializable, Serializable> alternateReference = new FileMap<>(testFile);
        alternateReference.put(KEY, VAL2);
        //noinspection deprecation
        assertEquals(testFileMap.get(KEY), VAL1);
        //noinspection deprecation
        assertEquals(alternateReference.get(KEY), VAL2);
        //noinspection deprecation
        assertNotEquals(testFileMap.get(KEY), VAL2);
        testFileMap.reloadFromFile();
        //noinspection deprecation
        assertEquals(testFileMap.get(KEY), VAL2);
    }

    @RepeatedTest(4)
    @DisplayName("Test that .size() works as expected through reload and duplicate keys")
    void size() {
        final int i = Math.abs(new Random().nextInt()) % 50;
        for (int j = 0; j < i; j++) testFileMap.put(j, "v");
        assertEquals(i, testFileMap.size());
        testFileMap.reloadFromFile();
        for (int j = 0; j < i; j++) testFileMap.put(j, "v"); // Try writing duplicates
        assertEquals(i, testFileMap.size());
        for (int j = 0; j < i; j++) testFileMap.put(j+i, "v");
        assertEquals(i*2, testFileMap.size());
    }

    @Test
    @DisplayName("Test .isEmpty() through remove()")
    void isEmpty() {
        assertTrue(testFileMap.isEmpty());
        testFileMap.put("v1", "v2");
        testFileMap.put("vA", "v3");
        //noinspection ConstantValue
        assertFalse(testFileMap.isEmpty());
        testFileMap.reloadFromFile();
        assertFalse(testFileMap.isEmpty());
        testFileMap.remove("v1");
        testFileMap.remove("vA");
        assertTrue(testFileMap.isEmpty());
        testFileMap.reloadFromFile();
        assertTrue(testFileMap.isEmpty());
    }

    @Test
    @DisplayName("Test .isEmpty() through clear()")
    void isEmptyClear() {
        assertTrue(testFileMap.isEmpty());
        testFileMap.put("v1", "v2");
        testFileMap.put("vA", "v3");
        //noinspection ConstantValue
        assertFalse(testFileMap.isEmpty());
        testFileMap.reloadFromFile();
        assertFalse(testFileMap.isEmpty());
        testFileMap.clear();
        //noinspection ConstantValue
        assertTrue(testFileMap.isEmpty());
        testFileMap.reloadFromFile();
        assertTrue(testFileMap.isEmpty());
    }
}