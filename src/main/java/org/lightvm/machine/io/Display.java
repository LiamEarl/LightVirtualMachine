package org.lightvm.machine.io;
import lombok.Getter;

public class Display {

    /*
        Visual memory is made up of bytes where each byte is a color value.
        Our eyes are not as sensitive to blue light as they are to red and green, so:
        000(red) 000(green) 00(blue)
        red and green get 3 bits each and blue gets only 2 bits. This results in 256
        different possible color values.
    */
    @Getter
    private final byte[] visualMemory;
    public Display(int width, int height) {
        visualMemory = new byte[width * height];
    }
    public void setPixel(int index, byte colorValue) {

    }
}
