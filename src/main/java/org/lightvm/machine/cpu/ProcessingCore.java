package org.lightvm.machine.cpu;
import org.lightvm.machine.Machine;
import org.lightvm.machine.busing.Busing;
import org.lightvm.utility.BinaryUtility;

import javax.crypto.Mac;

public class ProcessingCore extends Thread {
    private final int[] registerBankInts = new int[16];
//    private final float[] registerBankFloats = new float[16];
//    private final byte[] registerBankBytes = new byte[16];

    private final Cache cache = new Cache(1024);
    private int programRootAddress = 0;
    private int instructionAddress;
    private int returnAddress = 0;
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
        cache.initializeCacheLines(); // Cant run this inside the constructor as this creates a catch 22 between the machine and cache

        while(ticking) {
            tickCount ++;
            executeInstruction();
        }
    }

    private int get2ByteNumAtAddress(int targetAddress) {
        return BinaryUtility.getIntFromBytes(new byte[] {
                cache.getByteAtAddress(targetAddress),
                cache.getByteAtAddress(targetAddress+1)
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
            case 7:
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
                break;
            case 1: // Transfer disk to rel memory 1 byte opcode 2 byte mem address 2 byte disk address 2 byte numBytes
                Machine.getInstance().getBusing().transferDiskToMemory(
                        get2ByteNumAtAddress(instructionAddress + 3),
                        programRootAddress + get2ByteNumAtAddress(instructionAddress + 1),
                        get2ByteNumAtAddress(instructionAddress + 5));
                instructionAddress += 7;
                break;
            case 2: break;
            case 3: break;
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
        registerBankInts[Byte.toUnsignedInt(cache.getByteAtAddress(instructionOffset))] = opNumbers[1] & opNumbers[2];
        instructionAddress += instructionOffset + 1;
    }
    private void or(int metadata) {
        int[] opNumbers = getOperationNumbers(1, metadata);
        int instructionOffset = opNumbers[0];
        registerBankInts[Byte.toUnsignedInt(cache.getByteAtAddress(instructionOffset))] = opNumbers[1] | opNumbers[2];
        instructionAddress += instructionOffset + 1;
    }
    private void not(int metadata) {
        registerBankInts[metadata] = ~registerBankInts[Byte.toUnsignedInt(cache.getByteAtAddress(instructionAddress))];
        instructionAddress += 2;
    }
    private void branch(int metadata) {
        // get first bit which is used to determine whether the address is relative or absolute
        int firstBit = (metadata & (0b00001000)) >>> 3;
        metadata = metadata & 0b00000111; // remove first bit from metadata

        int relOffset = (firstBit == 0) ? 0 : programRootAddress;
        int temp = instructionAddress;
        int potentialOffset = 0;
        switch(metadata) {
            case 0: instructionAddress = programRootAddress + get2ByteNumAtAddress(instructionAddress + 1); break;
            case 1:
                if(registerBankInts[Byte.toUnsignedInt(cache.getByteAtAddress(instructionAddress + 1))] ==
                        registerBankInts[Byte.toUnsignedInt(cache.getByteAtAddress(instructionAddress + 2))]) {
                    instructionAddress = relOffset + get2ByteNumAtAddress(instructionAddress + 3);
                }else {
                    potentialOffset = 5;
                }
                break;
            case 2:
                if(registerBankInts[Byte.toUnsignedInt(cache.getByteAtAddress(instructionAddress + 1))] >
                        registerBankInts[Byte.toUnsignedInt(cache.getByteAtAddress(instructionAddress + 2))]) {
                    instructionAddress = relOffset + get2ByteNumAtAddress(instructionAddress + 3);
                }else {
                    potentialOffset = 5;
                }
                break;
            case 3:
                if(registerBankInts[Byte.toUnsignedInt(cache.getByteAtAddress(instructionAddress + 1))] <
                        registerBankInts[Byte.toUnsignedInt(cache.getByteAtAddress(instructionAddress + 2))]) {
                    instructionAddress = relOffset + get2ByteNumAtAddress(instructionAddress + 3);
                }else {
                    potentialOffset = 5;
                }
                break;
            case 4:
                instructionAddress = returnAddress;
                break;
        }
        instructionAddress += potentialOffset;
        if(potentialOffset == 0) returnAddress = temp + 1;
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
}
