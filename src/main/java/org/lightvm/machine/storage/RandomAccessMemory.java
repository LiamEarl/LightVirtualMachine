package org.lightvm.machine.storage;
import lombok.Getter;
import org.lightvm.utility.BinaryUtility;

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

    public int getIntAtAddress(int targetAddress) {
        if(targetAddress > memory.length - 4)
            throw new IllegalArgumentException("targetAddress + 3 outside memory bounds at RAM getIntAtAddress");
        return BinaryUtility.getIntFromBytes(new byte[] {
                memory[targetAddress],
                memory[targetAddress + 1],
                memory[targetAddress + 2],
                memory[targetAddress + 3]}
        );
    }

    public void setByte(int address, byte data) {
        memory[address] = data;
    }

    public void placeInt(int address, int integer) {
        memory[address] = (byte) (integer >>> 24);
        memory[address + 1] = (byte) (integer >>> 16);
        memory[address + 2] = (byte) (integer >>> 8);
        memory[address + 3] = (byte) (integer);
    }
}
