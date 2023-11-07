package dk.itu.raven.ksquared;

import java.util.ArrayList;
import java.util.List;

public class K2Raster {
    static final int k = 2;

    // K2Raster[] children = new K2Raster[k*k];
    public List<Character> Tree;
    public List<Integer> LMax;
    public List<Integer> LMin;


    public K2Raster(int[][] M, int n) {
        int maxLevel = 1+(int)Math.ceil(Math.log(n) / Math.log(k));
        List<ArrayList<Character>> T = new ArrayList<ArrayList<Character>>(maxLevel);
        List<ArrayList<Integer>> Vmin = new ArrayList<ArrayList<Integer>>(maxLevel);
        List<ArrayList<Integer>> Vmax = new ArrayList<ArrayList<Integer>>(maxLevel);
        for (int i = 0; i < maxLevel; i++) {
            T.add(new ArrayList<>());
            Vmin.add(new ArrayList<>());
            Vmax.add(new ArrayList<>());
        }
        Build(M, n, 0, 0, 0, T, Vmin, Vmax);

        
    }

    static int[] Build(int[][] M, int n, int level, int row, int column, List<ArrayList<Character>> T, List<ArrayList<Integer>> Vmin, List<ArrayList<Integer>> Vmax) {
        int min,max;
        min = Integer.MAX_VALUE;
        max = 0;
        // System.err.println(n + ": " + Math.log(n) + "/" + Math.log(k));
        // System.err.println(Math.ceil(Math.log(n) / Math.log(k)));
        // boolean lastlevel = level == (int)Math.ceil(Math.log(n) / Math.log(k));
        // System.err.println(row + " " + column);
        // if (level == 4) System.exit(-1);
        boolean lastlevel = n == k;
        int nKths = n / k;
        for (int i = 0; i < k; i++) {
            for (int j = 0; j < k; j++) {
                if (lastlevel) {
                    if (min > M[row+i][column+j]) {
                        min = M[row+i][column+j];
                    }

                    if (max < M[row + i][column + j]) {
                        max = M[row + i][column + j];
                    }
                    Vmax.get(level).add(M[row + i][column + j]);
                    T.get(level).add('#');
                    //pmax++
                } else {
                    int[] res = Build(M,nKths,level+1,row+i*nKths,column+j*nKths,T,Vmin,Vmax);
                    int childMax = res[0];
                    int childMin = res[1];
                    // System.err.println("children: " + childMin + " " + childMax);
                    Vmax.get(level).add(childMax);
                    if (childMin != childMax) {
                        Vmin.get(level).add(childMin);
                        T.get(level).add('1');
                    } else {
                        T.get(level).add('0');
                    }
                    //pmax++
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
            // System.err.println(row + ", " + column + " - " + n);
            // System.err.print("deleted ");
            for (int i = 0; i < k*k; i++) {
                int idx = T.get(level).size()-1;
                // System.err.print(T.get(level).get(idx));
                T.get(level).remove(idx);
                Vmax.get(level).remove(idx);
            }
            // System.err.println("");
        }

        return new int[] {max, min};
    }
}
