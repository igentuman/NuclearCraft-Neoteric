package igentuman.nc.util.math;

import java.util.Arrays;

public class ArrayPacker {
    
    public static int pack(int[] values) {
        int packedValue = 0;
        for (int value : values) {
            packedValue |= (1 << value);
        }
        return packedValue;
    }
    
    public static int[] unpack(int packedValue) {
        int[] unpackedValues = new int[6];
        int index = 0;
        for (int i = 0; i < 6; i++) {
            if ((packedValue & (1 << i)) != 0) {
                unpackedValues[index++] = i;
            }
        }
        return Arrays.copyOf(unpackedValues, index);
    }
    
}