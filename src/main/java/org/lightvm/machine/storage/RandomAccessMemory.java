package org.lightvm.machine.storage;
import lombok.Getter;
import org.lightvm.utility.BinaryUtility;

public class RandomAccessMemory {
    @Getter
    private byte[] memory;
    public RandomAccessMemory(int num64Bytes) {
        memory = new byte[num64Bytes * 64];
        //Set 0 at 0
        memory[0] = (byte) 0b10100000;
        memory[1] = (byte) 0b11111111;
        memory[2] = (byte) 0b11111111;
        memory[3] = (byte) 0b11111111;
        memory[4] = (byte) 0b11111111;
        //Set 1 at 1
        memory[5] = (byte) 0b10100001;
        memory[6] = (byte) 0b00000000;
        memory[7] = (byte) 0b00000000;
        memory[8] = (byte) 0b00000000;
        memory[9] = (byte) 0b00000001;
        //Add 0 and 1 place it in 0
        memory[10] = (byte) 0b01010000;
        memory[11] = (byte) 0b00000001;
        //Add 0 and 1 place it in 1
        memory[12] = (byte) 0b01010001;
        memory[13] = (byte) 0b00000001;
        //Branch back to memory add 10
        memory[14] = (byte) 0b10010000;
        memory[15] = (byte) 0b00000000;
        memory[16] = (byte) 0b00001010;
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
