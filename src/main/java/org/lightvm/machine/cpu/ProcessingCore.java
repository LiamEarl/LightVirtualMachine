package org.lightvm.machine.cpu;
import org.lightvm.machine.Machine;
import org.lightvm.utility.BinaryUtility;

import javax.crypto.Mac;

public class ProcessingCore extends Thread {
    private final int[] registerBank;
    private final Cache cache;
    private int programRootAddress;
    private int instructionAddress;
    private final int clockSpeedHertz;
    private boolean ticking = false;
    private long tickCount = 0L;

    private static final int[] instructionByteSizes = new int[] {
            1, // Code 0, Idle
            3, // Code 1 Store Relative
            3, // Code 2, Store Absolute
            3, // Code 3, Load Relative
            3, // Code 4, Load Absolute
            2, // Code 5, Add
            2, // Code 6, Subtract
            2, // Code 7, Multiply
            2, // Code 8, Divide
            2, // Code 9, and
            2, // Code 10, or
            2, // Code 11, not
            4, // Code 12, Branch
            5, // Code 13, set
            // Code 14
            1, //Code 15, Halt
    };
    private static final String[] opcodeNames = new String[]{
        "Idle",
        "Store Relative",
        "Store Absolute",
        "Load Relative",
        "Load Absolute",
        "Add",
        "Subtract",
        "Multiply",
        "Divide",
        "And",
        "Or",
        "Not",
        "Branch",
        "Set"
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
        cache.initializeCacheLines();
        int millisDelay = 1000 / clockSpeedHertz;
        long start = System.currentTimeMillis();
        while(ticking) {
            tickCount ++;
//            long timePassed = System.currentTimeMillis() - start;
            //System.out.printf("Tick #: %d, Time Passed: %d seconds.\n", tickCount, timePassed /1000);

           // executeInstruction();

//            try {
//                Thread.sleep(millisDelay);
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
        }
    }

    private int getAssistingAddress(int targetAddress) {
        return BinaryUtility.getIntFromBytes(new byte[] {
                cache.getByteAtAddress(targetAddress + 1),
                cache.getByteAtAddress(targetAddress)
        });
    }

    private void executeInstruction() {
        byte instructionRoot = cache.getByteAtAddress(instructionAddress);

        int opcode =  (instructionRoot & 0b11111111) >>> 4; // first 4 bits of first byte
        int opMetadata = (instructionRoot & 0b00001111); // last 4 bits of first byte
        byte nextInstructionByte = cache.getByteAtAddress(instructionAddress + 1);

        System.out.println(instructionAddress + " " + opcode + " " + opcodeNames[opcode]);

        // Used a switch statement with sequential integers as this has o(1) lookup speed.
        switch(opcode) {
            case 0: break; // Idle

            // Save To External: 1, 0001
            // First four bits code
            // Next four bits contains what location externally the bytes will be sent to
            // Next two bytes contains the address in memory to copy from
            // Next two bytes contains the address in the external system to copy to
            // Next two bytes contains the amount of bytes to copy over
            // Instruction Format: code(0000)metadata(0000)|memAddress(00000000|00000000)|externalAddress(00000000|00000000)|numBytesToCopy(00000000|00000000)
            case 1: saveToExternal(opMetadata, getAssistingAddress(instructionAddress + 1), getAssistingAddress(instructionAddress + 3), getAssistingAddress(instructionAddress + 5)); break;

            // Save To External: 2, 0010
            // First four bits code
            // Next four bits contains what location externally the bytes will be sent to
            // Next two bytes contains the address in memory to copy from
            // Next two bytes contains the address in the external system to copy to
            // Next two bytes contains the amount of bytes to copy over
            // Instruction Format: code(0000)metadata(0000)|memAddress(00000000|00000000)|externalAddress(00000000|00000000)|numBytesToCopy(00000000|00000000)
            case 2: loadFromExternal(opMetadata, getAssistingAddress(instructionAddress + 1)); break;

            // Load Relative: 2, 0010
            // First four bits code
            // Next four bits is the register to load the number into
            // Next two bytes contains the relative potentialAddress that the number will be taken from
            // Instruction Format: code(0000)r1(0000)|potentialAddress(00000000|00000000)
            case 3: loadMem(opMetadata, getAssistingAddress(instructionAddress + 1)); break;

            // Load Absolute: 3, 0011
            // First four bits code
            // Next four bits is the register to load the number into
            // Next two bytes contains the Absolute potentialAddress that the number will be taken from
            // Instruction Format: code(0000)r1(0000)|potentialAddress(00000000|00000000)
            case 4: storeMem(opMetadata, getAssistingAddress(instructionAddress + 1)); break;

            // Add: 4, 0100
            // First four bits code
            // Next four bits is the register to store the value to
            // Next four bits is the register containing the first number to add
            // Next four bits is the register containing the second number to add
            // Instruction format: code(0000)r3(0000)|r1(0000)r2(0000)
            case 5: add(opMetadata, (nextInstructionByte & 0xFF) >>> 4, nextInstructionByte & 0b00001111);break;

            // Subtract: 5, 0101
            // First four bits code
            // Next four bits is the register to store the value to
            // Next four bits is the register containing the first number
            // Next four bits is the register containing the second number
            // Instruction format: code(0000)r3(0000)|r1(0000)r2(0000)
            case 6: subtract(opMetadata, (nextInstructionByte & 0xFF) >>> 4, nextInstructionByte & 0b00001111);break; // Subtract: 5, 00101

            // Multiply: 6, 0110
            // First four bits code
            // Next four bits is the register to store the value to
            // Next four bits is the register containing the first number
            // Next four bits is the register containing the second number
            // Instruction format: code(0000)r3(0000)|r1(0000)r2(0000)
            case 7: multiply(opMetadata, (nextInstructionByte & 0xFF) >>> 4, nextInstructionByte & 0b00001111); break; // Multiply: 6, 00110

            // Divide (integer division): 7, 0111
            // First four bits code
            // Next four bits is the register to store the value to
            // Next four bits is the register containing the first number
            // Next four bits is the register containing the second number
            // Instruction format: code(0000)r3(0000)|r1(0000)r2(0000)
            case 8: divide(opMetadata, (nextInstructionByte & 0xFF) >>> 4, nextInstructionByte & 0b00001111); break; // Divide: 7, 00111

            case 9: and(opMetadata, (nextInstructionByte & 0xFF) >>> 4, nextInstructionByte & 0b00001111); break; // Divide: 7, 00111

            case 10: or(opMetadata, (nextInstructionByte & 0xFF) >>> 4, nextInstructionByte & 0b00001111); break; // Divide: 7, 00111

            case 11: not(opMetadata, (nextInstructionByte & 0xFF) >>> 4); break; // Divide: 7, 00111

            // Branch: 8, 1000
            // First four bits code
            // Next four bits is the register to store the value to
            // Next four bits is the register containing the first number
            // Next four bits is the register containing the second number
            // Instruction format: code(0000)branchType(0000)|r1(0000)r2(0000)|addressToBranch(00000000|00000000)
            case 12: branch(opMetadata, (nextInstructionByte & 0xFF) >>> 4, nextInstructionByte & 0b00001111, getAssistingAddress(instructionAddress+2));break;

            // Set: 9, 1001
            // First four bits code
            // Next four bits is the register to store the value to
            // Next four bytes is the literal integer to store
            // Instruction format: code(0000)register(0000)|numToStore(00000000|00000000|00000000|00000000)
            case 13: set(opMetadata, cache.getIntAtAddress(instructionAddress + 1)); break;
            case 14: break;
            case 15: halt(); break;
        }

        if(opcode != 9) {
            instructionAddress += instructionByteSizes[opcode];
        }
    }
    private void saveToExternal(int metadata, int memoryAddress, int externalAddress, int bytesToCopy) {
        switch(metadata) {
            case 0: // Write to output
                for(int i = 0; i < bytesToCopy; i++) {
                    //Machine.getInstance().getBusing().
                }
            case 1: break;
            case 2: break;
            case 3: break;
        }
    }
    private void loadFromExternal(int metadata, int memoryAddress, int externalAddress, int bytesToCopy) {

    }
    private void storeMem(int metadata, int absoluteAddress) {
        int toStore = registerBank[metadata];
        Machine.getInstance().getBusing().setMemoryInteger(
                absoluteAddress,
                toStore
        );
        cache.setIntAtAddress(absoluteAddress, toStore);
    }
    private void loadMem(int metadata, int absoluteAddress) {
        registerBank[metadata] = cache.getIntAtAddress(absoluteAddress);
    }
    private void add(int metadata, int register1Index, int register2Index) {
        registerBank[metadata] =
                registerBank[register1Index] +
                registerBank[register2Index];
    }
    private void subtract(int metadata, int register1Index, int register2Index) {
        registerBank[metadata] =
                registerBank[register1Index] -
                        registerBank[register2Index];
    }
    private void multiply(int metadata, int register1Index, int register2Index) {
        registerBank[metadata] =
                registerBank[register1Index] *
                        registerBank[register2Index];
    }
    private void divide(int metadata, int register1Index, int register2Index) {
        registerBank[metadata] =
                registerBank[register1Index] /
                        registerBank[register2Index];
    }
    private void and(int metadata, int register1Index, int register2Index) {
        registerBank[metadata] = registerBank[register1Index] & registerBank[register2Index];
    }
    private void or(int metadata, int register1Index, int register2Index) {
        registerBank[metadata] = registerBank[register1Index] | registerBank[register2Index];
    }
    private void not(int metadata, int registerIndex) {
        registerBank[metadata] = ~registerBank[registerIndex];
    }
    private void branch(int metadata, int register1Index, int register2Index, int travelAddress) {
        switch(metadata) {
            case 0: instructionAddress = travelAddress; break;
            case 1:
                if(registerBank[register1Index] == registerBank[register2Index])
                    instructionAddress = travelAddress;
                break;
            case 2:
                if(registerBank[register1Index] > registerBank[register2Index])
                    instructionAddress = travelAddress;
                break;
            case 3:
                if(registerBank[register1Index] < registerBank[register2Index])
                    instructionAddress = travelAddress;
                break;
            case 4: instructionAddress = programRootAddress + travelAddress; break;
            case 5:
                if(registerBank[register1Index] == registerBank[register2Index])
                    instructionAddress = programRootAddress + travelAddress;
                break;
            case 6:
                if(registerBank[register1Index] > registerBank[register2Index])
                    instructionAddress = programRootAddress + travelAddress;
                break;
            case 7:
                if(registerBank[register1Index] < registerBank[register2Index])
                    instructionAddress = programRootAddress + travelAddress;
                break;
        }
    }
    private void set(int metadata, int numToSet) {
        registerBank[metadata] = numToSet;
    }
    private void halt() {
        ticking = false;
    }
}
