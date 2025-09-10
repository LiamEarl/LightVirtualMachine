package org.lightvm.cpu;
import org.lightvm.storage.RandomAccessMemory;

public class CentralProcessingUnit extends Thread {
    private int accumulator;
    private short instructionAddress;
    private byte instruction;
    private int[] registerBank;
    private Cache cache;
    private RandomAccessMemory memory;
    private int clockSpeedHertz;
    private boolean ticking = false;
    private long timePassed = 0L;
    private long tickCount = 0L;



    public CentralProcessingUnit(RandomAccessMemory memory, int num64ByteCacheLines, int registerBankSize, int clockSpeedHertz) {
        this.memory = memory;
        cache = new Cache(num64ByteCacheLines);
        registerBank = new int[registerBankSize];
        this.clockSpeedHertz = clockSpeedHertz;
    }

    public void clockCPU() {
        ticking = true;
        this.start();
    }

    private byte load(short address) {
//        byte[][] cacheLines = cache.getCacheLines();
//        int targetAddress = Short.toUnsignedInt(address);
//        for(int i = 0; i < cacheLines.length; i++) {
//            int cacheDiff = targetAddress - (Byte.toUnsignedInt(cacheLines[i][64]) * 64);
//
//            if(cacheDiff >= 0 && cacheDiff < 64) {
//                return cacheLines[i][cacheDiff];
//            }
//        }
//
//        cache.loadLine()
        return (byte) 10;
    }

    @Override
    public void run() {
        int millisDelay = 1000 / clockSpeedHertz;
        long start = System.currentTimeMillis();
        while(ticking) {
            tickCount ++;
            timePassed = System.currentTimeMillis() - start;
            System.out.printf("Tick #: %d, Time Passed: %d seconds.\n", tickCount, timePassed/1000);

            try {
                Thread.sleep(millisDelay);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
