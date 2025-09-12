package org.lightvm.machine.storage;
import lombok.Getter;

public class RandomAccessMemory {
    @Getter
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

    public void setByte(int address, byte data) {
        memory[address] = data;
    }
}
