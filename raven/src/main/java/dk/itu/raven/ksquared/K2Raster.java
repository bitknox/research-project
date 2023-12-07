package dk.itu.raven.ksquared;

import java.util.ArrayList;
import java.util.List;

import dk.itu.raven.util.BitMap;
import dk.itu.raven.util.GoodArrayList;
import dk.itu.raven.util.GoodIntArrayList;
import dk.itu.raven.util.Pair;
import dk.itu.raven.util.matrix.Matrix;
import dk.itu.raven.util.Logger;

public class K2Raster {
    public static final int k = 2;

    // intermediate datastructures
    private List<GoodIntArrayList> VMax;
    private List<GoodIntArrayList> VMin;
    private List<BitMap> T;
    private int[] pmax;
    private int[] pmin;
    private Matrix M;

    private int maxval;
    private int minval;
    public BitMap Tree;
    // public DAC LMax;
    // public DAC LMin;
    private int[] LMax;
    private int[] LMin;
    // private int[] VMaxList;
    // private int[] VMinList;

    private int n;
    private int[] prefixsum;
    private int original_n, original_m;

    /**
     * bulds a K^2 Raster data-structure for an n*m matrix (meaning a 2-dimensional
     * array with {@code n} rows and {@code m} columns)
     * 
     * @param M the raw matrix data
     */
    public K2Raster(Matrix M) {
        int n = M.getHeight();
        int m = M.getWidth();

        this.M = M;

        // ensures n is a power of k even if the n from the input is not
        this.original_n = n;
        this.original_m = m;
        int real_n = 1;
        while (real_n < n || real_n < m) {
            real_n *= k;
        }
        this.n = real_n;

        int maxLevel = 1 + (int) Math.ceil(Math.log(Math.max(n, m)) / Math.log(k));
        T = new ArrayList<>(maxLevel);
        VMax = new ArrayList<GoodIntArrayList>(maxLevel);
        VMin = new ArrayList<GoodIntArrayList>(maxLevel);
        pmax = new int[maxLevel];
        pmin = new int[maxLevel];
        for (int i = 0; i < maxLevel; i++) {
            T.add(new BitMap(40));
            VMax.add(new GoodIntArrayList());
            VMin.add(new GoodIntArrayList());

        }

        Pair<Integer, Integer> res = Build(this.n, 1, 0, 0);
        maxval = res.first;
        minval = res.second;
        VMax.get(0).set(0, maxval);
        VMin.get(0).set(0, minval);

        int size_max = 0;
        int size_min = 0;
        for (int i = 1; i < maxLevel; i++) {
            size_max += pmax[i];
            size_min += pmin[i];
        }

        Logger.log("size_max: " + size_max);
        Logger.log("size_min: " + size_min);

        int[] LMaxList = new int[size_max + 1];
        int[] LMinList = new int[size_min + 1];

        Tree = new BitMap(Math.max(1, size_max));
        int bitmapIndex = 0;

        for (int i = 0; i < maxLevel - 1; i++) {
            for (int j = 0; j < pmax[i]; j++) {
                if (T.get(i).isSet(j)) {
                    Tree.set(++bitmapIndex);
                } else {
                    Tree.unset(++bitmapIndex);
                }
            }
        }
        pmax[0] = 1;

        if (maxval != minval) {
            Tree.set(0);
            T.get(0).set(0);
            pmin[0] = 1;
        } else {
            Tree.unset(0);
            T.get(0).unset(0);
            pmin[0] = 0;
        }

        prefixsum = new int[size_max + 1];
        prefixsum[0] = 0;
        for (int i = 1; i < size_max + 1; i++) {
            prefixsum[i] = prefixsum[i - 1] + Tree.getOrZero(i);
        }

        int imax = 0, imin = 0;

        for (int i = 0; i < maxLevel - 2; i++) {
            int internalNodeCount = 0;
            int innerInternalNodeCount = 0;
            for (int j = 0; j < pmax[i]; j++) {
                if (T.get(i).isSet(j)) {
                    int start = internalNodeCount * k * k;
                    for (int l = start; l < start + k * k; l++) {
                        if (T.get(i + 1).isSet(l)) {
                            LMinList[imin++] = Math.abs(
                                    VMin.get(i + 1).get(innerInternalNodeCount) - VMin.get(i).get(internalNodeCount));
                            innerInternalNodeCount++;
                        }
                    }
                    internalNodeCount++;
                }
            }
        }

        for (int i = 0; i < maxLevel - 1; i++) {
            int internalNodeCount = 0;
            for (int j = 0; j < pmax[i]; j++) {
                if (T.get(i).isSet(j)) {
                    int start = internalNodeCount * k * k;
                    for (int l = start; l < start + k * k; l++) {
                        LMaxList[imax++] = Math.abs(VMax.get(i).get(j) - VMax.get(i + 1).get(l));
                    }
                    internalNodeCount++;
                }
            }
        }

        VMax = null;
        VMin = null;
        T = null;
        pmax = null;
        pmin = null;
        M = null;

        // LMax = new DAC(LMaxList);
        // LMin = new DAC(LMinList);
        LMax = LMaxList;
        LMin = LMinList;
    }

    public boolean hasChildren(int index) {
        return Tree.isSet(index);
    }

    /**
     * Gets the minimum and maximum values in the K2Raster tree
     * 
     * @return an array of length 2, where the first element is the minimum value
     *         and the second element is the maximum value
     */
    public int[] getValueRange() {
        return new int[] { minval, maxval };
    }

    public int computeVMax(int parentMax, int index) {
        if (index == 0)
            return maxval;
        return parentMax - LMax[index - 1];
    }

    public int computeVMin(int parentMax, int parentMin, int index) {
        if (index == 0)
            return minval;
        if (!hasChildren(index)) {
            return computeVMax(parentMax, index);
        }
        int pref = prefixsum[index - 1];
        return parentMin + LMin[pref];
    }

    /**
     * gets the children of the node at index {@code index}
     * 
     * @param index the index of the parent node
     * @return array of indxes
     */
    public int[] getChildren(int index) {
        if (!hasChildren(index)) {
            return new int[0];
        } else {
            int numInternalNodes = prefixsum[index];
            int[] res = new int[k * k];
            for (int i = 0; i < k * k; i++) {
                res[i] = 1 + k * k * numInternalNodes + i;
            }
            return res;
        }
    }

    /**
     * 
     * @return the size of the K2Raster tree
     */
    public int getSize() {
        return this.n;
    }

    private Pair<Integer, Integer> Build(int n, int level, int row, int column) {
        int minval = Integer.MAX_VALUE;
        int maxval = 0;

        for (int i = 0; i < k; i++) {
            for (int j = 0; j < k; j++) {
                if (n == k) { // last level
                    int matrixVal = M.get(row + i, column + j);
                    if (minval > matrixVal) {
                        minval = matrixVal;
                    }
                    if (maxval < matrixVal) {
                        maxval = matrixVal;
                    }
                    VMax.get(level).set(pmax[level], matrixVal);
                    pmax[level]++;
                } else {
                    Pair<Integer, Integer> res = Build(n / k, level + 1, row + i * (n / k), column + j * (n / k));
                    VMax.get(level).set(pmax[level], res.first);
                    if (res.first != res.second) {
                        VMin.get(level).set(pmin[level], res.second);
                        pmin[level]++;
                        T.get(level).set(pmax[level]);
                    } else {
                        T.get(level).unset(pmax[level]);
                    }
                    pmax[level]++;
                    if (minval > res.second) {
                        minval = res.second;
                    }
                    if (maxval < res.first) {
                        maxval = res.first;
                    }
                }
            }
        }
        if (minval == maxval) {
            pmax[level] -= k * k;
        }

        return new Pair<>(maxval, minval);
    }

    /**
     * Use of this method is discouraged for performance reasons. Use
     * {@code getWindow}
     * instead.
     * 🤬
     * 
     * @param n      size of the matrix
     * @param r      the row to access
     * @param c      the column to access
     * @param z      only God knows what this does
     * @param maxval the max value in the matrix
     * @return the value from the matrix at index {@code (r,c)}
     */
    private int getCell(int n, int r, int c, int z, int maxval) {
        int nKths = (n / k);
        z = this.Tree.rank(z) * k * k;
        z = z + (r / nKths) * k + (c / nKths);
        int val = LMax[z]; // 😡
        maxval = maxval - val;
        // maxval = VMaxList[z];
        if (!hasChildren(z + 1)) // 😡
            return maxval;
        return getCell(nKths, r % nKths, c % nKths, z, maxval);
    }

    /**
     * @param r the row to access
     * @param c the column to access
     * @return the value from the matrix at index {@code (r,c)}
     */
    public int getCell(int r, int c) {
        return getCell(this.n, r, c, -1, this.maxval);
    }

    private void getWindow(int n, int r1, int r2, int c1, int c2, int z, int maxval, int[] out,
            IntPointer index,
            int level, List<Pair<Integer, Integer>> indexRanks) {
        int nKths = (n / k);
        Pair<Integer, Integer> indexRank = indexRanks.get(level);
        int rank = (indexRank.second + this.Tree.rank(indexRank.first + 1, z));
        indexRank.first = z;
        indexRank.second = rank;

        z = rank * k * k;
        int initialI = r1 / nKths;
        int lastI = r2 / nKths;
        int initialJ = c1 / nKths;
        int lastJ = c2 / nKths;

        int r1p, r2p, c1p, c2p, maxvalp, zp;

        for (int i = initialI; i <= lastI; i++) {
            if (i == initialI)
                r1p = r1 % nKths;
            else
                r1p = 0;

            if (i == lastI)
                r2p = r2 % nKths;
            else
                r2p = nKths - 1;

            for (int j = initialJ; j <= lastJ; j++) {
                if (j == initialJ)
                    c1p = c1 % nKths;
                else
                    c1p = 0;

                if (j == lastJ)
                    c2p = c2 % nKths;
                else
                    c2p = nKths - 1;

                zp = z + i * k + j;

                maxvalp = maxval - LMax[zp];
                // maxvalp = VMaxList[zp];
                if (!hasChildren(zp + 1)) {
                    int times = ((r2p - r1p) + 1) * ((c2p - c1p) + 1);
                    for (int l = 0; l < times; l++) {
                        out[index.val++] = maxvalp;
                    }
                } else {
                    getWindow(nKths, r1p, r2p, c1p, c2p, zp, maxvalp, out, index, level + 1, indexRanks);
                }

            }
        }
    }

    /**
     * Reads data from a window of the matrix given by the two points
     * {@code (r1,c1)} and {@code (r2,c2)}
     * 
     * @param r1 row number for the top left corner of window
     * @param r2 row number for the bottom right corner of window
     * @param c1 column number for the top left corner of window
     * @param c2 column number for the bottom right corner of window
     * @return a window of the matrix
     */
    public int[] getWindow(int r1, int r2, int c1, int c2) {
        if (r1 < 0 || r1 >= n || r2 < 0 || r2 >= n || c1 < 0 || c1 >= n || c2 < 0
                || c2 >= n)
            throw new IndexOutOfBoundsException("looked up window (" + r1 + ", " + c1 + ", " + r2 + ", " + c2
                    + ") in matrix with size (" + n + ", " + n + ")");
        int returnSize = (r2 - r1 + 1) * (c2 - c1 + 1);
        int[] out = new int[returnSize];
        int maxLevel = 1 + (int) Math.ceil(Math.log(n) / Math.log(k));
        GoodArrayList<Pair<Integer, Integer>> indexRanks = new GoodArrayList<Pair<Integer, Integer>>(maxLevel);
        for (int i = 0; i < maxLevel; i++) {
            indexRanks.set(i, new Pair<>(-1, 0));
        }
        getWindow(this.n, r1, r2, c1, c2, -1, this.maxval, out, new IntPointer(), 0, indexRanks);

        return out;
    }

    private void searchValuesInWindow(int n, int r1, int r2, int c1, int c2, int basex, int basey, int z, int maxval,
            int minval, int vb,
            int ve, int[] out,
            IntPointer index,
            int level, List<Pair<Integer, Integer>> indexRanks) {
        int nKths = (n / k); // childsize
        Pair<Integer, Integer> indexRank = indexRanks.get(level);
        int rank = (indexRank.second + this.Tree.rank(indexRank.first + 1, z));
        indexRank.first = z;
        indexRank.second = rank;

        z = rank * k * k;
        int initialI = r1 / nKths;
        int lastI = r2 / nKths;
        int initialJ = c1 / nKths;
        int lastJ = c2 / nKths;

        int r1p, r2p, c1p, c2p, maxvalp, minvalp, zp;

        for (int i = initialI; i <= lastI; i++) {
            int cbasey = basey + i * nKths;
            if (i == initialI)
                r1p = r1 % nKths;
            else
                r1p = 0;

            if (i == lastI)
                r2p = r2 % nKths;
            else
                r2p = nKths - 1;

            for (int j = initialJ; j <= lastJ; j++) {
                int cbasex = basex + j * nKths;
                if (j == initialJ)
                    c1p = c1 % nKths;
                else
                    c1p = 0;

                if (j == lastJ)
                    c2p = c2 % nKths;
                else
                    c2p = nKths - 1;

                zp = z + i * k + j;

                maxvalp = maxval - LMax[zp];
                // maxvalp = VMaxList[zp];
                boolean addCells = false;
                if (!hasChildren(zp + 1)) {
                    minvalp = maxvalp;
                    if (minvalp >= vb && maxvalp <= ve) {
                        addCells = true;
                        /* all cells meet the condition in this branch */
                    }
                } else {
                    int rank2 = this.Tree.rank(zp); // missing from and to.
                    int pref = prefixsum[rank2];
                    int min1 = LMin[pref];
                    minvalp = minval + min1;
                    if (minvalp >= vb && maxvalp <= ve) {
                        addCells = true;
                        /* all cells meet the condition in this branch */
                    }
                    if (minvalp > ve && maxvalp < vb) {
                        addCells = false;
                    }
                    if (minvalp < vb || maxvalp > ve) {
                        searchValuesInWindow(nKths, r1p, r2p, c1p, c2p, cbasex, cbasey, zp, maxvalp, minvalp, vb, ve,
                                out, index,
                                level + 1,
                                indexRanks);
                    }
                }

                int cxini = Math.max(c1, cbasex);
                int cxend = Math.max(c2, cbasex + nKths - 1);
                int cyini = Math.max(r1, cbasey);
                int cyend = Math.max(r2, cbasey + nKths - 1);

                if (addCells) {
                    System.out.println("x1: " + cxini + ", x2: " + cxend + ", y1: " + cyini + ", y2: " + cyend);
                    // System.out.println("x1: " + c1 + ", x2: " + c2 + ", y1: " + c1p + ", y2: " +
                    // c2p);
                }
            }
        }
    }

    /**
     * Reads data from a window of the matrix given by the two points
     * {@code (r1,c1)} and {@code (r2,c2)}
     * 
     * @param r1 row number for the top left corner of window
     * @param r2 row number for the bottom right corner of window
     * @param c1 column number for the top left corner of window
     * @param c2 column number for the bottom right corner of window
     * @return a window of the matrix
     */
    public int[] searchValuesInWindow(int r1, int r2, int c1, int c2, int thresholdLow, int thresholdHigh) {
        if (r1 < 0 || r1 >= n || r2 < 0 || r2 >= n || c1 < 0 || c1 >= n || c2 < 0
                || c2 >= n)
            throw new IndexOutOfBoundsException("looked up window (" + r1 + ", " + c1 + ", " + r2 + ", " + c2
                    + ") in matrix with size (" + original_n + ", " + original_m + ")");
        int returnSize = (r2 - r1 + 1) * (c2 - c1 + 1); // can be smaller.
        int[] out = new int[returnSize];
        int maxLevel = 1 + (int) Math.ceil(Math.log(n) / Math.log(k));
        GoodArrayList<Pair<Integer, Integer>> indexRanks = new GoodArrayList<Pair<Integer, Integer>>(maxLevel);
        for (int i = 0; i < maxLevel; i++) {
            indexRanks.set(i, new Pair<>(-1, 0));
        }
        searchValuesInWindow(this.n, r1, r2, c1, c2, 0, 0, -1, this.maxval, this.minval, thresholdLow, thresholdHigh,
                out,
                new IntPointer(), 0,
                indexRanks);

        return out;
    }
}
