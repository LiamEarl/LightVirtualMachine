package org.lightvm.machine.storage;
import lombok.Getter;
import org.lightvm.utility.BinaryUtility;

public class RandomAccessMemory {
    @Getter
    private byte[] memory;
    public RandomAccessMemory() {
        memory = new byte[1024 * 64];
        //Code to calculate and print 50 fibbonaci sequence numbers

        //save 0 at 0
        memory[0] = (byte) 0b00010001;
        memory[1] = (byte) 0b00000000; // register address 0
        memory[2] = (byte) 0b00000000;// )
        memory[3] = (byte) 0b00000000;// - literal for 0
        memory[4] = (byte) 0b00000000;// -
        memory[5] = (byte) 0b00000000;// )

        //save 1 at 1
        memory[6] = (byte) 0b00010001;
        memory[7] = (byte) 0b00000001; // register address 1
        memory[8] = (byte) 0b00000000;// )
        memory[9] = (byte) 0b00000000;// - literal for 1
        memory[10] = (byte) 0b00000000;// -
        memory[11] = (byte) 0b00000001;// )

        //save 0 at 2
        memory[12] = (byte) 0b00010001;
        memory[13] = (byte) 0b00000010; // register address 1
        memory[14] = (byte) 0b00000000;// )
        memory[15] = (byte) 0b00000000;// - literal for 0
        memory[16] = (byte) 0b00000000;// -
        memory[17] = (byte) 0b00000000;// )

        //save 50 at 3
        memory[18] = (byte) 0b00010001;
        memory[19] = (byte) 0b00000011; // register address 3
        memory[20] = (byte) 0b00000000;// )
        memory[21] = (byte) 0b00000000;// - literal for 50
        memory[22] = (byte) 0b00000000;// -
        memory[23] = (byte) 0b00110010;// )

        //save reg 0 to add 128
        memory[24] = (byte) 0b00010000; // Save reg to mem
        memory[25] = (byte) 0b00000000; // reg 0
        memory[26] = (byte) 0b00000000;
        memory[27] = (byte) 0b10000000; // store at mem address 128

        //print add 128
        memory[28] = (byte) 0b00010100; // write ints mem to printing output
        memory[29] = (byte) 0b00000000;//]
        memory[30] = (byte) 0b10000000;//] mem address 128
        memory[31] = (byte) 0b00000000;
        memory[32] = (byte) 0b00000001;// transfer 1 int

        //print add 255 (newline)
        memory[33] = (byte) 0b00010101; // write chars mem to printing output
        memory[34] = (byte) 0b00000000;//]
        memory[35] = (byte) 0b11111111;//] mem address 255
        memory[36] = (byte) 0b00000000;
        memory[37] = (byte) 0b00000001;// transfer 1 char

        //add 0 and 1 save to 0
        memory[38] = (byte) 0b00110000; // code add, reg + reg
        memory[39] = (byte) 0b00000000; // register address 0
        memory[40] = (byte) 0b00000001; // register address 1
        memory[41] = (byte) 0b00000000; // save to register address 0

        //save reg 1 to add 132
        memory[42] = (byte) 0b00010000; // Save reg to mem
        memory[43] = (byte) 0b00000001; // reg 1
        memory[44] = (byte) 0b00000000;
        memory[45] = (byte) 0b10000100; // store at mem address 132

        //print add 132
        memory[46] = (byte) 0b00010100; // write ints mem to printing output
        memory[47] = (byte) 0b00000000;//]
        memory[48] = (byte) 0b10000100;//] mem address 128
        memory[49] = (byte) 0b00000000;
        memory[50] = (byte) 0b00000001;// transfer 1 int

        //print add 255 (newline)
        memory[51] = (byte) 0b00010101; // write chars mem to printing output
        memory[52] = (byte) 0b00000000;//]
        memory[53] = (byte) 0b11111111;//] mem address 255
        memory[54] = (byte) 0b00000000;
        memory[55] = (byte) 0b00000001;// transfer 1 char

        //add 0 and 1 save to 1
        memory[56] = (byte) 0b00110000; // code add, reg + reg
        memory[57] = (byte) 0b00000000; // register address 0
        memory[58] = (byte) 0b00000001; // register address 1
        memory[59] = (byte) 0b00000001; // save to register address 0

        //add 2 with literal 1 and save to 2
        memory[60] = (byte) 0b00110011; // add a register with a literal
        memory[61] = (byte) 0b00000010; // register 2
        memory[62] = (byte) 0b00000000;
        memory[63] = (byte) 0b00000000;
        memory[64] = (byte) 0b00000000;
        memory[65] = (byte) 0b00000001; // literal 1
        memory[66] = (byte) 0b00000010; // register 2

        //branch back to beginning if reg 2 is less than 50
        memory[67] = (byte) 0b10101011; // branch if reg 1 < reg 2
        memory[68] = (byte) 0b00000010; // reg 2
        memory[69] = (byte) 0b00000011; // < reg 3
        memory[70] = (byte) 0b00000000;
        memory[71] = (byte) 0b00011000; //branch to address 24

        //halt
        memory[72] = (byte) 0b11110000; // HALT, program is done

        memory[255] = (byte) '~';// Save the newline character to memory at address 255
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

    public byte getByte(int address) {
        return memory[address];
    }

    public void placeInt(int address, int integer) {
        memory[address] = (byte) (integer >>> 24);
        memory[address + 1] = (byte) (integer >>> 16);
        memory[address + 2] = (byte) (integer >>> 8);
        memory[address + 3] = (byte) (integer);
    }
}
