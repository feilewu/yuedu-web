package me.ag2s.base;

import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;

@SuppressWarnings("unused")
public final class PfdHelper {

    private static final byte[] readBuffer = new byte[8];

    public static void seek(RandomAccessFile raf, long pos) throws IOException {
        raf.seek(pos);
    }

    public static long getFilePointer(RandomAccessFile raf) throws IOException {
        return raf.getFilePointer();
    }

    public static long length(RandomAccessFile raf) throws IOException {
        return raf.length();
    }

    public static int read(RandomAccessFile raf) throws IOException {
        return raf.read();
    }

    public static int read(RandomAccessFile raf, byte[] b, int off, int len) throws IOException {
        return raf.read(b, off, len);
    }

    public static int read(RandomAccessFile raf, byte[] b) throws IOException {
        return raf.read(b);
    }

    public static void readFully(RandomAccessFile raf, byte[] b) throws IOException {
        raf.readFully(b);
    }

    public static void readFully(RandomAccessFile raf, byte[] b, int off, int len) throws IOException {
        raf.readFully(b, off, len);
    }

    public static int skipBytes(RandomAccessFile raf, int n) throws IOException {
        return raf.skipBytes(n);
    }
}
