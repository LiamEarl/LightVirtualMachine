package org.lightvm.machine.storage;

import java.io.BufferedReader;
import java.io.FileReader;

public class SolidStateDrive {
    public byte[] diskData;
    public SolidStateDrive() {
        diskData = new byte[1024*64];
        try {
            BufferedReader codeReader = new BufferedReader(new FileReader("src/main/resources/code_c.txt"));
            BufferedReader winIMGReader = new BufferedReader(new FileReader("src/main/resources/youwin.txt"));
            BufferedReader loseIMGReader = new BufferedReader(new FileReader("src/main/resources/youlose.txt"));

            String line;
            int i = 0;
            while ((line = codeReader.readLine()) != null) {
                diskData[i] = (byte) Integer.parseInt(line, 2);
                i ++;
            }

            int lineNum = 0;
            while ((line = winIMGReader.readLine()) != null) {
                for(int j = 0; j < line.length(); j++) {
                    if(line.charAt(j) != '1') continue;
                    diskData[3000 + (lineNum * 256) + 108 + j] = (byte) 28;
                }
                lineNum ++;
            }

            lineNum = 0;
            while ((line = loseIMGReader.readLine()) != null) {
                for(int j = 0; j < line.length(); j++) {
                    if(line.charAt(j) != '1') continue;
                    diskData[4280 + (lineNum * 256) + 105 + j] = (byte) 224;
                }
                lineNum ++;
            }
        } catch (Exception e) {
            System.err.println("Error reading the file: code.txt" + e.getMessage());
        }

    }
    public byte getByte(int address) {
        return diskData[address];
    }
    public void setByte(int address, byte data) {
        diskData[address] = data;
    }
}
