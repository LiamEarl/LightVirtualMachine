package org.lightvm;

import lombok.Getter;
import org.lightvm.cpu.CentralProcessingUnit;
import org.lightvm.storage.RandomAccessMemory;
import org.lightvm.storage.SolidStateDrive;

/**
 * Machine class that represents a self-contained virtual machine.
 * Uses the Singleton design pattern as this project does not utilize multiple machines.
 */
public class Machine {
    private final CentralProcessingUnit centralProcessingUnit;
    private final RandomAccessMemory randomAccessMemory;
    private SolidStateDrive solidStateDrive;

    /* 32768 bytes (32.7kb) of ram, and 32768 bytes of disk (32.7kb) one short can represent all of these addresses.
       Shorts can represent a value from 0 to 65535, resulting in 65536 combinations
       16 lines of cache each containing 64 bytes meaning 1024 bytes (1kb) of cache.
       16 register slots
       1hz clock speed (for testing) */

    @Getter
    private static final Machine instance = new Machine(512, 16, 16, 1);

    public Machine(int num64BytesRam, int num64ByteCacheLines, int registerBankSize, int cpuClockSpeed) {
        randomAccessMemory = new RandomAccessMemory(num64BytesRam);
        centralProcessingUnit = new CentralProcessingUnit(randomAccessMemory, num64ByteCacheLines, registerBankSize, cpuClockSpeed);
    }

    public void powerOn() {
        centralProcessingUnit.clockCPU();
    }
}
