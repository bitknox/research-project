package dk.itu.raven.ksquared;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import dk.itu.raven.util.BitMap;
import dk.itu.raven.util.GoodArrayList;
import dk.itu.raven.util.GoodIntArrayList;
import dk.itu.raven.util.Pair;
import dk.itu.raven.util.Tuple5;
import dk.itu.raven.util.matrix.Matrix;

public class K2Raster {
    public static final int k = 2;

    private int maxval;
    private int minval;
    public BitMap Tree;
    // public DAC LMax;
    // public DAC LMin;
    private int[] LMax;
    private int[] LMin;

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

        // ensures n is a power of k even if the n from the input is not
        this.original_n = n;
        this.original_m = m;
        int real_n = 1;
        while (real_n < n || real_n < m) {
            real_n *= k;
        }
        this.n = real_n;

        int maxLevel = 1 + (int) Math.ceil(Math.log(Math.max(n, m)) / Math.log(k));
        List<BitMap> T = new ArrayList<>(maxLevel);
        List<GoodIntArrayList> Vmax = new ArrayList<GoodIntArrayList>(maxLevel);
        List<GoodIntArrayList> Vmin = new ArrayList<GoodIntArrayList>(maxLevel);
        List<GoodIntArrayList> parentMax = new ArrayList<>(maxLevel);
        List<GoodIntArrayList> parentMin = new ArrayList<>(maxLevel);
        int pmax[] = new int[maxLevel];
        int pmin[] = new int[maxLevel];
        int bitsSet[] = new int[maxLevel];
        for (int i = 0; i < maxLevel; i++) {
            T.add(new BitMap(40));
            Vmax.add(new GoodIntArrayList());
            Vmin.add(new GoodIntArrayList());
            parentMax.add(new GoodIntArrayList());
            parentMin.add(new GoodIntArrayList());
        }

        int[] res = Build(M, this.n, original_n, original_m, 1, 0, 0, T, Vmin, Vmax, pmax, pmin, parentMax, parentMin,
                0, 0, bitsSet);
        // int[] res = BuildIterative(M, this.n, original_n, original_m, 1, 0, 0, T, Vmin, Vmax, pmax, pmin, parentMax, parentMin,
        //         0, 0, bitsSet);
        maxval = res[0];
        minval = res[1];
        Vmax.get(0).set(0, maxval);
        Vmin.get(0).set(0, minval);

        int minSize = 0;
        for (GoodIntArrayList p : Vmin) {
            minSize += p.size();
        }

        System.out.println("minSize: " + minSize);

        int size_max = 0;
        int size_min = 0;
        for (int i = 1; i < maxLevel; i++) {
            size_max += pmax[i];
            size_min += pmin[i];
        }

        System.out.println("size_max: " + size_max);
        System.out.println("size_min: " + size_min);

        int[] LMaxList = new int[size_max];
        int[] LMinList = new int[size_min];

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

        if (maxval != minval) {
            Tree.set(0);
        } else {
            Tree.unset(0);
        }

        prefixsum = new int[Tree.size()+1];
        prefixsum[0] = 0;
        for (int i = 1; i <= Tree.size(); i++) {
            prefixsum[i] = prefixsum[i - 1] + Tree.getOrZero(i);
        }

        int imax = 0, imin = 0;
        for (int i = 1; i < maxLevel; i++) {
            for (int j = 0; j < pmax[i]; j++) {
                int par = parentMax.get(i).get(j);
                int vMaxCur = Vmax.get(i).get(j);
                int vMaxLast = Vmax.get(i - 1).get(par);
                LMaxList[imax++] = Math.abs(vMaxLast - vMaxCur);

            }
        }
        for (int i = 1; i < maxLevel - 1; i++)
            for (int j = 0; j < Vmin.get(i).size(); j++) {
                int par = parentMin.get(i).get(j);
                int vminCur = Vmin.get(i).get(j);
                int vMinLast = Vmin.get(i - 1).get(par);
                LMinList[imin++] = Math.abs(vminCur - vMinLast);
            }

        // LMax = new DAC(LMaxList);
        // LMin = new DAC(LMinList);
        LMax = LMaxList;
        LMin = LMinList;

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
        if (index == 0) return maxval;
        index--;
        return parentMax - LMax[index];
    }
    
    public int computeVMin(int parentMax, int parentMin, int index) {
        if (index == 0) return minval;
        if (Tree.getOrZero(index) == 1) {
            return parentMin + LMin[prefixsum[index]-1]; // convert LMax index to the corresponding LMin index
        }
        return computeVMax(parentMax, index); // since this node has no children, VMax == VMin.
    }

    /**
     * gets the children of the node at index {@code index}
     * 
     * @param index the index of the parent node
     * @return array of indxes
     */
    public int[] getChildren(int index) {
        if (Tree.getOrZero(index) == 0) {
            return new int[0];
        } else {
            index = prefixsum[index];
            int[] res = new int[k * k];
            for (int i = 0; i < k * k; i++) {
                res[i] = 1 + k * k * index + i;
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

    private static int[] BuildIterative(Matrix M, int n, int original_n, int original_m, int level, int row, int column,
            List<BitMap> T,
            List<GoodIntArrayList> Vmin, List<GoodIntArrayList> Vmax, int[] pmax, int[] pmin,
            List<GoodIntArrayList> parentsMax, List<GoodIntArrayList> parentsMin, int parentMax, int parentMin,
            int[] bitsSet) {
        Stack<int[]> stack = new Stack<>();
        Stack<int[]> returnStack = new Stack<>();
        Stack<int[]> localStack = new Stack<>();
        stack.push(new int[] {n, level, row, column, parentMax, parentMin});
        while (!stack.empty()) {
            int[] tuple = stack.pop();
            int min, max;
            min = Integer.MAX_VALUE;
            max = 0;
            boolean lastlevel = tuple[0] == k;
            int nKths = tuple[0] / k;
            
            for (int i = 0; i < k; i++) {
                for (int j = 0; j < k; j++) {
                    int child = pmax[tuple[1]];
                    if (lastlevel) {
                        int matrix_value;
                        matrix_value = M.get(tuple[2] + i, tuple[3] + j);

                        if (min > matrix_value) {
                            min = matrix_value;
                        }
                        if (max < matrix_value) {
                            max = matrix_value;
                        }
                        T.get(tuple[1]).unset(child);
                        Vmax.get(tuple[1]).set(child, matrix_value);
                        parentsMax.get(tuple[1]).set(child, tuple[4]);
                        pmax[tuple[1]]++;
                    } else {
                        // int[] res = Build(M, nKths, original_n, original_m, level + 1, row + i * nKths, column + j * nKths,
                        //         T, Vmin, Vmax, pmax,
                        //         pmin, parentsMax, parentsMin,
                        //         T.get(level).size(), bitsSet[level], bitsSet);
                        stack.push(new int[] {nKths, tuple[1] + 1, tuple[2] + i * nKths, tuple[3] + j * nKths, T.get(tuple[1]).size(), bitsSet[tuple[1]]});
                        localStack.push(new int[] {tuple[1],tuple[4],tuple[5]});
                    }
                }
            }
            if (lastlevel) {
                if (min == max) {
                    pmax[tuple[1]] = pmax[tuple[1]] - k * k;
                    T.get(tuple[1]).setSize(pmax[tuple[1]]);
                }

                returnStack.push(new int[] {min, max});
            }
        }

        while (!localStack.empty()) {
            int min, max;
            min = Integer.MAX_VALUE;
            max = 0;
            int innerlevel = -1;
            for (int i = 0; i < k; i++) {
                for (int j = 0; j < k; j++) {
                    int[] tuple = returnStack.pop();
                    int[] localTuple = localStack.pop();
                    int childMax = tuple[0];
                    int childMin = tuple[1];
                    innerlevel = localTuple[0];
                    int child = pmax[innerlevel];
                    // System.out.println(child);
                    Vmax.get(innerlevel).set(child, childMax);
                    parentsMax.get(innerlevel).set(child, localTuple[1]);
                    pmax[innerlevel]++;
                    if (childMin != childMax) {
                        Vmin.get(innerlevel).set(pmin[innerlevel], childMin);
                        parentsMin.get(innerlevel).set(child, localTuple[2]);
                        pmin[innerlevel]++;
                        T.get(innerlevel).set(child);
                        bitsSet[innerlevel]++;
                    } else {
                        // Vmin.get(level).set(pmin[level], childMin);
                        // pmin[level]++;
                        T.get(innerlevel).unset(child);
                    }
                    if (min > childMin) {
                        min = childMin;
                    }
                    if (max < childMax) {
                        max = childMax;
                    }
                }
            }
            
            if (min == max) {
                pmax[innerlevel] = pmax[innerlevel] - k * k;
                T.get(innerlevel).setSize(pmax[innerlevel]);
            }
            
            returnStack.push(new int[] {min, max});
        }

        return returnStack.pop();
    }


    private static int[] Build(Matrix M, int n, int original_n, int original_m, int level, int row, int column,
            List<BitMap> T,
            List<GoodIntArrayList> Vmin, List<GoodIntArrayList> Vmax, int[] pmax, int[] pmin,
            List<GoodIntArrayList> parentsMax, List<GoodIntArrayList> parentsMin, int parentMax, int parentMin,
            int[] bitsSet) {

        int min, max;
        min = Integer.MAX_VALUE;
        max = 0;
        boolean lastlevel = n == k;
        int nKths = n / k;
        
        for (int i = 0; i < k; i++) {
            for (int j = 0; j < k; j++) {
                int child = pmax[level];
                if (lastlevel) {
                    int matrix_value;
                    matrix_value = M.get(row + i, column + j);

                    if (min > matrix_value) {
                        min = matrix_value;
                    }
                    if (max < matrix_value) {
                        max = matrix_value;
                    }
                    T.get(level).unset(child);
                    Vmax.get(level).set(child, matrix_value);
                    parentsMax.get(level).set(child, parentMax);
                    pmax[level]++;
                } else {
                    int[] res = Build(M, nKths, original_n, original_m, level + 1, row + i * nKths, column + j * nKths,
                            T, Vmin, Vmax, pmax,
                            pmin, parentsMax, parentsMin,
                            T.get(level).size(), bitsSet[level], bitsSet);
                    // System.out.println(child);
                    int childMax = res[0];
                    int childMin = res[1];
                    Vmax.get(level).set(child, childMax);
                    parentsMax.get(level).set(child, parentMax);
                    pmax[level]++;
                    if (childMin != childMax) {
                        Vmin.get(level).set(pmin[level], childMin);
                        parentsMin.get(level).set(child, parentMin);
                        pmin[level]++;
                        T.get(level).set(child);
                        bitsSet[level]++;
                    } else {
                        // Vmin.get(level).set(pmin[level], childMin);
                        // pmin[level]++;
                        T.get(level).unset(child);
                    }
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
            T.get(level).setSize(pmax[level]);
        }

        return new int[] { max, min };
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
        if (z >= Tree.size() || Tree.getOrZero(z + 1) == 0) // 😡
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
                if (zp + 1 >= Tree.size() || Tree.getOrZero(zp + 1) == 0) {
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
        if (r1 < 0 || r1 >= original_n || r2 < 0 || r2 >= original_n || c1 < 0 || c1 >= original_m || c2 < 0
                || c2 >= original_m)
            throw new IndexOutOfBoundsException("looked up window (" + r1 + ", " + c1 + ", " + r2 + ", " + c2
                    + ") in matrix with size (" + original_n + ", " + original_m + ")");
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

    private void searchValuesInWindow(int n, int r1, int r2, int c1, int c2, int z, int maxval, int minval, int vb,
            int ve, int[] out,
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

        int r1p, r2p, c1p, c2p, maxvalp, minvalp, zp;

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
                if (zp + 1 >= Tree.size() || Tree.getOrZero(zp + 1) == 0) {
                    minvalp = maxvalp;
                    if (minvalp >= vb && maxvalp <= ve) {

                        /* all cells meet the condition in this branch */
                        // int times = ((r2p - r1p) + 1) * ((c2p - c1p) + 1);
                        // for (int l = 0; l < times; l++) {
                        // out[index.index++] = maxvalp;
                        // }
                    }
                } else {
                    minvalp = LMin[this.Tree.rank(zp)];
                    if (minvalp >= vb && maxvalp <= ve) {
                        /* all cells meet the condition in this branch */
                        // int times = ((r2p - r1p) + 1) * ((c2p - c1p) + 1);
                        // for (int l = 0; l < times; l++) {
                        // out[index.index++] = maxvalp;
                        // }
                    }
                    if (minvalp < vb && maxvalp > ve) {
                        searchValuesInWindow(nKths, r1p, r2p, c1p, c2p, zp, maxvalp, minvalp, vb, ve, out, index,
                                level + 1,
                                indexRanks);
                    }
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
        if (r1 < 0 || r1 >= original_n || r2 < 0 || r2 >= original_n || c1 < 0 || c1 >= original_m || c2 < 0
                || c2 >= original_m)
            throw new IndexOutOfBoundsException("looked up window (" + r1 + ", " + c1 + ", " + r2 + ", " + c2
                    + ") in matrix with size (" + original_n + ", " + original_m + ")");
        int returnSize = (r2 - r1 + 1) * (c2 - c1 + 1); // can be smaller.
        int[] out = new int[returnSize];
        int maxLevel = 1 + (int) Math.ceil(Math.log(n) / Math.log(k));
        GoodArrayList<Pair<Integer, Integer>> indexRanks = new GoodArrayList<Pair<Integer, Integer>>(maxLevel);
        for (int i = 0; i < maxLevel; i++) {
            indexRanks.set(i, new Pair<>(-1, 0));
        }
        searchValuesInWindow(this.n, r1, r2, c1, c2, -1, this.maxval, this.minval, thresholdLow, thresholdHigh, out,
                new IntPointer(), 0,
                indexRanks);

        return out;
    }
}
