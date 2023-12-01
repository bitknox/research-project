package dk.itu.raven.ksquared;

public abstract class Basics {
    public static int bits(int n) {
        int b = 0;
        while (n > 0) {
            b++;
            n >>= 1;
        }
        return b;
    }

    // EEEW, yuckey 🤮
    public static char __popcount_tab[] = {
            0, 1, 1, 2, 1, 2, 2, 3, 1, 2, 2, 3, 2, 3, 3, 4, 1, 2, 2, 3, 2, 3, 3, 4, 2, 3, 3, 4, 3, 4, 4, 5,
            1, 2, 2, 3, 2, 3, 3, 4, 2, 3, 3, 4, 3, 4, 4, 5, 2, 3, 3, 4, 3, 4, 4, 5, 3, 4, 4, 5, 4, 5, 5, 6,
            1, 2, 2, 3, 2, 3, 3, 4, 2, 3, 3, 4, 3, 4, 4, 5, 2, 3, 3, 4, 3, 4, 4, 5, 3, 4, 4, 5, 4, 5, 5, 6,
            2, 3, 3, 4, 3, 4, 4, 5, 3, 4, 4, 5, 4, 5, 5, 6, 3, 4, 4, 5, 4, 5, 5, 6, 4, 5, 5, 6, 5, 6, 6, 7,
            1, 2, 2, 3, 2, 3, 3, 4, 2, 3, 3, 4, 3, 4, 4, 5, 2, 3, 3, 4, 3, 4, 4, 5, 3, 4, 4, 5, 4, 5, 5, 6,
            2, 3, 3, 4, 3, 4, 4, 5, 3, 4, 4, 5, 4, 5, 5, 6, 3, 4, 4, 5, 4, 5, 5, 6, 4, 5, 5, 6, 5, 6, 6, 7,
            2, 3, 3, 4, 3, 4, 4, 5, 3, 4, 4, 5, 4, 5, 5, 6, 3, 4, 4, 5, 4, 5, 5, 6, 4, 5, 5, 6, 5, 6, 6, 7,
            3, 4, 4, 5, 4, 5, 5, 6, 4, 5, 5, 6, 5, 6, 6, 7, 4, 5, 5, 6, 5, 6, 6, 7, 5, 6, 6, 7, 6, 7, 7, 8,
    };

    public static int W = 32;

    public static int mask31 = 0x0000001F;

    public static int popcount(int x) {
        return __popcount_tab[(x >> 0) & 0xff] + __popcount_tab[(x >> 8) & 0xff] + __popcount_tab[(x >> 16) & 0xff]
                + __popcount_tab[(x >> 24) & 0xff];
    }

    public static short bitShiftLeftC(short shiftBy) {
        if (shiftBy >= 32) return 0;
        return (short) (1 << shiftBy);
    }
    public static int bitShiftLeftC(int num, int shiftBy) {
        if (shiftBy >= 32) return 0;
        return (num << shiftBy);
    }
    
    public static int bitShiftRightC(int num, int shiftBy) {
        if (shiftBy >= 32) return 0;
        return (num >> shiftBy);
    }

    public static void bitset(int[] e, int p) {
        e[(p) / W] |= bitShiftLeftC(1, (p) % W);
    }

    public static int bitget(int[] e, int p) {
        return bitShiftRightC(e[(p) / W], ((p) % W)) & 1;
    }

    public static int bitread(int[] e, int p, int len) {
        int idx = 0;
        int answ;
        idx += p / W;
        p %= W;
        answ = bitShiftRightC(e[idx], p); 
        if (len == W) {
            if (p != 0)
                answ |= bitShiftLeftC(e[idx + 1],(W - p));
        } else {
            if (p + len > W)
                answ |= bitShiftLeftC(e[idx + 1],(W - p));
            answ &= bitShiftLeftC(1,len) - 1;
        }
        return answ;
    }

    public static void bitwrite(int[] e, int p, int len, int s) {
        int idx = 0;
        idx += p / W;
        p %= W;
        if (len == W) {
            e[idx] |= (e[idx] & (bitShiftLeftC(1,p) - 1)) | bitShiftLeftC(s,p);
            if (p == 0)
                return;
            idx++;
            e[idx] = (e[idx] & ~(bitShiftLeftC(1,p) - 1)) | bitShiftRightC(s,W-p);
        } else {
            if (p + len <= W) {
                e[idx] = (e[idx] & ~bitShiftLeftC((bitShiftLeftC(1,len) - 1), p)) | bitShiftLeftC(s,p);
                return;
            }
            e[idx] = (e[idx] & (bitShiftLeftC(1,p) - 1)) | (bitShiftLeftC(s, p));
            idx++;
            len -= W - p;
            e[idx] = (e[idx] & ~(bitShiftLeftC(1, len) - 1)) | bitShiftRightC(s, W - p); 
        }
    }
}
