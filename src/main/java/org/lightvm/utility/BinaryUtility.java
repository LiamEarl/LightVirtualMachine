package org.lightvm.utility;

public class BinaryUtility {
    public static byte[] getBytesFromShort(Short twoByteNumber) {
        byte mostSignificantByte = (byte) ((twoByteNumber >>> 8) & 0xFF);
        byte leastSignificantByte = (byte) (twoByteNumber & 0xFF);
        return new byte[] {mostSignificantByte, leastSignificantByte};
    }

    public static byte[] getBytesFromInt(int integer) {
        return new byte[] {
                (byte) (integer >>> 24),
                (byte) (integer >>> 16),
                (byte) (integer >>> 8),
                (byte) integer
        };
    }

    public static int getIntFromBytes(byte[] numberToConvert) {
        if(numberToConvert.length != 4) throw new IllegalArgumentException("BinaryUtility.getIntFromBytes needs a 4 byte long array 'numberToConvert'");
        int total = 0;
        for(int i = 0; i < numberToConvert.length; i++) {
            // Add each byte's true value to the total by bit shifting an integer
            total += Byte.toUnsignedInt(numberToConvert[i]) << 8 * (numberToConvert.length - i - 1);
        }
        return total;
    }
}
