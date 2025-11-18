package org.lightvm.machine;

import lombok.Getter;
import org.lightvm.machine.busing.Busing;
import org.lightvm.machine.cpu.ProcessingCore;
import org.lightvm.machine.io.GUI;
import org.lightvm.machine.storage.RandomAccessMemory;
import org.lightvm.machine.storage.SolidStateDrive;

import javax.swing.*;

/**
 * Machine class that represents a self-contained virtual machine.
 * Uses the Singleton design pattern as this project does not utilize multiple machines.
 */
public class Machine {
    private final ProcessingCore processingCore;
    private final RandomAccessMemory randomAccessMemory;
    private final SolidStateDrive solidStateDrive;
    private final GUI gui;
    @Getter
    private final Busing busing;

    /* 65536 bytes (65.5kb) of ram, and 65536 bytes of disk (65.5kb) one short can represent all of these addresses.
       Shorts can represent a value from 0 to 65535, resulting in 65536 combinations
       16 lines of cache each containing 64 bytes meaning 1024 bytes (1kb) of cache.
       16 register slots
       1hz clock speed (for testing) */

    @Getter
    private static final Machine instance = new Machine();

    public Machine() {
        solidStateDrive = new SolidStateDrive();
        randomAccessMemory = new RandomAccessMemory();
        processingCore = new ProcessingCore();

        int displayWidth = 256, displayHeight = 256;
        JFrame frame = new JFrame("LearlOS");
        gui = new GUI(displayWidth, displayHeight);
        frame.add(gui);
        frame.setSize(displayWidth*3, displayHeight*3);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.addKeyListener(gui);
        Thread displayThread = new Thread(gui);
        displayThread.start();

        busing = new Busing.BusBuilder()
                .setRAM(randomAccessMemory)
                .setSSD(solidStateDrive)
                .setDisplay(gui)
                .setCPU(processingCore)
                .build();
    }

    public void powerOn() {
        processingCore.clockCPU();
    }
}
