package org.lightvm.machine.cpu;
import org.lightvm.machine.Machine;
import org.lightvm.utility.BinaryUtility;

import java.util.Stack;

public class ProcessingCore extends Thread {
    private final int[] registerBankInts = new int[16];
//    private final float[] registerBankFloats = new float[16];
//    private final byte[] registerBankBytes = new byte[16];

    private final Cache cache = new Cache(16);
    private int programRootAddress = 0;
    private int instructionAddress;
    private Stack<Integer> returnAddress = new Stack<>();
    private boolean ticking = false;
    private long tickCount = 0L;

    private static final String[] opcodeNames = new String[]{
        "Idle","Save","Load","Add","Subtract","Multiply","Divide","And","Or","Not","Branch","Undefined","Undefined","Undefined","Undefined","Halt"
    };

    public ProcessingCore() {}

    public void clockCPU() {
        ticking = true;
        this.start();
    }

    @Override
    public void run() {
        Machine.getInstance().getBusing().transferDiskToMemory(0,0,2000);
        cache.initializeCacheLines(); // Cant run this inside the constructor as this creates a catch 22 between the machine and cache
        while(ticking) {
            tickCount ++;
            //System.out.println(instructionAddress + "  " + returnAddress);
            executeInstruction();
        }
    }

    private int get2ByteNumAtAddress(int targetAddress) {
        return BinaryUtility.getIntFromBytes(new byte[] {
                cache.getByteAtAddress(targetAddress),
                cache.getByteAtAddress(targetAddress + 1)
        });
    }

    private void executeInstruction() {
        byte instructionRoot = cache.getByteAtAddress(instructionAddress);

        int opcode =  (instructionRoot & 0b11111111) >>> 4; // first 4 bits of first byte
        int opMetadata = (instructionRoot & 0b00001111); // last 4 bits of first byte

        //System.out.println(instructionAddress + " " + opcode + " " + opcodeNames[opcode]);

        // Used a switch statement with sequential integers as this has o(1) lookup speed rather than o(n).
        switch(opcode) {
            case 0: break; // Idle
            case 1: save(opMetadata); break;
            case 2: load(opMetadata); break;
            case 3: add(opMetadata);break;
            case 4: subtract(opMetadata);break;
            case 5: multiply(opMetadata); break;
            case 6: divide(opMetadata); break;
            case 7: and(opMetadata); break;
            case 8: or(opMetadata); break;
            case 9: not(opMetadata); break;
            case 10: branch(opMetadata); break;
            case 11: break;
            case 12: break;
            case 13: break;
            case 14: break;
            case 15: halt(); break;
        }
    }

    private void save(int metadata) {
        switch(metadata) {
            case 0: // Store reg to mem rel 1 byte opcode, 1 byte reg add, 2 byte memory add
                cache.setIntAtAddress(
                        programRootAddress + get2ByteNumAtAddress(instructionAddress + 2),
                        registerBankInts[Byte.toUnsignedInt(cache.getByteAtAddress(instructionAddress + 1))]);
                instructionAddress += 4;
                break;
            case 1: // Store literal to reg 1 byte opcode, 1 byte reg add, 4 byte literal
                registerBankInts[
                        Byte.toUnsignedInt(cache.getByteAtAddress(instructionAddress + 1))
                        ] = cache.getIntAtAddress(instructionAddress + 2);
                instructionAddress += 6;
                break;
            case 2: // Store literal to rel mem 1 byte opcode, 2 byte mem add, 4 byte literal
                cache.setIntAtAddress(
                        programRootAddress + get2ByteNumAtAddress(instructionAddress + 1),
                        cache.getIntAtAddress(instructionAddress + 3));
                instructionAddress += 7;
                break;
            case 3: // Transfer rel mem to vmem 1 byte opcode, 2 byte mem add, 2 byte vMem address 2 byte numBytesTransferred
                Machine.getInstance().getBusing().transferMemoryToVMem(
                        programRootAddress + get2ByteNumAtAddress(instructionAddress + 1),
                        get2ByteNumAtAddress(instructionAddress + 3),
                        get2ByteNumAtAddress(instructionAddress + 5));
                instructionAddress += 7;
                break;
            case 4: // Write ints rel mem to printing output 1 byte opcode 2 bytes mem address 2 bytes numIntsToTransfer
                Machine.getInstance().getBusing().transferIntsMemToPrintQueue(
                        programRootAddress + get2ByteNumAtAddress(instructionAddress + 1),
                        get2ByteNumAtAddress(instructionAddress + 3));
                instructionAddress += 5;
                break;
            case 5: // Write chars rel mem to printing output 1 byte opcode 2 bytes mem address 2 bytes numCharsToTransfer
                Machine.getInstance().getBusing().transferCharsMemToPrintQueue(
                        programRootAddress + get2ByteNumAtAddress(instructionAddress + 1),
                        get2ByteNumAtAddress(instructionAddress + 3));
                instructionAddress += 5;
                break;
            case 6: // Write rel memory to disk 1 byte opcode 2 bytes mem address 2 bytes disk address 2 bytes numBytesToTransfer
                Machine.getInstance().getBusing().transferMemoryToDisk(
                        get2ByteNumAtAddress(instructionAddress + 3),
                        programRootAddress + get2ByteNumAtAddress(instructionAddress + 1),
                        get2ByteNumAtAddress(instructionAddress + 5));
                instructionAddress += 7;
                break;
            case 7: // Write a register value to memory as a byte 1 byte opcode 1 byte reg 2 bytes mem address
                cache.setByteAtAddress(programRootAddress + get2ByteNumAtAddress(instructionAddress + 2),
                        (byte) registerBankInts[Byte.toUnsignedInt(cache.getByteAtAddress(instructionAddress + 1))]);
                instructionAddress += 4;
                break;
            case 8: // Write a literal value to a register as a byte 1 byte opcode 1 byte reg 1 byte value
                registerBankInts[Byte.toUnsignedInt(cache.getByteAtAddress(instructionAddress + 1))] = cache.getByteAtAddress(instructionAddress + 2);
                instructionAddress += 3;
                break;
            case 9: // Write a literal value to memory as a byte 1 byte opcode 2 byte mem address 1 byte value
                cache.setByteAtAddress(programRootAddress + get2ByteNumAtAddress(instructionAddress + 1), cache.getByteAtAddress(instructionAddress + 3));
                instructionAddress += 4;
                break;
            case 10: // Write a literal value to memory as a byte 1 byte opcode 1 byte reg that will contain mem address 1 byte literal
                cache.setByteAtAddress(programRootAddress + registerBankInts[Byte.toUnsignedInt(cache.getByteAtAddress(instructionAddress + 1))], cache.getByteAtAddress(instructionAddress + 2));
                instructionAddress += 3;
                break;
            case 11: // Save rel mem to visual memory using registers as addresses 1 byte opcode 1 byte reg1 1 byte reg2 1 byte reg3
                Machine.getInstance().getBusing().transferMemoryToVMem(
                        programRootAddress + registerBankInts[Byte.toUnsignedInt(cache.getByteAtAddress(instructionAddress + 1))],
                        registerBankInts[Byte.toUnsignedInt(cache.getByteAtAddress(instructionAddress + 2))],
                        registerBankInts[Byte.toUnsignedInt(cache.getByteAtAddress(instructionAddress + 3))]);
                instructionAddress += 4;
                break;
            case 12: // wipe display 1 byte opcode
                Machine.getInstance().getBusing().wipeDisplay();
                instructionAddress += 1;
                break;
        }
    }

    private void load(int metadata) {
        switch(metadata) {
            case 0: // Load Memory into reg 1 byte opcode 1 byte reg address 2 byte memory address
                registerBankInts[
                        Byte.toUnsignedInt(cache.getByteAtAddress(instructionAddress + 1))
                        ] = cache.getIntAtAddress(programRootAddress + get2ByteNumAtAddress(instructionAddress + 2));
                instructionAddress += 4;
                //System.out.println("LOADING INTO REG" + Byte.toUnsignedInt(cache.getByteAtAddress(instructionAddress + 1)) + " " + cache.getIntAtAddress(programRootAddress + get2ByteNumAtAddress(instructionAddress + 2)));
                break;
            case 1: // Transfer disk to rel memory 1 byte opcode 2 byte mem address 2 byte disk address 2 byte numBytes
                Machine.getInstance().getBusing().transferDiskToMemory(
                        get2ByteNumAtAddress(instructionAddress + 3),
                        programRootAddress + get2ByteNumAtAddress(instructionAddress + 1),
                        get2ByteNumAtAddress(instructionAddress + 5));
                instructionAddress += 7;
                break;
            case 2: // Transfer unprocessed inputted char to a location in memory 1 byte opcode 2 bytes mem address
                Machine.getInstance().getBusing().loadMostRecentCharToMem(programRootAddress + get2ByteNumAtAddress(instructionAddress + 1));
                instructionAddress += 3;
                break;
            case 3:
                registerBankInts[
                        Byte.toUnsignedInt(cache.getByteAtAddress(instructionAddress + 1))
                        ] = Byte.toUnsignedInt(cache.getByteAtAddress(programRootAddress + get2ByteNumAtAddress(instructionAddress + 2)));
                instructionAddress += 4;
                break;
            case 4: break;
        }
    }

    private void add(int metadata) {
        int[] opNumbers = getOperationNumbers(1, metadata);
        int instructionOffset = opNumbers[0];
        registerBankInts[Byte.toUnsignedInt(cache.getByteAtAddress(instructionAddress + instructionOffset))] = opNumbers[1] + opNumbers[2];
        instructionAddress += instructionOffset + 1;
    }
    private void subtract(int metadata) {
        int[] opNumbers = getOperationNumbers(1, metadata);
        int instructionOffset = opNumbers[0];
        registerBankInts[Byte.toUnsignedInt(cache.getByteAtAddress(instructionAddress + instructionOffset))] = opNumbers[1] - opNumbers[2];
        instructionAddress += instructionOffset + 1;
    }
    private void multiply(int metadata) {
        int[] opNumbers = getOperationNumbers(1, metadata);
        int instructionOffset = opNumbers[0];
        registerBankInts[Byte.toUnsignedInt(cache.getByteAtAddress(instructionAddress + instructionOffset))] = opNumbers[1] * opNumbers[2];
        instructionAddress += instructionOffset + 1;
    }
    private void divide(int metadata) {
        int[] opNumbers = getOperationNumbers(1, metadata);
        int instructionOffset = opNumbers[0];
        registerBankInts[Byte.toUnsignedInt(cache.getByteAtAddress(instructionAddress + instructionOffset))] = opNumbers[1] / opNumbers[2];
        instructionAddress += instructionOffset + 1;
    }
    private void and(int metadata) {
        int[] opNumbers = getOperationNumbers(1, metadata);
        int instructionOffset = opNumbers[0];
        registerBankInts[Byte.toUnsignedInt(cache.getByteAtAddress(instructionAddress + instructionOffset))] = opNumbers[1] & opNumbers[2];
        instructionAddress += instructionOffset + 1;
    }
    private void or(int metadata) {
        int[] opNumbers = getOperationNumbers(1, metadata);
        int instructionOffset = opNumbers[0];
        registerBankInts[Byte.toUnsignedInt(cache.getByteAtAddress(instructionOffset))] = opNumbers[1] | opNumbers[2];
        instructionAddress += instructionOffset + 1;
    }
    private void not(int metadata) {
        registerBankInts[metadata] = ~registerBankInts[Byte.toUnsignedInt(cache.getByteAtAddress(instructionAddress + 1))];
        instructionAddress += 2;
    }
    private void branch(int metadata) {
        // get first bit which is used to determine whether the address is relative or absolute
        int firstBit = (metadata & (0b00001000)) >>> 3;
        metadata = metadata & 0b00000111; // remove first bit from metadata

        int relOffset = programRootAddress;
        int temp = instructionAddress;
        int potentialOffset = 0;
        boolean failedBranch = true;
        switch(metadata) {
            case 0:
                instructionAddress = programRootAddress + get2ByteNumAtAddress(instructionAddress + 1);
                failedBranch = false;
                potentialOffset = 3;
                break;
            case 1:
                if(registerBankInts[Byte.toUnsignedInt(cache.getByteAtAddress(instructionAddress + 1))] ==
                        registerBankInts[Byte.toUnsignedInt(cache.getByteAtAddress(instructionAddress + 2))]) {
                    instructionAddress = relOffset + programRootAddress + get2ByteNumAtAddress(instructionAddress + 3);
                    failedBranch = false;
                }
                potentialOffset = 5;
                break;
            case 2:
                if(registerBankInts[Byte.toUnsignedInt(cache.getByteAtAddress(instructionAddress + 1))] >
                        registerBankInts[Byte.toUnsignedInt(cache.getByteAtAddress(instructionAddress + 2))]) {
                    instructionAddress = relOffset + programRootAddress + get2ByteNumAtAddress(instructionAddress + 3);
                    failedBranch = false;
                }
                potentialOffset = 5;
                break;
            case 3:
                if(registerBankInts[Byte.toUnsignedInt(cache.getByteAtAddress(instructionAddress + 1))] <
                        registerBankInts[Byte.toUnsignedInt(cache.getByteAtAddress(instructionAddress + 2))]) {
                    instructionAddress = relOffset + programRootAddress + get2ByteNumAtAddress(instructionAddress + 3);
                    failedBranch = false;
                }
                potentialOffset = 5;
                break;
            case 4:
                instructionAddress = returnAddress.pop();
                failedBranch = false;
                break;
        }

        if(failedBranch) {
            instructionAddress += potentialOffset;
        }else if(firstBit == 1) {
            returnAddress.push(temp + potentialOffset);
        }
    }

    private void halt() {
        ticking = false;
    }

    private int[] getOperationNumber(int offset, int code) {
        int opNumber = 0;

        switch (code) {
            case 0: // register
                opNumber = registerBankInts[Byte.toUnsignedInt(cache.getByteAtAddress(instructionAddress + offset))];
                offset += 1;
                break;
            case 1: // memory relative
                opNumber = cache.getIntAtAddress(
                        programRootAddress + get2ByteNumAtAddress(instructionAddress + offset)
                );
                offset += 2;
                break;
            case 2: // memory absolute
                opNumber = cache.getIntAtAddress(
                        get2ByteNumAtAddress(instructionAddress + offset)
                );
                offset += 2;
                break;
            case 3: // literal
                opNumber = cache.getIntAtAddress(instructionAddress + offset);
                offset += 4;
        }

        return new int[] {offset, opNumber};
    }

    private int[] getOperationNumbers(int offset, int opMetadata) {
        int topTwoBits = opMetadata >>> 2;
        int bottomTwoBits = opMetadata & 0b00000011;

        int[] firstNumResult = getOperationNumber(offset, topTwoBits);
        offset = firstNumResult[0];

        int[] secondNumResult = getOperationNumber(offset, bottomTwoBits);
        offset = secondNumResult[0];

        return new int[] {offset, firstNumResult[1], secondNumResult[1]};
    }

    public void snoopMemoryMutation(int address) {
        cache.revalidateLine(address);
    }
}
