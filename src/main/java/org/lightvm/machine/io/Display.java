package org.lightvm.machine.io;
import lombok.Getter;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class Display extends JPanel implements Runnable{

    /*
        Visual memory is made up of bytes where each byte is a color value.
        Our eyes are not as sensitive to blue light as they are to red and green, so:
        000(red) 000(green) 00(blue)
        red and green get 3 bits each and blue gets only 2 bits. This results in 256
        different possible color values.
    */
    @Getter
    private final byte[] visualMemory;

    private boolean displaying = true;
    private int width, height;

    private final BufferedImage img;
    public Display(int displayWidth, int displayHeight) {
        visualMemory = new byte[displayWidth * displayHeight];
        width = displayWidth;
        height = displayHeight;
        img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        // Set a few pixels for demo
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int color = (x ^ y) & 0xFF; // cool XOR pattern
                img.setRGB(x, y, (color << 16) | (color << 8) | color);
            }
        }

    }
    public void setPixel(int index, byte colorValue) {

    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(img, 0, 0, null);
    }

    @Override
    public void run() {
        while(displaying) {
            //for(int i = 0; i < visualMemory.length; i+)
        }
    }
}
