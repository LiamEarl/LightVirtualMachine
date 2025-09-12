package org.lightvm.machine.cpu;
import lombok.Getter;
import org.lightvm.machine.busing.Busing;

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
    private final int[] lineAccessedOrder;
    private final Busing bus;

    public Cache(int num64ByteLines, Busing bus) {
        this.bus = bus;
        numLines = num64ByteLines;
        cacheLines = new byte[num64ByteLines][17];
        // instantiate lineAccessedOrder and put some initial values in ascending order
        lineAccessedOrder = new int[cacheLines.length];
        for(int i = 0; i < lineAccessedOrder.length; i++) {lineAccessedOrder[i] = i;}
    }

    private void updateAccessedOrder(int lineAccessed) {
        int next = lineAccessed;

        for(int i = 0; i < lineAccessedOrder.length; i++) {
            int lineAtIndex = lineAccessedOrder[i];
            if(lineAtIndex == lineAccessed) {
                lineAccessedOrder[i] = next;
                return;
            }
            lineAccessedOrder[i] = next;
            next = lineAtIndex;
        }
    }

    public byte load(int address) {
        for(int i = 0; i < cacheLines.length; i++) {
            int cacheDiff = address - (Byte.toUnsignedInt(cacheLines[lineAccessedOrder[i]][64]) * 64);

            if(cacheDiff >= 0 && cacheDiff < 64) {
                updateAccessedOrder(lineAccessedOrder[i]);
                return cacheLines[lineAccessedOrder[i]][cacheDiff];
            }
        }

        //cache.loadLine()
        return (byte) 10;
    }

    public void setAddress(int address, byte value) {
        for(int i = 0; i < cacheLines.length; i++) {
            int cacheDiff = address - (Byte.toUnsignedInt(cacheLines[lineAccessedOrder[i]][64]) * 64);

            if(cacheDiff >= 0 && cacheDiff < 64) {
                updateAccessedOrder(lineAccessedOrder[i]);
                cacheLines[lineAccessedOrder[i]][cacheDiff] = value;
                return;
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
