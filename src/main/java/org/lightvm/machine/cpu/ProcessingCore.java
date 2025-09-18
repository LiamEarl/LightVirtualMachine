package org.lightvm.machine.cpu;
import org.lightvm.machine.Machine;
import org.lightvm.machine.busing.Busing;
import org.lightvm.utility.BinaryUtility;

public class ProcessingCore extends Thread {
    private final int[] registerBank;
    private final Cache cache;
    private int programRootAddress;
    private int instructionAddress;
    private final int clockSpeedHertz;
    private boolean ticking = false;
    private long tickCount = 0L;

    private static final int[] instructionByteSizes = new int[] {
            3, // Code 0, Store Relative
            3, // Code 1, Store Absolute
            3, // Code 2, Load Relative
            3, // Code 3, Load Absolute
            2, // Code 4, Add
            2, // Code 5, Subtract
            2, // Code 6, Multiply
            2, // Code 7, Divide
            4, // Code 8, Branch
            4, // Code 9, Conditional Branch
    };

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

    private void executeInstruction(int instructionAddress) {
//        int opcode =  (fullInstruction[0] & 0xFF) >>> 3; // first 5 bits of first byte
////        int opMetadata = fullInstruction[0] & 0x07; // last 3 bits of first byte
////
////        // Next two bytes
////        int address = BinaryUtility.getIntFromBytes(new byte[] {fullInstruction[1], fullInstruction[2]});
//
//        // Used a switch statement with sequential integers as this has o(1) lookup speed.
//        switch(opcode) {
//            // Store Relative: 0, 00000
//            // First five bits code
//            // Next three bits is the register to take the number from
//            // Next two bytes contains the offset from the program root address that the number will be stored in
//            // Instruction Format: code(00000)r1(000)|relative address(00000000|00000000)
//            case 0: storeRelative(opMetadata, address); break;
//
//            // Store Absolute: 1, 00001
//            // First five bits code
//            // Next three bits is the register to take the number from
//            // Next two bytes contains the absolute address that the number will be stored in
//            // Instruction Format: code(00000)r1(000)|address(00000000|00000000)
//            case 1: storeAbsolute(opMetadata, address); break;
//
//            // Load Relative: 2, 00010
//            // First five bits code
//            // Next three bits is the register to load the number into
//            // Next two bytes contains the relative address that the number will be taken from
//            // Instruction Format: code(00000)r1(000)|address(00000000|00000000)
//            case 2: loadRelative(opMetadata, address); break;
//
//            // Load Absolute: 3, 00011
//            // First five bits code
//            // Next three bits is the register to load the number into
//            // Next two bytes contains the Absolute address that the number will be taken from
//            // Instruction Format: code(00000)r1(000)|address(00000000|00000000)
//            case 3: loadAbsolute(opMetadata, address); break;
//
//            // Add: 4, 00100
//            // First 5 bits code
//            // Next three bits is the register to store the value to
//            // Next 3 bits is the register containing the first number to add
//            // Next 3 bits is the register containing the second number to add
//            // Next 2 bits is wasted
//            // Instruction format: code(00000)r3(000)|r1(000)r2(000) Wasted bits:(00)
//            case 4:
//                //int firstNumber = registerBank[]
//                //add(opMetadata, fullInstruction);
//                break;
////            case 5: subtract(opMetadata, fullInstruction); break; // Subtract: 5, 00101
////            case 6: multiply(opMetadata, fullInstruction); break; // Multiply: 6, 00110
////            case 7: divide(opMetadata, fullInstruction); break; // Divide: 7, 00111
////            case 8: break;
//            case 9: break;
//            case 10: break;
//            case 11: break;
//            case 12: break;
//            case 13: break;
//            case 14: break;
//            case 15: break;
//            case 16: break;
//            case 17: break;
//            case 18: break;
//            case 19: break;
//            case 20: break;
//            case 21: break;
//            case 22: break;
//            case 23: break;
//            case 24: break;
//            case 25: break;
//            case 26: break;
//            case 27: break;
//            case 28: break;
//            case 29: break;
//            case 30: break;
//            case 31: break;
//        }
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
    private void branch(int metadata, byte[] fullInstruction) {
        int travelAddress = (Byte.toUnsignedInt(fullInstruction[1]) << 8) +
                Byte.toUnsignedInt(fullInstruction[2]);
        if(metadata == 0) travelAddress += programRootAddress;


    }
}
