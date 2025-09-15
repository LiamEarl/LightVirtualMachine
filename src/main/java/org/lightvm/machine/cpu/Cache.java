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

    public byte load(int addressToLoad) {
        for(int i = 0; i < cacheLines.length; i++) {
            // Get the two bytes at the end of the cache line and convert them into a proper integer
            int lineMemoryAddress = BinaryUtility.getIntFromBytes(
                    new byte[] {cacheLines[lineAccessedOrder[i]][64], cacheLines[lineAccessedOrder[i]][65]}
            );
            // Find the difference between the root of the block and the address you're searching for
            int cacheDiff = addressToLoad - (lineMemoryAddress * 64);
            // If the difference is between 0 and 63 then you know that you are accessing the right cache line
            if(cacheDiff >= 0 && cacheDiff < 64) {
                // Make sure that the accessed order is up to date and send the data off
                updateAccessedOrder(lineAccessedOrder[i]);
                return cacheLines[lineAccessedOrder[i]][cacheDiff];
            }
        }

        //cache miss

        // Get the block of memory that maps to the address requested
        byte[] newLine = Machine.getInstance().getBusing().getMemoryBlock(addressToLoad);
        // Find where that address sits on that block
        int lineOffset = addressToLoad % 64;
        // Find the root address of the block of memory that you're pulling
        short newLineAddress = (short) (addressToLoad - lineOffset);
        // Take the line of cache that is the most stale (last accessed) and replace it with that block
        replaceLine(lineAccessedOrder[cacheLines.length - 1], newLine, newLineAddress);
        // Return the data initially requested.
        return cacheLines[lineAccessedOrder[0]][lineOffset];
    }

    public void setIntAtAddress(int addressToSet, int value) {
        for(int i = 0; i < cacheLines.length; i++) {
            // Get the two bytes at the end of the cache line and convert them into a proper integer
            int lineMemoryAddress = BinaryUtility.getIntFromBytes(
                    new byte[] {cacheLines[lineAccessedOrder[i]][64], cacheLines[lineAccessedOrder[i]][65]}
            );
            // Find the difference between the root of the block and the address you're searching for
            int cacheDiff = addressToSet - (lineMemoryAddress * 64);

            // If the difference is between 0 and 60 then you know that you are accessing the right cache line
            if(cacheDiff >= 0 && cacheDiff < 61) {
                //Update accessed order and set the value
                updateAccessedOrder(lineAccessedOrder[i]);
                cacheLines[lineAccessedOrder[i]][cacheDiff] = (byte) (value >>> 24);
                cacheLines[lineAccessedOrder[i]][cacheDiff + 1] = (byte) (value >>> 16);
                cacheLines[lineAccessedOrder[i]][cacheDiff + 2] = (byte) (value >>> 8);
                cacheLines[lineAccessedOrder[i]][cacheDiff + 3] = (byte) (value);
                return;
            }
        }
        System.out.println("Address to set was not loaded in cache, or tried to wrap an integer between cache lines");
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
