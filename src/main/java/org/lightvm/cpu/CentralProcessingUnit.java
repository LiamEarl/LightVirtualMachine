package org.lightvm.cpu;
import org.lightvm.storage.RandomAccessMemory;
public class CentralProcessingUnit {
    private Register accumulator;
    private RandomAccessMemory memory;
    public CentralProcessingUnit() {
        memory = new RandomAccessMemory(1024); // 65536 bytes, a short can represent all of these addresses.
        accumulator = new Register();

    }
}
