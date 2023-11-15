package dk.itu.raven.ksquared;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class K2Raster {
    static final int k = 2;

    private int maxval;
    public BitMap Tree;
    public DAC LMax;
    public DAC LMin;
    public List<ArrayList<Integer>> parent;
    private int n;

    public K2Raster(int[][] M, int n) {
        // ensures n is a power of k even if the n from the input is not
        int original_n = n;
        int real_n = 1;
        while (real_n < n) {
            real_n *= k;
        }
        this.n = real_n;
        
        int maxLevel =  1+(int) Math.ceil(Math.log(n) / Math.log(k));
        List<BitMap> T = new ArrayList<>(maxLevel);
        List<ArrayList<Integer>> Vmax = new ArrayList<ArrayList<Integer>>(maxLevel);
        List<ArrayList<Integer>> Vmin = new ArrayList<ArrayList<Integer>>(maxLevel);
        int pmax[] = new int[maxLevel];
        int pmin[] = new int[maxLevel];
        parent = new ArrayList<>(maxLevel);
        for (int i = 0; i < maxLevel; i++) {
            T.add(new BitMap(40));
            Vmax.add(new ArrayList<>());
            Vmin.add(new ArrayList<>());
            parent.add(new ArrayList<>());
        }
        int[] res = Build(M, this.n, original_n, 1, 0, 0, T, Vmin, Vmax, pmax, pmin, parent, 0);
        Vmax.get(0).add(res[0]);
        Vmin.get(0).add(res[1]);
        maxval = res[0];

        int size_max = 0;
        int size_min = 0;
        for (int i = 1; i < maxLevel; i++) {
            size_max += pmax[i];
            size_min += pmin[i];
            System.out.println(pmin[i]);
        }

        System.out.println(size_max);
        System.out.println(size_min);

        int[] LMaxList = new int[size_max];
        int[] LMinList = new int[size_min];

        Tree = new BitMap(size_max);
        int bitmapIndex = 0;
        // maxLevel-1 is a bit funky :-(
        for (int i = 0; i < maxLevel-1; i++) {
            for (int j = 0; j < pmax[i]; j++) {
                if (T.get(i).isSet(j)) {
                    Tree.set(++bitmapIndex);
                } else {
                    Tree.unset(++bitmapIndex);
                }
            }
        }

        int imax = 0, imin = 0;
        for (int i = 1; i < maxLevel; i++) {
            for (int j = 0; j < pmax[i]; j++) {
                LMaxList[imax++] = (Vmax.get(i - 1).get(parent.get(i).get(j)) - Vmax.get(i).get(j));
                if (T.get(i).isSet(j)) {
                    LMinList[imin++] = (Vmin.get(i).get(j) - Vmin.get(i - 1).get(parent.get(i).get(j)));
                }
            }
        }

        LMax = new DAC(LMaxList);
        LMin = new DAC(LMinList);
    }

    private static int[] Build(int[][] M, int n, int original_n, int level, int row, int column, List<BitMap> T,
            List<ArrayList<Integer>> Vmin, List<ArrayList<Integer>> Vmax, int[] pmax, int[] pmin,
            List<ArrayList<Integer>> parent, int caller) {
        int min, max;
        min = Integer.MAX_VALUE;
        max = 0;
        boolean lastlevel = n == k;
        int nKths = n / k;
        for (int i = 0; i < k; i++) {
            for (int j = 0; j < k; j++) {
                if (lastlevel) {
                    int matrix_value;
                    if (row+i >= original_n || column+j >= original_n) {
                        matrix_value = 0;
                    } else {
                        matrix_value = M[row + i][column + j];
                    }
                    if (min > matrix_value) {
                        min = matrix_value;
                    }
                    if (max < matrix_value) {
                        max = matrix_value;
                    }
                    Vmax.get(level).add(pmax[level], matrix_value);
                    T.get(level).unset(pmax[level]);
                    parent.get(level).add(pmax[level], caller);
                    pmax[level]++;
                } else {
                    int[] res = Build(M, nKths, original_n, level + 1, row + i * nKths, column + j * nKths, T, Vmin, Vmax, pmax,
                            pmin, parent,
                            T.get(level).size());
                    int childMax = res[0];
                    int childMin = res[1];
                    Vmax.get(level).add(pmax[level], childMax);
                    if (childMin != childMax) {
                        Vmin.get(level).add(pmin[level], childMin);
                        pmin[level]++;
                        T.get(level).set(pmax[level]);
                    } else {
                        Vmin.get(level).add(pmin[level], childMin);
                        pmin[level]++;
                        T.get(level).unset(pmax[level]);
                    }
                    parent.get(level).add(pmax[level], caller);
                    pmax[level]++;
                    if (min > childMin) {
                        min = childMin;
                    }
                    if (max < childMax) {
                        max = childMax;
                    }
                }
            }
        }

        if (min == max) {
            pmax[level] = pmax[level] - k * k;
            pmin[level-1] = pmin[level-1] - 1; // actual real improvement of the K^2 Raster data-structure 😱
            T.get(level).setSize(pmax[level]);
        }

        return new int[] { max, min };
    }

    /**
     * 🤬
     * @param n size of the matrix
     * @param r the row to access
     * @param c the column to access
     * @param z only God knows what this does
     * @param maxval the max value in the matrix
     * @return the value from the matrix at index {@code (r,c)}
     */
    private int getCell(int n, int r, int c, int z, int maxval) {
        int nKths = (n/k);
        z = this.Tree.rank(z) * k * k;
        z = z + (r / nKths) * k + (c / nKths);
        int val = LMax.accessFT(z + 1); //😡
        maxval = maxval - val;
        if (z >= Tree.size() || Tree.getOrZero(z + 1) == 0) //😡
            return maxval;
        return getCell(nKths, r % nKths, c % nKths, z, maxval);
    }

    /**
     * @param n size of the matrix
     * @param r the row to access
     * @param c the column to access
     * @return the value from the matrix at index {@code (r,c)}
     */
    public int getCell(int r, int c) {
        return getCell(this.n, r, c, -1, this.maxval);
    }

    private class IntPointer {
        int index;
    }

    private void getWindow(int n, int r1, int r2, int c1, int c2, int z, int maxval, int[] out, IntPointer index) {
        int nKths = (n/k);
        z = this.Tree.rank(z) * k * k;
        int initialI = r1 / nKths;
        int lastI = r2 / nKths;
        int initialJ = c1 / nKths;
        int lastJ = c2 / nKths;

        int r1p, r2p, c1p, c2p, maxvalp, zp;

        for (int i = initialI; i <= lastI ; i++) {
            if(i == initialI) r1p = r1 % nKths;
            else r1p = 0; 
            
            if(i == lastI) r2p = r2 % nKths;
            else r2p = nKths - 1;

            for (int j = initialJ; j <= lastJ ; j++) {
                if(j == initialJ) c1p = c1 % nKths;
                else c1p = 0; 
                
                if(j == lastJ) c2p = c2 % nKths;
                else c2p = nKths - 1;

                zp = z + i * k + j;
                maxvalp = maxval - LMax.accessFT(zp + 1);
                if (zp + 1 >= Tree.size() || Tree.getOrZero(zp + 1) == 0) {
                    int times = ((r2p-r1p) + 1) * ((c2p-c1p) + 1);
                    for (int l = 0; l < times; l++) {
                        // System.out.println("used index " + index.index);
                        out[index.index++] = maxvalp;
                    }
                } else {
                    getWindow(nKths, r1p, r2p, c1p, c2p, zp, maxvalp, out, index);
                }
                
            }
        }
    }

    /**
     * 
     * @param n size of the matrix
     * @param r1 row number for the top left corner of window
     * @param r2 row number for the bottom right corner of window
     * @param c1 column number for the top left corner of window
     * @param c2 column number for the bottom right corner of window
     * @return a window of the matrix
     */
    public int[] getWindow(int r1, int r2, int c1, int c2) {
        int returnSize = (r2-r1 + 1) * (c2-c1 + 1);
        int[] out = new int[returnSize];
        getWindow(this.n, r1, r2, c1, c2, -1, this.maxval, out, new IntPointer());
    
        return out;
    }
}
