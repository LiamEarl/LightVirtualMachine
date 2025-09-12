package org.lightvm.machine.storage;

public class SolidStateDrive {
    public byte[] diskData;
    public byte getByte(int address) {
        return diskData[address];
    }
    public void setByte(int address, byte data) {
        diskData[address] = data;
    }
}
