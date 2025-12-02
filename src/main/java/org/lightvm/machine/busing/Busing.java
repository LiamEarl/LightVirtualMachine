package org.lightvm.machine.busing;

import org.lightvm.machine.cpu.ProcessingCore;
import org.lightvm.machine.io.GUI;
import org.lightvm.machine.storage.RandomAccessMemory;
import org.lightvm.machine.storage.SolidStateDrive;

import java.util.Random;

public class Busing {
    private final RandomAccessMemory ram;
    private final SolidStateDrive ssd;
    private final GUI gui;
    private final ProcessingCore cpu;

    public Busing(RandomAccessMemory ram, SolidStateDrive ssd, GUI gui, ProcessingCore cpu) {
        this.ram = ram;
        this.ssd = ssd;
        this.gui = gui;
        this.cpu = cpu;
    }

    public void setMemoryByte(int address, byte byteToSet) {
        ram.setByte(address, byteToSet);
    }

    public void setMemoryInteger(int address, int intToSet) {
        ram.placeInt(address, intToSet);
    }

    public byte[] getMemoryBlock(int address) {
        return ram.getMemoryBlock(address);
    }

    public int getIntFromMemory(int address) {
        return ram.getIntAtAddress(address);
    }

    public void transferMemoryToVMem(int memoryAddress, int vMemoryAddress, int numBytesToTransfer) {
        for(int i = 0; i < numBytesToTransfer; i++) {
            gui.setPixel(vMemoryAddress + i, ram.getByte(memoryAddress + i));
        }
    }

    public void transferDiskToMemory(int diskAddress, int memoryAddress, int numBytesToTransfer) {
        for(int i = 0; i < numBytesToTransfer; i++) {
            ram.setByte(memoryAddress + i, ssd.getByte(diskAddress + i));
        }
    }

    public void transferMemoryToDisk(int diskAddress, int memoryAddress, int numBytesToTransfer) {
        for(int i = 0; i < numBytesToTransfer; i++) {
            ssd.setByte(memoryAddress + i, ram.getByte(diskAddress + i));
        }
    }

    public void transferIntsMemToPrintQueue(int sourceMemAddress, int numInts) {
        for(int i = 0; i < numInts; i++) {
            gui.addPrintQueue(getIntFromMemory(sourceMemAddress+(i*4)));
            if(i != numInts - 1) gui.addPrintQueue(',');
        }
    }

    public void transferCharsMemToPrintQueue(int sourceMemAddress, int numChars) {
        for(int i = 0; i < numChars; i++) {
            gui.addPrintQueue((char)Byte.toUnsignedInt(ram.getByte(sourceMemAddress+i)));
        }
    }

    public void loadMostRecentCharToMem(int memoryAddress) {
        ram.setByte(memoryAddress, gui.getFirstChar());
        cpu.snoopMemoryMutation(memoryAddress);
    }

    public void wipeDisplay() {
        gui.wipeScreen();
    }

    public static class BusBuilder {
        private RandomAccessMemory ram;
        private SolidStateDrive ssd;
        private GUI gui;
        private ProcessingCore cpu;

        public Busing build() {
            if(ram == null || ssd == null || gui == null || cpu == null)
                throw new IllegalArgumentException("All busing fields must be set before you call build().");

            return new Busing(ram, ssd, gui, cpu);
        }

        public BusBuilder setRAM(RandomAccessMemory ram) {
            this.ram = ram;
            return this;
        }
        public BusBuilder setSSD(SolidStateDrive ssd) {
            this.ssd = ssd;
            return this;
        }
        public BusBuilder setDisplay(GUI gui) {
            this.gui = gui;
            return this;
        }
        public BusBuilder setCPU(ProcessingCore cpu) {
            this.cpu = cpu;
            return this;
        }
    }
}
