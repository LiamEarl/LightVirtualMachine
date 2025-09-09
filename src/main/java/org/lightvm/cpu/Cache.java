package org.lightvm.cpu;

public class Cache {
    /*
    { all cache lines
        {  one cache line
            individual number
            .
            .
            last index of a cache line is always metadata for the chunk of memory it's associated with
            (in this implementation)
        },
        .
        .
    }
    */
    private int[][] cacheLines;
    public Cache(int num64ByteLines) {
        this.cacheLines = new int[num64ByteLines][17];
    }


}
