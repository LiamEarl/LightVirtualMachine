package org.lightvm.cpu;
import lombok.Getter;
import java.util.Arrays;

public class Cache {
    /*
    { all cache lines
        {  one cache line
            individual number
            .
            .
            last index of a cache line is always metadata for the chunk of memory it's associated with
            (in this implementation)
        },
        .
        .
    }
    */
    @Getter
    private final byte[][] cacheLines;
    @Getter
    private final int numLines;
    private final byte[] lineAccessedOrder;
    public Cache(int num64ByteLines) {
        numLines = num64ByteLines;
        cacheLines = new byte[num64ByteLines][17];
        lineAccessedOrder = new byte[cacheLines.length];
    }
    private void updateAccessedOrder(int lineAccessed) {
        for(int i = 1; i < lineAccessedOrder.length; i++) {
            if(lineAccessedOrder[i] == lineAccessed) {
                continue;
            }

        }
    }
    public byte[] getCacheLine(int index) {
        return cacheLines[index];
    }
    public void replaceLine(int lineToReplace, byte[] data, byte chunkNumber) {

        cacheLines[lineToReplace] = Arrays.copyOf(data, 65);
        cacheLines[lineToReplace][64] = chunkNumber;
    }
}
