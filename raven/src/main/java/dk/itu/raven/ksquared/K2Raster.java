package dk.itu.raven.ksquared;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class K2Raster {
    static final int k = 2;

    public BitMap Tree;
    public List<Integer> LMax;
    public List<Integer> LMin;
    public List<ArrayList<Integer>> parent;

    public K2Raster(int[][] M, int n) {
        int maxLevel = 1 + (int) Math.ceil(Math.log(n) / Math.log(k));
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
        int[] res = Build(M, n, 1, 0, 0, T, Vmin, Vmax, pmax, pmin, parent, 0);
        Vmax.get(0).add(res[0]);
        Vmin.get(0).add(res[1]);

        Tree = new BitMap(0);
        LMax = new ArrayList<>();
        LMin = new ArrayList<>();

        Tree = new BitMap(100);
        int bitmapIndex = 0;
        for (int i = 0; i < maxLevel; i++) {
            for (int j = 0; j < T.get(i).size(); j++) {
                if (T.get(i).isSet(j)) {
                    Tree.set(++bitmapIndex);
                } else {
                    Tree.unset(++bitmapIndex);
                }
            }
        }

        // use Tree instead
        // int length = 0;
        // int bitmapIndex = 0;
        for (int i = 1; i < maxLevel; i++) {
            // byte[] b = T.get(i).getBytes();
            // System.out.println(b[0]);
            // Tree.setBytes(b, bitmapIndex);
            // bitmapIndex = T.get(i).size();
            // // if (i != maxLevel - 1)
            // length += pmax[i];
            for (int j = 0; j < pmax[i]; j++) {
                LMax.add(Vmax.get(i - 1).get(parent.get(i).get(j)) - Vmax.get(i).get(j));
                if (T.get(i).isSet(j)) {
                    LMin.add(Vmin.get(i).get(j) - Vmin.get(i - 1).get(parent.get(i).get(j)));
                }
            }
        }

        // Tree = Tree.subList(0, length);
    }

    static int[] Build(int[][] M, int n, int level, int row, int column, List<BitMap> T,
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
                    if (min > M[row + i][column + j]) {
                        min = M[row + i][column + j];
                    }
                    if (max < M[row + i][column + j]) {
                        max = M[row + i][column + j];
                    }
                    Vmax.get(level).add(pmax[level], M[row + i][column + j]);
                    T.get(level).unset(pmax[level]);
                    parent.get(level).add(pmax[level], caller);
                    pmax[level]++;
                } else {
                    int[] res = Build(M, nKths, level + 1, row + i * nKths, column + j * nKths, T, Vmin, Vmax, pmax,
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
        }

        return new int[] { max, min };
    }
}
