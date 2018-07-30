/*
package com.huivip.gpsspeedwidget.utils;

public final class KuwoDES {
    private static final int DES_MODE_DECRYPT = 1;
    private static final int DES_MODE_ENCRYPT = 0;
    private static long L;
    private static long R;
    public static final byte[] SECRET_KEY = "ylzsxkwm".getBytes();
    public static final int SECRET_KEY_LENGTH = SECRET_KEY.length;
    private static int SOut;
    private static final int[] g_arrayE;
    private static final int[] g_arrayIP;
    private static final int[] g_arrayIP_1;
    private static final int[] g_arrayLs;
    private static final long[] g_arrayLsMask;
    private static final long[] g_arrayMask = {1L, 2L, 4L, 8L, 16L, 32L, 64L, 128L, 256L, 512L, 1024L, 2048L, 4096L, 8192L, 16384L, 32768L, 65536L, 131072L, 262144L, 524288L, 1048576L, 2097152L, 4194304L, 8388608L, 16777216L, 33554432L, 67108864L, 134217728L, 268435456L, 536870912L, 1073741824L, 2147483648L, 4294967296L, 8589934592L, 17179869184L, 34359738368L, 68719476736L, 137438953472L, 274877906944L, 549755813888L, 1099511627776L, 2199023255552L, 4398046511104L, 8796093022208L, 17592186044416L, 35184372088832L, 70368744177664L, 140737488355328L, 281474976710656L, 562949953421312L, 1125899906842624L, 2251799813685248L, 4503599627370496L, 9007199254740992L, 18014398509481984L, 36028797018963968L, 72057594037927936L, 144115188075855872L, 288230376151711744L, 576460752303423488L, 1152921504606846976L, 2305843009213693952L, 4611686018427387904L, Long.MIN_VALUE};
    private static final int[] g_arrayP;
    private static final int[] g_arrayPC_1;
    private static final int[] g_arrayPC_2;
    private static final char[][] g_matrixNSBox;
    private static long out;
    private static byte[] pR = new byte[8];
    private static int[] pSource;
    private static int sbi;
    private static int t;

    static {
        g_arrayIP = new int[]{57, 49, 41, 33, 25, 17, 9, 1, 59, 51, 43, 35, 27, 19, 11, 3, 61, 53, 45, 37, 29, 21, 13, 5, 63, 55, 47, 39, 31, 23, 15, 7, 56, 48, 40, 32, 24, 16, 8, 0, 58, 50, 42, 34, 26, 18, 10, 2, 60, 52, 44, 36, 28, 20, 12, 4, 62, 54, 46, 38, 30, 22, 14, 6};
        g_arrayE = new int[]{31, 0, 1, 2, 3, 4, -1, -1, 3, 4, 5, 6, 7, 8, -1, -1, 7, 8, 9, 10, 11, 12, -1, -1, 11, 12, 13, 14, 15, 16, -1, -1, 15, 16, 17, 18, 19, 20, -1, -1, 19, 20, 21, 22, 23, 24, -1, -1, 23, 24, 25, 26, 27, 28, -1, -1, 27, 28, 29, 30, 31, 30, -1, -1};
        char[] arrayOfChar1 = {14, 4, 3, 15, 2, 13, 5, 3, 13, 14, 6, 9, 11, 2, 0, 5, 4, 1, 10, 12, 15, 6, 9, 10, 1, 8, 12, 7, 8, 11, 7, 0, 0, 15, 10, 5, 14, 4, 9, 10, 7, 8, 12, 3, 13, 1, 3, 6, 15, 12, 6, 11, 2, 9, 5, 0, 4, 2, 11, 14, 1, 7, 8, 13};
        char[] arrayOfChar2 = {10, 13, 1, 11, 6, 8, 11, 5, 9, 4, 12, 2, 15, 3, 2, 14, 0, 6, 13, 1, 3, 15, 4, 10, 14, 9, 7, 12, 5, 0, 8, 7, 13, 1, 2, 4, 3, 6, 12, 11, 0, 13, 5, 14, 6, 8, 15, 2, 7, 10, 8, 15, 4, 9, 11, 5, 9, 0, 14, 3, 10, 7, 1, 12};
        char[] arrayOfChar3 = {12, 9, 0, 7, 9, 2, 14, 1, 10, 15, 3, 4, 6, 12, 5, 11, 1, 14, 13, 0, 2, 8, 7, 13, 15, 5, 4, 10, 8, 3, 11, 6, 10, 4, 6, 11, 7, 9, 0, 6, 4, 2, 13, 1, 9, 15, 3, 8, 15, 3, 1, 14, 12, 5, 11, 0, 2, 12, 14, 7, 5, 10, 8, 13};
        g_matrixNSBox = new char[][]{arrayOfChar1, {15, 0, 9, 5, 6, 10, 12, 9, 8, 7, 2, 12, 3, 13, 5, 2, 1, 14, 7, 8, 11, 4, 0, 3, 14, 11, 13, 6, 4, 1, 10, 15, 3, 13, 12, 11, 15, 3, 6, 0, 4, 10, 1, 7, 8, 4, 11, 14, 13, 8, 0, 6, 2, 15, 9, 5, 7, 1, 10, 12, 14, 2, 5, 9}, arrayOfChar2, {7, 10, 1, 15, 0, 12, 11, 5, 14, 9, 8, 3, 9, 7, 4, 8, 13, 6, 2, 1, 6, 11, 12, 2, 3, 0, 5, 14, 10, 13, 15, 4, 13, 3, 4, 9, 6, 10, 1, 12, 11, 0, 2, 5, 0, 13, 14, 2, 8, 15, 7, 4, 15, 1, 10, 7, 5, 6, 12, 11, 3, 8, 9, 14}, {2, 4, 8, 15, 7, 10, 13, 6, 4, 1, 3, 12, 11, 7, 14, 0, 12, 2, 5, 9, 10, 13, 0, 3, 1, 11, 15, 5, 6, 8, 9, 14, 14, 11, 5, 6, 4, 1, 3, 10, 2, 12, 15, 0, 13, 2, 8, 5, 11, 8, 0, 15, 7, 14, 9, 4, 12, 7, 10, 9, 1, 13, 6, 3}, arrayOfChar3, {4, 1, 3, 10, 15, 12, 5, 0, 2, 11, 9, 6, 8, 7, 6, 9, 11, 4, 12, 15, 0, 3, 10, 5, 14, 13, 7, 8, 13, 14, 1, 2, 13, 6, 14, 9, 4, 1, 2, 14, 11, 13, 5, 0, 1, 10, 8, 3, 0, 11, 3, 5, 9, 4, 15, 2, 7, 8, 12, 15, 10, 7, 6, 12}, {13, 7, 10, 0, 6, 9, 5, 15, 8, 4, 3, 10, 11, 14, 12, 5, 2, 11, 9, 6, 15, 12, 0, 3, 4, 1, 14, 13, 1, 2, 7, 8, 1, 2, 12, 15, 10, 4, 0, 3, 13, 14, 6, 9, 7, 8, 9, 6, 15, 1, 5, 12, 3, 10, 14, 5, 8, 7, 11, 0, 4, 13, 2, 11}};
        g_arrayP = new int[]{15, 6, 19, 20, 28, 11, 27, 16, 0, 14, 22, 25, 4, 17, 30, 9, 1, 7, 23, 13, 31, 26, 2, 8, 18, 12, 29, 5, 21, 10, 3, 24};
        g_arrayIP_1 = new int[]{39, 7, 47, 15, 55, 23, 63, 31, 38, 6, 46, 14, 54, 22, 62, 30, 37, 5, 45, 13, 53, 21, 61, 29, 36, 4, 44, 12, 52, 20, 60, 28, 35, 3, 43, 11, 51, 19, 59, 27, 34, 2, 42, 10, 50, 18, 58, 26, 33, 1, 41, 9, 49, 17, 57, 25, 32, 0, 40, 8, 48, 16, 56, 24};
        g_arrayPC_1 = new int[]{56, 48, 40, 32, 24, 16, 8, 0, 57, 49, 41, 33, 25, 17, 9, 1, 58, 50, 42, 34, 26, 18, 10, 2, 59, 51, 43, 35, 62, 54, 46, 38, 30, 22, 14, 6, 61, 53, 45, 37, 29, 21, 13, 5, 60, 52, 44, 36, 28, 20, 12, 4, 27, 19, 11, 3};
        g_arrayPC_2 = new int[]{13, 16, 10, 23, 0, 4, -1, -1, 2, 27, 14, 5, 20, 9, -1, -1, 22, 18, 11, 3, 25, 7, -1, -1, 15, 6, 26, 19, 12, 1, -1, -1, 40, 51, 30, 36, 46, 54, -1, -1, 29, 39, 50, 44, 32, 47, -1, -1, 43, 48, 38, 55, 33, 52, -1, -1, 45, 41, 49, 35, 28, 31, -1, -1};
        g_arrayLs = new int[]{1, 1, 2, 2, 2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 1};
        g_arrayLsMask = new long[]{0L, 1048577L, 3145731L};
        out = 0L;
        pSource = new int[2];
    }

    private static long BitTransform(int[] paramArrayOfInt, int paramInt, long paramLong) {
        long l2=0L;
        for (int i =0; i < paramInt; i++) {
            if (paramArrayOfInt[i] >= 0) {
                if ((g_arrayMask[paramArrayOfInt[i]] & paramLong) != 0L) {
                    l2|=g_arrayMask[i];
                }
            }
        }
        return l2;
    }

    private static long DES64(long[] paramArrayOfLong, long paramLong) {
        out = BitTransform(g_arrayIP, 64, paramLong);
        pSource[0] = ((int) (out & 0xFFFFFFFFL));
        pSource[1] = ((int) ((out & 0xFFFFFFFF00000000L) >> 32));
        int i = 0;
        while (i < 16) {
            R = pSource[1];
            R = BitTransform(g_arrayE, 64, R);
            R ^= paramArrayOfLong[i];
            int j = 0;
            while (j < 8) {
                pR[j] = ((byte) (int) (0xFF & R >> j * 8));
                j += 1;
            }
            SOut = 0;
            for (sbi = 7; sbi >= 0; sbi -= 1) {
                SOut <<= 4;
                SOut |= g_matrixNSBox[sbi][pR[sbi]];
            }
            R = SOut;
            R = BitTransform(g_arrayP, 32, R);
            L = pSource[0];
            pSource[0] = pSource[1];
            pSource[1] = ((int) (L ^ R));
            i += 1;
        }
        t = pSource[0];
        pSource[0] = pSource[1];
        pSource[1] = t;
        out = pSource[1] << 32 & 0xFFFFFFFF00000000L | 0xFFFFFFFF & pSource[0];
        out = BitTransform(g_arrayIP_1, 64, out);
        return out;
    }

    private static void DESSubKeys(long paramLong, long[] paramArrayOfLong, int paramInt) {
        int j = 0;
        paramLong = BitTransform(g_arrayPC_1, 56, paramLong);
        int i = 0;
        while (i < 16) {
            long l = g_arrayLsMask[g_arrayLs[i]];
            int k = g_arrayLs[i];
            paramLong = (paramLong & (g_arrayLsMask[g_arrayLs[i]] ^ 0xFFFFFFFFFFFFFFFFL)) >> g_arrayLs[i] | (l & paramLong) << 28 - k;
            paramArrayOfLong[i] = BitTransform(g_arrayPC_2, 64, paramLong);
            i += 1;
        }
        if (paramInt == 1) {
            paramInt = j;
            while (paramInt < 8) {
                paramLong = paramArrayOfLong[paramInt];
                paramArrayOfLong[paramInt] = paramArrayOfLong[(15 - paramInt)];
                paramArrayOfLong[(15 - paramInt)] = paramLong;
                paramInt += 1;
            }
        }
    }

    public static byte[] encrypt(byte[] paramArrayOfByte1, int paramInt1, byte[] paramArrayOfByte2, int paramInt2) {
        long l = 0L;
        paramInt2 = 0;
        while (paramInt2 < 8) {
            l |= paramArrayOfByte2[paramInt2] << paramInt2 * 8;
            paramInt2 += 1;
        }
        int j;
        long[] arrayOfLong;
        try {
            j = paramInt1 / 8;
            arrayOfLong = new long[16];
            paramInt2 = 0;
        } finally {
        }
        Object localObject = new long[j];
        paramInt2 = 0;
        break label257;
        label63:
        paramArrayOfByte2 = new long[((j + 1) * 8 + 1) / 8];
        DESSubKeys(l, arrayOfLong, 0);
        paramInt2 = 0;
        while (paramInt2 < j) {
            paramArrayOfByte2[paramInt2] = DES64(arrayOfLong, localObject[paramInt2]);
            paramInt2 += 1;
        }
        localObject = new byte[paramInt1 - j * 8];
        System.arraycopy(paramArrayOfByte1, j * 8, localObject, 0, paramInt1 - j * 8);
        l = 0L;
        paramInt2 = 0;
        for (; ; ) {
            paramArrayOfByte2[j] = DES64(arrayOfLong, l);
            paramArrayOfByte1 = new byte[paramArrayOfByte2.length * 8];
            paramInt2 = 0;
            paramInt1 = 0;
            int i;
            while (paramInt1 < paramArrayOfByte2.length) {
                i = 0;
                while (i < 8) {
                    paramArrayOfByte1[paramInt2] = ((byte) (int) (0xFF & paramArrayOfByte2[paramInt1] >> i * 8));
                    paramInt2 += 1;
                    i += 1;
                }
                paramInt1 += 1;
            }
            return paramArrayOfByte1;
            while (paramInt2 < 16) {
                arrayOfLong[paramInt2] = 0L;
                paramInt2 += 1;
            }
            break;
            label257:
            while (paramInt2 < j) {
                i = 0;
                while (i < 8) {
                    localObject[paramInt2] |= paramArrayOfByte1[(paramInt2 * 8 + i)] << i * 8;
                    i += 1;
                }
                paramInt2 += 1;
            }
            break label63;
            while (paramInt2 < paramInt1 % 8) {
                l |= localObject[paramInt2] << paramInt2 * 8;
                paramInt2 += 1;
            }
        }
    }
}
*/
