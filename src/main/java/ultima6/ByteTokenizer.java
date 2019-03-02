package ultima6;

import java.nio.ByteBuffer;

public class ByteTokenizer {

    private int currentPosition;
    private int newPosition;
    private final int maxPosition;
    private final byte[] bytes;
    private final byte[] delimiters;
    private final byte[] exceptions;

    public ByteTokenizer(ByteBuffer bb, int start, int length, byte[] delimiters, byte[] exceptions) {
        this.bytes = new byte[length];
        for (int i = 0; i < length; i++) {
            this.bytes[i] = bb.get(start + i);
        }
        this.delimiters = delimiters;
        this.exceptions = exceptions;
        currentPosition = 0;
        newPosition = -1;
        maxPosition = bytes.length;
    }

    public ByteTokenizer(ByteBuffer bb, int start, int length, byte... delimiters) {
        this.bytes = new byte[length];
        for (int i = 0; i < length; i++) {
            this.bytes[i] = bb.get(start + i);
        }
        this.delimiters = delimiters;
        this.exceptions = null;
        currentPosition = 0;
        newPosition = -1;
        maxPosition = bytes.length;
    }

    public ByteTokenizer(byte[] bytes, byte[] delimiters, byte[] exceptions) {
        this.bytes = bytes;
        this.delimiters = delimiters;
        this.exceptions = exceptions;
        currentPosition = 0;
        newPosition = -1;
        maxPosition = bytes.length;
    }

    public ByteTokenizer(byte[] bytes, byte... delimiters) {
        this.bytes = bytes;
        this.delimiters = delimiters;
        this.exceptions = null;
        currentPosition = 0;
        newPosition = -1;
        maxPosition = bytes.length;
    }

    public byte[] data() {
        return this.bytes;
    }

    public void reset() {
        currentPosition = 0;
        newPosition = -1;
    }

    public boolean hasMoreTokens() {
        newPosition = skipDelimiters(currentPosition);
        return (newPosition < maxPosition);
    }

    public byte[] nexToken() {
        currentPosition = (newPosition >= 0) ? newPosition : skipDelimiters(currentPosition);
        newPosition = -1;
        if (currentPosition >= maxPosition) {
            throw new IndexOutOfBoundsException("Current token position is out of bounds.");
        }
        final int startPosition = currentPosition;
        currentPosition = scanToken(currentPosition);
        final int length = currentPosition - startPosition;
        final byte[] token = new byte[length];
        System.arraycopy(bytes, startPosition, token, 0, length);
        return token;
    }

    public int countTokens() {
        int tokenNums = 0;
        int idx = currentPosition;
        while (idx < maxPosition) {
            idx = skipDelimiters(idx);
            if (idx >= maxPosition) {
                break;
            }
            idx = scanToken(idx);
            tokenNums++;
        }
        return tokenNums;
    }

    public Byte nextDelimiter() {
        int idx = currentPosition;
        while (idx < maxPosition) {
            if (isDelimiter(idx)) {
                return bytes[idx];
            }
            idx++;
        }
        return null;
    }

    private int skipDelimiters(int pos) {
        int idx = pos;
        while (idx < maxPosition) {
            if (isDelimiter(idx)) {
                idx++;
            }
            break;
        }
        return idx;
    }

    private int scanToken(int pos) {
        int idx = pos;
        while (idx < maxPosition) {
            if (isDelimiter(idx)) {
                break;
            }
            idx++;
        }
        return idx;
    }

    private boolean isDelimiter(int pos) {
        for (byte delim : delimiters) {
            if (bytes[pos] == delim) {
                if (this.exceptions != null && pos >= 1) {
                    for (byte exc : exceptions) {
                        if (bytes[pos - 1] == exc) {
                            return false;
                        }
                    }
                }
                return true;
            }
        }
        return false;
    }

}
