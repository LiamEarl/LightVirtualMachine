package org.lightvm;

import org.lightvm.machine.Machine;

public class LightVMApplication {
    public static void main(String[] args) {
        Machine.getInstance().powerOn();
    }
}