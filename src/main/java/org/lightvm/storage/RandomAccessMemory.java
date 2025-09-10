package org.lightvm.storage;

public class RandomAccessMemory {
    private byte[] memory;
    public RandomAccessMemory(int num64Bytes) {
        memory = new byte[num64Bytes * 64];
        //TODO Remove this when I set up the boot system
        //memory[1000] =
    }
    public byte[] getMemoryBlock(short targetAddress) {
        // The short datatype is always signed. For memory addresses it must be unsigned. This turns it into an unsigned int.
        int address = Short.toUnsignedInt(targetAddress);
        byte[] result = new byte[64];
        int start = targetAddress - (targetAddress % 64);
        for(int i = 0; i < 64; i++) {
            result[i] = memory[start + i];
        }
        return result;
    }
}
