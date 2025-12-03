package org.lightvm.machine.storage;

import java.io.BufferedReader;
import java.io.FileReader;

public class SolidStateDrive {
    public byte[] diskData;
    public SolidStateDrive() {
        diskData = new byte[1024*64];
        try (BufferedReader reader = new BufferedReader(new FileReader("src/main/resources/code_c.txt"))) {
            String line;
            int i = 0;
            while ((line = reader.readLine()) != null) {
                diskData[i] = (byte) Integer.parseInt(line, 2);
                i ++;
            }
        } catch (Exception e) {
            System.err.println("Error reading the file: " + e.getMessage());
        }
    }
    public byte getByte(int address) {
        return diskData[address];
    }
    public void setByte(int address, byte data) {
        diskData[address] = data;
    }
}
