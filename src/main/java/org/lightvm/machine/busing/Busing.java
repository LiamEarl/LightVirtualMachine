package org.lightvm.machine.busing;

import org.lightvm.machine.io.Display;
import org.lightvm.machine.io.Keyboard;
import org.lightvm.machine.storage.RandomAccessMemory;
import org.lightvm.machine.storage.SolidStateDrive;

public class Busing {
    private final RandomAccessMemory ram;
    private final SolidStateDrive ssd;
    private final Keyboard keyboard;
    private final Display display;

    public Busing(RandomAccessMemory ram, SolidStateDrive ssd, Keyboard keyboard, Display display) {
        this.ram = ram;
        this.ssd = ssd;
        this.keyboard = keyboard;
        this.display = display;
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
            display.setPixel(vMemoryAddress + i, ram.getByte(memoryAddress + i));
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
            display.addPrintQueue(getIntFromMemory(sourceMemAddress+(i*4)));
            if(i != numInts - 1) display.addPrintQueue(',');
        }
    }

    public void transferCharsMemToPrintQueue(int sourceMemAddress, int numChars) {
        for(int i = 0; i < numChars; i++) {
            display.addPrintQueue((char)ram.getByte(sourceMemAddress+i));
        }
    }

    public static class Builder {
        private RandomAccessMemory ram;
        private SolidStateDrive ssd;
        private Keyboard keyboard;
        private Display display;

        public Busing build() {
            if(ram == null || ssd == null || keyboard == null || display == null)
                throw new IllegalArgumentException("All busing fields must be set before you call build().");

            return new Busing(ram, ssd, keyboard, display);
        }

        public Builder setRAM(RandomAccessMemory ram) {
            this.ram = ram;
            return this;
        }
        public Builder setSSD(SolidStateDrive ssd) {
            this.ssd = ssd;
            return this;
        }
        public Builder setKeyboard(Keyboard keyboard) {
            this.keyboard = keyboard;
            return this;
        }
        public Builder setDisplay(Display display) {
            this.display = display;
            return this;
        }
    }
}
