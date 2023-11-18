package dk.itu.raven;

import dk.itu.raven.ksquared.DAC;
import dk.itu.raven.ksquared.K2Raster;

public class Raven {
    public static void main(String[] args) {
        int[][] M = { { 5, 5, 4, 4}, //
                      { 5, 4, 0, 4}, //
                      { 0, 0, 0, 0}, //
                      { 0, 0, 0, 0}}; //
        K2Raster K2 = new K2Raster(M, 4, 4);

        for (int j : K2.Tree) {
            System.out.print(j + "");
        }
        System.err.println("");

        // for (int i = 1; i <= K2.LMax.listLength; i++) {
        //     System.err.print(K2.LMax.accessFT(i) + " ");
        // }
        // System.err.println("");

        // for (int r = 0; r < 8; r++) {
        //     for (int c = 0; c < 8; c++) {
        //         System.err.print(K2.getCell(8, r, c, 5) + " ");
        //     }
        //     System.err.println("");
        // }

        for(int i : K2.getWindow(0, 8, 0, 8)){
            System.out.print(i + " ");
        }
    }
}