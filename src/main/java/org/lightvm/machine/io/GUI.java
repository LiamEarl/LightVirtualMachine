package org.lightvm.machine.io;
import lombok.Getter;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class GUI extends JPanel implements Runnable, KeyListener {

    /*
        Visual memory is made up of bytes where each byte is a color value.
        Our eyes are not as sensitive to blue light as they are to red and green, so:
        000(red) 000(green) 00(blue)
        red and green get 3 bits each and blue gets only 2 bits. This results in 256
        different possible color values.
    */
    @Getter
    private final byte[] visualMemory;

    private final List<Character> printQueue = new CopyOnWriteArrayList<>();
    private final List<Character> keyInputQueue = new CopyOnWriteArrayList<>();
    private final AtomicBoolean needsRepaint = new AtomicBoolean(true);
    private boolean displaying = true;
    private final int width;
    private final int height;
    private final Map<Integer, Integer> byteRGBToIntRGBMap = new HashMap<>();
    private final BufferedImage img;

    public GUI(int displayWidth, int displayHeight) {
        visualMemory = new byte[displayWidth * displayHeight];

        Arrays.fill(visualMemory, (byte) 0);

        width = displayWidth;
        height = displayHeight;
        img = new BufferedImage(width*3, height*3, BufferedImage.TYPE_INT_RGB);

        for(int i = 0; i < 255; i++) {
            byteRGBToIntRGBMap.put(i, byteRGBtoIntRGB(i));
        }
    }

    public void addPrintQueue(char token) {
        printQueue.add(token);
    }

    public void addPrintQueue(int integer) {
        char[] strVal = Integer.toString(integer).toCharArray();
        for(int i = 0; i < strVal.length; i++) {
            printQueue.add(strVal[i]);
        }
    }

    public void setPixel(int index, byte colorValue) {
        needsRepaint.set(true);
        visualMemory[index] = colorValue;
    }

    public byte getFirstChar() {
        if(keyInputQueue.isEmpty()) return (byte) 0;
        return (byte) keyInputQueue.removeFirst().charValue();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(img, 0, 0, null);
    }

    @Override
    public void run() {
        while(displaying) {
            try {
                Thread.sleep(32);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            for(Character c : printQueue) {
                if(c == '~') System.out.println();
                else {
                    System.out.print(c);
                }
                printQueue.removeFirst();
            }

            if(!needsRepaint.get()) continue;

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int actualX = x * 3;
                    int actualY = y * 3;
                    int col = byteRGBtoIntRGB(Byte.toUnsignedInt(visualMemory[(y * width) + x]));
                    for(int i = 0; i < 3; i++) {
                        for(int j = 0; j < 3; j++) {
                            img.setRGB(
                                    actualX + j,
                                    actualY + i,
                                    col
                            );
                        }
                    }
                }
            }
            repaint();
            needsRepaint.set(false);
        }
    }

    private int byteRGBtoIntRGB(int rgb) {
        int unsigned = rgb & 0xFF;       // convert to unsigned

        int r = (unsigned >> 5) & 0b111;   // top 3 bits
        int g = (unsigned >> 2) & 0b111;   // middle 3 bits
        int b =  unsigned       & 0b11;    // bottom 2 bits

        r = (r * 255) / 7;   // 3-bit -> 8-bit
        g = (g * 255) / 7;   // 3-bit -> 8-bit
        b = (b * 255) / 3;   // 2-bit -> 8-bit

        return (r << 16) | (g << 8) | b;
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        keyInputQueue.add(e.getKeyChar());
    }

    @Override
    public void keyReleased(KeyEvent e) {}
}
