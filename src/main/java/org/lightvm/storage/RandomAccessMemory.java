package org.lightvm.storage;

public class RandomAccessMemory {
    private byte[] memory;
    public RandomAccessMemory(int num64Bytes) {
        memory = new byte[num64Bytes * 64];
    }
    public byte[] getMemoryBlock(int targetAddress) {
        byte[] result = new byte[64];
        int start = targetAddress - (targetAddress % 64);
        for(int i = 0; i < 64; i++) {
            result[i] = memory[start + i];
        }
        return result;
    }
}
