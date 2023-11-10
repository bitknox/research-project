package dk.itu.raven;

import dk.itu.raven.ksquared.K2Raster;

public class Raven {
    public static void main(String[] args) {
        int[][] M = { { 5, 5, 4, 4, 4, 4, 1, 1 }, //
                { 5, 4, 4, 4, 4, 4, 1, 1 }, //
                { 4, 4, 4, 4, 1, 2, 2, 1 }, //
                { 3, 3, 4, 3, 2, 1, 2, 2 }, //
                { 3, 4, 3, 3, 2, 2, 2, 2 }, //
                { 4, 3, 3, 2, 2, 2, 2, 2 }, //
                { 1, 1, 1, 3, 2, 2, 2, 2 }, //
                { 1, 1, 1, 2, 2, 2, 2, 2 } }; //
        K2Raster K2 = new K2Raster(M, 8);

        for (int j : K2.Tree) {
            System.out.print(j + "");
        }
        System.err.println("");

        // for (int i = 0; i < 3; i++) {
        // for (int j : K2.parent.get(i)) {
        // System.err.print(j + " ");
        // }
        // System.err.println("");
        // }

        for (int i : K2.LMin) {
            System.err.print(i + " ");
        }
        System.err.println("");

        for (int i : K2.LMax) {
            System.err.print(i + " ");
        }
        System.err.println("");

    }
}