/**
 * Test persisting to local disk
 */
package name.chen.dave.impl;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


public class IntArrayPersistenceTest {
    private IntArrayPersistence intArrayPersistence = null;
    private File tempFile = null;

    @Before
    public void setUp() throws IOException {
        intArrayPersistence = new IntArrayPersistence();
        tempFile = File.createTempFile("bdayhistogram", "test");
        tempFile.deleteOnExit();
    }

    @Test
    public void testWriteAndReloadFromDisk() throws IOException {
        int[] toDisk = {0,1,2,3,4,5};
        intArrayPersistence.toLocalDisk(toDisk, tempFile.getAbsolutePath());
        int[] fromDisk = intArrayPersistence.fromLocalDisk(tempFile.getAbsolutePath());
        Assert.assertArrayEquals(toDisk, fromDisk);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReadBadFile() throws IOException {
        FileOutputStream fw = new FileOutputStream(tempFile);
        byte[] b = {1,2,3};
        fw.write(b);
        intArrayPersistence.fromLocalDisk(tempFile.getAbsolutePath());
    }
}
