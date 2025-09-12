package org.lightvm.machine.cpu;
import org.lightvm.machine.busing.Busing;

public class ProcessingCore extends Thread {
    private int accumulator;
    private short instructionAddress;
    private byte instruction;
    private int[] registerBank;
    private Cache cache;
    private int clockSpeedHertz;
    private boolean ticking = false;
    private long timePassed = 0L;
    private long tickCount = 0L;
    private final Busing bus;


    public ProcessingCore(int num64ByteCacheLines, int registerBankSize, int clockSpeedHertz, Busing bus) {
        this.bus = bus;
        cache = new Cache(num64ByteCacheLines, bus);
        registerBank = new int[registerBankSize];
        this.clockSpeedHertz = clockSpeedHertz;
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
