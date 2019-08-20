package name.chen.dave.impl;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class IntArrayPersistence {

    private static final int BYTES_IN_INT = 4;

    public void toLocalDisk(int[] intArray, String outputFile) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(4 * intArray.length);
        for (int i : intArray) {
            buffer.putInt(i);
        }
        buffer.rewind();
        FileChannel fc = null;
        try {
            fc = new FileOutputStream(outputFile).getChannel();
            fc.write(buffer);
        } finally {
            ensureClose(fc);
        }
    }

    public int[] fromLocalDisk(String inputFile) throws IOException {
        int bytesInFile = (int)(new File(inputFile).length());
        if (bytesInFile % BYTES_IN_INT != 0) {
            throw new IllegalArgumentException("Invalid input file " + inputFile + " : Not series of integers." +
                    " Possible file corruption.");
        }
        int lengthOfIntArray = bytesInFile / BYTES_IN_INT;
        ByteBuffer buffer = null;
        FileChannel fc = null;
        try {
            fc = new FileInputStream(inputFile).getChannel();
            buffer = ByteBuffer.allocate(bytesInFile);
            fc.read(buffer);
        } finally {
            ensureClose(fc);
        }
        buffer.rewind();
        int[] newInts = new int[lengthOfIntArray];
        for (int i=0; i<lengthOfIntArray; i++) {
            newInts[i] = buffer.getInt();
        }
        return newInts;
    }

    private void ensureClose(FileChannel fc) {
        try {
            if (fc != null) {
                fc.close();
            }
        } catch (IOException e) {
            // do nothing
        }
    }
}
