package dk.itu.raven.ksquared;

import static dk.itu.raven.ksquared.Basics.*;

/**
 * DAC
 * https://github.com/sladra/DACs/blob/master/src/dacs.c
 */
public class DAC {
    public static class FTRep {
        int listLength;
        byte nLevels;
        int tamCode;
        int[] levels;
        int[] levelsIndex;
        int[] iniLevel;
        int[] rankLevels;
        BitRank bS;
        int[] base;
        short[] base_bits;
        int[] tablebase;
        int tamtablebase;
    }

    static final int epsilon = 1;

    private static short[] Optimize(int[] cf) {
        int m = cf.length - 1;
        int[] s = new int[m + 1];
        int[] l = new int[m + 1];
        int[] b = new int[m + 1];

        for (int t = m; t >= 0; t--) {
            int minSize = Integer.MAX_VALUE;
            int minPos = m;
            for (int i = t + 1; i <= m; i++) {
                int currentSize = s[i] + cf[t] * ((i - t) + (1 + epsilon));
                if (minSize > currentSize) {
                    minSize = currentSize;
                    minPos = i;
                }
            }
            if (minSize < cf[t] * ((m + 1) - t)) {
                s[t] = minSize;
                l[t] = l[minPos] + 1;
                b[t] = minPos - t;
            } else {
                s[t] = cf[t] * ((m + 1) - t);
                l[t] = 1;
                b[t] = (m + 1) - t;
            }
        }
        int L = l[0];
        int t = 0;
        short[] result = new short[L];
        for (int k = 1; k <= L; k++) {
            result[k - 1] = (short) b[t]; // unsafe cast
            t = t + b[t];
        }

        return result;
    }

    public static FTRep createFT(int[] list) {
        int listLength = list.length;
        FTRep rep = new FTRep();
        int[] levelSizeAux;
        int[] cont;
        int[] contB;

        short[] kvalues;

        rep.listLength = listLength;
        int i;
        int j, k;
        int value, newvalue;
        int bits_BS_len = 0;

        kvalues = Optimize(list);
        int nkvalues = kvalues.length;

        short kval;
        int oldval = 0;
        int newval = 0;

        i = 0;
        int multval = 1;
        do {
            oldval = newval;
            if (i >= nkvalues) {
                kval = (short) (1 << (kvalues[nkvalues - 1])); // unsafe cast
            } else
                kval = (short) (1 << (kvalues[i])); // unsafe cast
            multval *= kval;
            newval = oldval + multval;

            i++;
        } while (oldval < newval);

        rep.tamtablebase = i;
        rep.tablebase = new int[rep.tamtablebase];
        levelSizeAux = new int[rep.tamtablebase];
        cont = new int[rep.tamtablebase];
        contB = new int[rep.tamtablebase];

        oldval = 0;
        newval = 0;
        multval = 1;
        for (i = 0; i < rep.tamtablebase; i++) {
            oldval = newval;
            if (i >= nkvalues) {
                kval = (short) (1 << (kvalues[nkvalues - 1])); // unsafe cast
            } else
                kval = (short) (1 << (kvalues[i])); // unsafe cast
            multval *= kval;
            newval = oldval + multval;
            rep.tablebase[i] = oldval;
        }

        for (i = 0; i < rep.tamtablebase; i++) {
            levelSizeAux[i] = 0;
        }

        for (i = 0; i < listLength; i++) {
            value = list[i];
            for (j = 0; j < rep.tamtablebase; j++)
                if (value >= rep.tablebase[j])
                    levelSizeAux[j]++;
        }

        j = 0;

        while ((j < rep.tamtablebase) && (levelSizeAux[j] != 0)) {
            j++;
        }
        rep.nLevels = (byte) j;

        rep.levelsIndex = new int[(rep.nLevels + 1)];
        bits_BS_len = 0;

        rep.base = new int[rep.nLevels];
        rep.base_bits = new short[rep.nLevels];

        for (i = 0; i < rep.nLevels; i++) {
            if (i >= nkvalues) {
                rep.base[i] = 1 << (kvalues[nkvalues - 1]);
                rep.base_bits[i] = kvalues[nkvalues - 1];
            } else {
                rep.base[i] = 1 << (kvalues[i]);
                rep.base_bits[i] = kvalues[i];
            }
        }

        int tamLevels = 0;

        tamLevels = 0;
        for (i = 0; i < rep.nLevels; i++)
            tamLevels += rep.base_bits[i] * levelSizeAux[i];

        rep.iniLevel = new int[rep.nLevels];
        rep.tamCode = tamLevels;

        int indexLevel = 0;
        rep.levelsIndex[0] = 0;
        for (j = 0; j < rep.nLevels; j++) {
            rep.levelsIndex[j + 1] = rep.levelsIndex[j] + levelSizeAux[j];
            rep.iniLevel[j] = indexLevel;
            cont[j] = rep.iniLevel[j];
            indexLevel += levelSizeAux[j] * rep.base_bits[j];
            contB[j] = rep.levelsIndex[j];

        }

        rep.levels = new int[(tamLevels / W + 1)];

        bits_BS_len = rep.levelsIndex[rep.nLevels - 1] + 1;

        int[] bits_BS = new int[(bits_BS_len / W + 1)];
        for (i = 0; i < ((bits_BS_len) / W + 1); i++)
            bits_BS[i] = 0;
        for (i = 0; i < listLength; i++) {
            value = list[i];
            j = rep.nLevels - 1;

            while (j >= 0) {
                if (value >= rep.tablebase[j]) {

                    newvalue = value - rep.tablebase[j];

                    for (k = 0; k < j; k++) {

                        bitwrite(rep.levels, cont[k], rep.base_bits[k], newvalue % rep.base[k]);
                        cont[k] += rep.base_bits[k];
                        contB[k]++;

                        newvalue = newvalue / rep.base[k];
                    }
                    k = j;

                    bitwrite(rep.levels, cont[j], rep.base_bits[j], newvalue % rep.base[j]);
                    cont[j] += rep.base_bits[j];
                    contB[j]++;
                    if (j < rep.nLevels - 1) {
                        bitset(bits_BS, contB[j] - 1);

                    }

                    break;
                }
                j--;
            }

        }

        bitset(bits_BS, bits_BS_len - 1);

        rep.bS = new BitRank(bits_BS, bits_BS_len, (char) 1, 20);

        rep.rankLevels = new int[rep.nLevels];
        for (j = 0; j < rep.nLevels; j++)
            rep.rankLevels[j] = rep.bS.Rank(rep.levelsIndex[j] - 1);

        return rep;
    }

    public static int accessFT(FTRep listRep, int param) {
        int mult = 0;
        int j;
        int partialSum = 0;
        int ini = param - 1;
        int nLevels = listRep.nLevels;
        int[] level;
        int readByte;
        int cont, pos, rankini;

        partialSum = 0;
        j = 0;
        level = listRep.levels;

        pos = listRep.levelsIndex[j] + ini;

        mult = 0;

        cont = listRep.iniLevel[j] + ini * listRep.base_bits[j];

        readByte = bitread(level, cont, listRep.base_bits[j]);
        if (nLevels == 1) {
            return readByte;
        }
        while ((bitget(listRep.bS.data, pos)) == 0) {

            rankini = listRep.bS.Rank(listRep.levelsIndex[j] + ini - 1) - listRep.rankLevels[j];
            ini = ini - rankini;

            partialSum = partialSum + (readByte << mult);

            mult += listRep.base_bits[j];
            j++;

            cont = listRep.iniLevel[j] + ini * listRep.base_bits[j];
            pos = listRep.levelsIndex[j] + ini;

            readByte = bitread(level, cont, listRep.base_bits[j]);

            if (j == nLevels - 1) {
                break;
            }

        }

        partialSum = partialSum + (readByte << mult) + listRep.tablebase[j];

        return partialSum;

    }

}
