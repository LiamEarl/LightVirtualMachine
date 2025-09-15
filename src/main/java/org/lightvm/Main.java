package org.lightvm;

import org.lightvm.machine.Machine;
import org.lightvm.utility.BinaryUtility;

public class Main {
    public static void main(String[] args) {
        System.out.println(BinaryUtility.getIntFromBytes(new byte[] {(byte) 255, (byte) 3}));
        Machine.getInstance().powerOn();
    }
}