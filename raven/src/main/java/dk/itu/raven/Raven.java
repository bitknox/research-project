package dk.itu.raven;

import dk.itu.raven.ksquared.DAC;
import dk.itu.raven.ksquared.K2Raster;

public class Raven {
    public static void main(String[] args) {
        int[][] M = { { 5, 5, 4, 4, 4, 4, 1, 1 }, //
                      { 5, 4, 4, 4, 4, 4, 1, 1 }, //
                      { 4, 4, 4, 4, 1, 2, 2, 1 }, //
                      { 3, 3, 4, 3, 2, 1, 2, 2 }, //
                      { 1, 1, 1, 1, 1, 1, 1, 1 }, //
                      { 1, 1, 1, 1, 1, 1, 1, 1}, //
                      { 1, 1, 1, 1, 1, 1, 1, 1}, //
                      { 1, 1, 1, 1, 1, 1, 1, 1} }; //
        K2Raster K2 = new K2Raster(M, 8);


        for(int i : K2.getWindow(8, 0, 7, 0, 7)){
            System.out.print(i + " ");
        }
    }
}