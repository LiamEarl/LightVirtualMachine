package org.lightvm.machine.cpu;
import org.lightvm.machine.Machine;
import org.lightvm.machine.busing.Busing;
import org.lightvm.utility.BinaryUtility;

public class ProcessingCore extends Thread {
    private final int[] registerBank;
    private final Cache cache;
    private int accumulator;
    private int programRootAddress;
    private short instructionAddress;
    private byte instruction;
    private final int clockSpeedHertz;
    private boolean ticking = false;
    private long tickCount = 0L;

    public ProcessingCore(int num64ByteCacheLines, int registerBankSize, int clockSpeedHertz) {
        cache = new Cache(num64ByteCacheLines);
        registerBank = new int[registerBankSize];
        this.clockSpeedHertz = clockSpeedHertz;
        programRootAddress = 0;
    }

    public void clockCPU() {
        ticking = true;
        this.start();
    }

    @Override
    public void run() {
        int millisDelay = 1000 / clockSpeedHertz;
        long start = System.currentTimeMillis();
        while(ticking) {
            tickCount ++;
            long timePassed = System.currentTimeMillis() - start;
            System.out.printf("Tick #: %d, Time Passed: %d seconds.\n", tickCount, timePassed /1000);

            try {
                Thread.sleep(millisDelay);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void executeInstruction(byte[] threeByteInstruction) {
        int opcode =  (threeByteInstruction[0] & 0xFF) >>> 3; // first 5 bits of first byte
        int opMetadata = threeByteInstruction[0] & 0x07; // last 3 bits of first byte

        // Next two bytes
        int address = BinaryUtility.getIntFromBytes(new byte[] {threeByteInstruction[1], threeByteInstruction[2]});

        // Used a switch statement with sequential integers as this has o(1) lookup speed.
        switch(opcode) {
            case 0: storeRelative(opMetadata, address); break; // Store Relative Opcode: 0, 00000
            case 1: storeAbsolute(opMetadata, address); break; // Store Absolute Opcode: 1, 00001
            case 2: loadRelative(opMetadata, address); break; // Load Relative: 2, 00010
            case 3: loadAbsolute(opMetadata, address); break; // Load Absolute: 3, 00011
            case 4: add(opMetadata, threeByteInstruction); break; // Add: 4, 00100
            case 5: subtract(opMetadata, threeByteInstruction); break; // Subtract: 5, 00101
            case 6: multiply(opMetadata, threeByteInstruction); break; // Multiply: 6, 00110
            case 7: divide(opMetadata, threeByteInstruction); break; // Divide: 7, 00111
            case 8: break;
            case 9: break;
            case 10: break;
            case 11: break;
            case 12: break;
            case 13: break;
            case 14: break;
            case 15: break;
            case 16: break;
            case 17: break;
            case 18: break;
            case 19: break;
            case 20: break;
            case 21: break;
            case 22: break;
            case 23: break;
            case 24: break;
            case 25: break;
            case 26: break;
            case 27: break;
            case 28: break;
            case 29: break;
            case 30: break;
            case 31: break;
        }
    }

    private void storeAbsolute(int metadata, int absoluteAddress) {
        int toStore = registerBank[metadata];
        Machine.getInstance().getBusing().setMemoryInteger(
                absoluteAddress,
                toStore
        );
        cache.setIntAtAddress(absoluteAddress, toStore);
    }
    private void storeRelative(int metadata, int relativeAddress) {
        storeAbsolute(metadata, programRootAddress + relativeAddress);
    }
    private void loadAbsolute(int metadata, int absoluteAddress) {
        registerBank[metadata] = cache.getIntAtAddress(absoluteAddress);
    }
    private void loadRelative(int metadata, int relativeAddress) {
        loadAbsolute(metadata, programRootAddress + relativeAddress);
    }
    private void add(int metadata, byte[] fullInstruction) {
        registerBank[metadata] =
                registerBank[Byte.toUnsignedInt(fullInstruction[1])] +
                registerBank[Byte.toUnsignedInt(fullInstruction[2])];
    }
    private void subtract(int metadata, byte[] fullInstruction) {
        registerBank[metadata] =
                registerBank[Byte.toUnsignedInt(fullInstruction[1])] -
                        registerBank[Byte.toUnsignedInt(fullInstruction[2])];
    }
    private void multiply(int metadata, byte[] fullInstruction) {
        registerBank[metadata] =
                registerBank[Byte.toUnsignedInt(fullInstruction[1])] *
                        registerBank[Byte.toUnsignedInt(fullInstruction[2])];
    }
    private void divide(int metadata, byte[] fullInstruction) {
        registerBank[metadata] =
                registerBank[Byte.toUnsignedInt(fullInstruction[1])] /
                        registerBank[Byte.toUnsignedInt(fullInstruction[2])];
    }
}
