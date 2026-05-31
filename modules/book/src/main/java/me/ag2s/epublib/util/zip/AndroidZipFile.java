package me.ag2s.epublib.util.zip;

import static me.ag2s.base.PfdHelper.seek;

import java.io.BufferedInputStream;
import java.io.DataInput;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;
import java.util.zip.ZipOutputStream;

import me.ag2s.base.PfdHelper;

public class AndroidZipFile implements ZipConstants {

    public static final int OPEN_READ = 0x1;
    public static final int OPEN_DELETE = 0x4;

    private final String name;
    private final RandomAccessFile raf;

    private HashMap<String, AndroidZipEntry> entries;
    private boolean closed = false;

    public AndroidZipFile(RandomAccessFile raf, String name) throws ZipException, IOException {
        this.raf = raf;
        this.name = name;
    }

    public AndroidZipFile(File file) throws ZipException, IOException {
        this.raf = new RandomAccessFile(file, "r");
        this.name = file.getPath();
    }

    private int readLeShort(DataInput di, byte[] b) throws IOException {
        di.readFully(b, 0, 2);
        return (b[0] & 0xff) | (b[1] & 0xff) << 8;
    }

    private int readLeInt(DataInput di, byte[] b) throws IOException {
        di.readFully(b, 0, 4);
        return ((b[0] & 0xff) | (b[1] & 0xff) << 8)
                | ((b[2] & 0xff) | (b[3] & 0xff) << 8) << 16;
    }

    private int readLeShort(byte[] b, int off) {
        return (b[off] & 0xff) | (b[off + 1] & 0xff) << 8;
    }

    private int readLeInt(byte[] b, int off) {
        return ((b[off] & 0xff) | (b[off + 1] & 0xff) << 8)
                | ((b[off + 2] & 0xff) | (b[off + 3] & 0xff) << 8) << 16;
    }

    private void readEntries() throws ZipException, IOException {
        long pos = PfdHelper.length(raf) - ENDHDR;
        byte[] ebs = new byte[CENHDR];

        do {
            if (pos < 0)
                throw new ZipException("central directory not found, probably not a zip file: " + name);
            seek(raf, pos--);
        }
        while (readLeInt(raf, ebs) != ENDSIG);

        if (PfdHelper.skipBytes(raf, ENDTOT - ENDNRD) != ENDTOT - ENDNRD)
            throw new EOFException(name);
        int count = readLeShort(raf, ebs);
        if (PfdHelper.skipBytes(raf, ENDOFF - ENDSIZ) != ENDOFF - ENDSIZ)
            throw new EOFException(name);
        int centralOffset = readLeInt(raf, ebs);

        entries = new HashMap<>(count + count / 2);
        seek(raf, centralOffset);

        byte[] buffer = new byte[16];
        for (int i = 0; i < count; i++) {
            PfdHelper.readFully(raf, ebs);
            if (readLeInt(ebs, 0) != CENSIG)
                throw new ZipException("Wrong Central Directory signature: " + name);

            int method = readLeShort(ebs, CENHOW);
            int dostime = readLeInt(ebs, CENTIM);
            int crc = readLeInt(ebs, CENCRC);
            int csize = readLeInt(ebs, CENSIZ);
            int size = readLeInt(ebs, CENLEN);
            int nameLen = readLeShort(ebs, CENNAM);
            int extraLen = readLeShort(ebs, CENEXT);
            int commentLen = readLeShort(ebs, CENCOM);

            int offset = readLeInt(ebs, CENOFF);

            int needBuffer = Math.max(nameLen, commentLen);
            if (buffer.length < needBuffer)
                buffer = new byte[needBuffer];

            PfdHelper.readFully(raf, buffer, 0, nameLen);
            String name = new String(buffer, 0, nameLen);

            AndroidZipEntry entry = new AndroidZipEntry(name, nameLen);
            entry.setMethod(method);
            entry.setCrc(crc & 0xffffffffL);
            entry.setSize(size & 0xffffffffL);
            entry.setCompressedSize(csize & 0xffffffffL);
            entry.setTime(dostime);
            if (extraLen > 0) {
                byte[] extra = new byte[extraLen];
                PfdHelper.readFully(raf, extra);
                entry.setExtra(extra);
            }
            if (commentLen > 0) {
                PfdHelper.readFully(raf, buffer, 0, commentLen);
                entry.setComment(new String(buffer, 0, commentLen));
            }
            entry.offset = offset;
            entries.put(name, entry);
        }
    }

    public void close() throws IOException {
        synchronized (raf) {
            closed = true;
            entries = null;
            raf.close();
        }
    }

    protected void finalize() throws IOException {
        if (!closed && raf != null) close();
    }

    public Enumeration<AndroidZipEntry> entries() {
        try {
            return new ZipEntryEnumeration(getEntries().values().iterator());
        } catch (IOException ioe) {
            return null;
        }
    }

    private HashMap<String, AndroidZipEntry> getEntries() throws IOException {
        synchronized (raf) {
            if (closed)
                throw new IllegalStateException("AndroidZipFile has closed: " + name);

            if (entries == null)
                readEntries();

            return entries;
        }
    }

    public AndroidZipEntry getEntry(String name) {
        try {
            HashMap<String, AndroidZipEntry> entries = getEntries();
            AndroidZipEntry entry = entries.get(name);
            return entry != null ? (AndroidZipEntry) entry.clone() : null;
        } catch (IOException ioe) {
            return null;
        }
    }

    private final byte[] locBuf = new byte[LOCHDR];

    private long checkLocalHeader(AndroidZipEntry entry) throws IOException {
        synchronized (raf) {
            seek(raf, entry.offset);
            PfdHelper.readFully(raf, locBuf);

            if (readLeInt(locBuf, 0) != LOCSIG)
                throw new ZipException("Wrong Local header signature: " + name);

            if (entry.getMethod() != readLeShort(locBuf, LOCHOW))
                throw new ZipException("Compression method mismatch: " + name);

            if (entry.getNameLen() != readLeShort(locBuf, LOCNAM))
                throw new ZipException("file name length mismatch: " + name);

            int extraLen = entry.getNameLen() + readLeShort(locBuf, LOCEXT);
            return entry.offset + LOCHDR + extraLen;
        }
    }

    public InputStream getInputStream(AndroidZipEntry entry) throws IOException {
        HashMap<String, AndroidZipEntry> entries = getEntries();
        String name = entry.getName();
        AndroidZipEntry zipEntry = entries.get(name);
        if (zipEntry == null)
            throw new NoSuchElementException(name);

        long start = checkLocalHeader(zipEntry);
        int method = zipEntry.getMethod();
        InputStream is = new BufferedInputStream(new PartialInputStream
                (raf, start, zipEntry.getCompressedSize()));
        switch (method) {
            case ZipOutputStream.STORED:
                return is;
            case ZipOutputStream.DEFLATED:
                return new InflaterInputStream(is, new Inflater(true));
            default:
                throw new ZipException("Unknown compression method " + method);
        }
    }

    public String getName() {
        return name;
    }

    public int size() {
        try {
            return getEntries().size();
        } catch (IOException ioe) {
            return 0;
        }
    }

    private static class ZipEntryEnumeration implements Enumeration<AndroidZipEntry> {
        private final Iterator<AndroidZipEntry> elements;

        public ZipEntryEnumeration(Iterator<AndroidZipEntry> elements) {
            this.elements = elements;
        }

        public boolean hasMoreElements() {
            return elements.hasNext();
        }

        public AndroidZipEntry nextElement() {
            return (AndroidZipEntry) (elements.next()).clone();
        }
    }

    private static class PartialInputStream extends InputStream {
        private final RandomAccessFile raf;
        long filepos, end;

        public PartialInputStream(RandomAccessFile raf, long start, long len) {
            this.raf = raf;
            filepos = start;
            end = start + len;
        }

        public int available() {
            long amount = end - filepos;
            if (amount > Integer.MAX_VALUE)
                return Integer.MAX_VALUE;
            return (int) amount;
        }

        public int read() throws IOException {
            if (filepos == end)
                return -1;
            synchronized (raf) {
                seek(raf, filepos++);
                return PfdHelper.read(raf);
            }
        }

        public int read(byte[] b, int off, int len) throws IOException {
            if (len > end - filepos) {
                len = (int) (end - filepos);
                if (len == 0)
                    return -1;
            }
            synchronized (raf) {
                seek(raf, filepos);
                int count = PfdHelper.read(raf, b, off, len);
                if (count > 0)
                    filepos += len;
                return count;
            }
        }

        public long skip(long amount) {
            if (amount < 0)
                throw new IllegalArgumentException();
            if (amount > end - filepos)
                amount = end - filepos;
            filepos += amount;
            return amount;
        }
    }
}
