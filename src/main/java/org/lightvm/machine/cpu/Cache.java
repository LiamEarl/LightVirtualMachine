package org.lightvm.machine.cpu;
import lombok.Getter;
import org.lightvm.machine.Machine;
import org.lightvm.utility.BinaryUtility;

import java.util.Arrays;

public class Cache {
    /*
    { all cache lines
        {  one cache line
            individual number
            .
            .
            last 2 index of a cache line is always metadata for the chunk of memory it's associated with
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

    public Cache(int num64ByteLines) {
        numLines = num64ByteLines;
        cacheLines = new byte[num64ByteLines][66];
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

    private int[] findCacheLine(int address) {
        for(int i = 0; i < cacheLines.length; i++) {
            // Get the two bytes at the end of the cache line and convert them into a proper integer
            int lineMemoryAddress = BinaryUtility.getIntFromBytes(
                    new byte[] {cacheLines[lineAccessedOrder[i]][64], cacheLines[lineAccessedOrder[i]][65]}
            );
            // Find the difference between the root of the block and the address you're searching for
            int cacheDiff = address - (lineMemoryAddress * 64);
            // If the difference is between 0 and 63 then you know that you are accessing the right cache line
            if(cacheDiff >= 0 && cacheDiff < 64) {
                // Make sure that the accessed order is up to date and send the data off
                updateAccessedOrder(lineAccessedOrder[i]);
                return new int[] {lineAccessedOrder[0], cacheDiff};
            }
        }
        return new int[] {-1, -1};
    }

    private int fetchFromMemory(int address) {
        // Get the block of memory that maps to the address requested
        byte[] newLine = Machine.getInstance().getBusing().getMemoryBlock(address);
        // Find where that address sits on that block
        int lineOffset = address % 64;
        // Find the root address of the block of memory that you're pulling
        short newLineAddress = (short) (address - lineOffset);
        // Take the line of cache that is the most stale (last accessed) and replace it with that block
        replaceLine(lineAccessedOrder[cacheLines.length - 1], newLine, newLineAddress);
        return lineOffset;
    }

    public byte load(int addressToLoad) {
        int[] appropriateCacheLine = findCacheLine(addressToLoad);

        if(appropriateCacheLine[0] != -1)
            return cacheLines[appropriateCacheLine[0]][appropriateCacheLine[1]];

        //cache miss

        int lineOffset = fetchFromMemory(addressToLoad);

        // Return the data initially requested.
        return cacheLines[lineAccessedOrder[0]][lineOffset];
    }

    public void setByteAtAddress(int addressToSet, byte value) {
        for(int i = 0; i < cacheLines.length; i++) {
            // Get the two bytes at the end of the cache line and convert them into a proper integer
            int lineMemoryAddress = BinaryUtility.getIntFromBytes(
                    new byte[] {cacheLines[lineAccessedOrder[i]][64], cacheLines[lineAccessedOrder[i]][65]}
            );
            // Find the difference between the root of the block and the address you're searching for
            int cacheDiff = addressToSet - (lineMemoryAddress * 64);

            // If the difference is between 0 and 63 then you know that you are accessing the right cache line
            if(cacheDiff >= 0 && cacheDiff < 64) {
                //Update accessed order and set the value
                updateAccessedOrder(lineAccessedOrder[i]);
                cacheLines[lineAccessedOrder[i]][cacheDiff] = value;
                return;
            }
        }
        System.out.println("Address to set was not loaded in cache");
    }

    public void setIntAtAddress(int addressToSet, int value) {
        int[] appropriateCacheLine = findCacheLine(addressToSet);

        if(appropriateCacheLine[0] != -1) {
            if(appropriateCacheLine[1] > 60)
                throw new IllegalArgumentException("Tried to set an integer between cache lines at setIntAtAddress");
            cacheLines[appropriateCacheLine[0]][appropriateCacheLine[1]] = (byte) (value >>> 24);
            cacheLines[appropriateCacheLine[0]][appropriateCacheLine[1] + 1] = (byte) (value >>> 16);
            cacheLines[appropriateCacheLine[0]][appropriateCacheLine[1] + 2] = (byte) (value >>> 8);
            cacheLines[appropriateCacheLine[0]][appropriateCacheLine[1] + 3] = (byte) (value);

        }else System.out.println("Address to set was not loaded in cache. setIntAtAddress");

    }

    public int getIntAtAddress(int addressToLoad) {
        int[] appropriateCacheLine = findCacheLine(addressToLoad);
        int offset, cacheLineIndex;

        if(appropriateCacheLine[0] != -1 && appropriateCacheLine[1] <= 60) {
            cacheLineIndex = appropriateCacheLine[0];
            offset = appropriateCacheLine[1];
        }else {
            offset = fetchFromMemory(addressToLoad);
            cacheLineIndex = lineAccessedOrder[0];
        }

        return BinaryUtility.getIntFromBytes(new byte[] {
                cacheLines[cacheLineIndex][offset],
                cacheLines[cacheLineIndex][offset + 1],
                cacheLines[cacheLineIndex][offset + 2],
                cacheLines[cacheLineIndex][offset + 3],
        });
    }

    public byte[] getCacheLine(int index) {
        return cacheLines[index];
    }

    public void replaceLine(int lineToReplace, byte[] data, short chunkNumber) {
        cacheLines[lineToReplace] = Arrays.copyOf(data, 66);

        byte mostSignificantByte = (byte) ((chunkNumber >>> 8) & 0xFF);
        byte leastSignificantByte = (byte) (chunkNumber & 0xFF);

        cacheLines[lineToReplace][64] = mostSignificantByte;
        cacheLines[lineToReplace][65] = leastSignificantByte;

        updateAccessedOrder(lineToReplace);
    }
}
