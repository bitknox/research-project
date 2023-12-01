package dk.itu.raven.ksquared;

import static dk.itu.raven.ksquared.Basics.*;

/*
 * https://github.com/sladra/DACs/blob/master/src/bitrankw32int.c
 */

public class BitRank {
    int[] data;
    char owner;
    int integers;
    int factor, b, s;
    int[] Rs;
    int n;

    public BitRank(int[] bitArray, int n, char owner, int factor) {
        this.data = bitArray;
        this.owner = owner;
        this.n = n;
        int lgn = bits(this.n - 1);
        this.factor = factor;
        if (factor == 0)
            this.factor = lgn;
        else
            this.factor = factor;
        this.b = 32;
        this.s = this.b * this.factor;
        this.integers = this.n / W;
        BuildRank(this);
    }

    private static int BuildRankSub(BitRank br, int ini, int fin) {
        int i;
        int rank = 0, aux;
        for (i = ini; i < ini + fin; i++) {
            if (i <= br.integers) {
                aux = br.data[i];
                rank += popcount(aux);
            }
        }
        return rank; // retorna el numero de 1's del intervalo
    }

    private static void BuildRank(BitRank br) {
        int i;
        int num_sblock = br.n / br.s;
        br.Rs = new int[num_sblock + 1];
        for (i = 0; i < num_sblock + 1; i++)
            br.Rs[i] = 0;
        int j;
        br.Rs[0] = 0;
        for (j = 1; j <= num_sblock; j++) {
            br.Rs[j] = br.Rs[j - 1];
            br.Rs[j] += BuildRankSub(br, (j - 1) * (br.factor), br.factor);
        }
    }

    public int Rank(int i) {
        int a;
        if (i + 1 == 0)
            return 0;
        ++i;
        int resp = this.Rs[i / this.s];
        int aux = (i / this.s) * (this.factor);
        for (a = aux; a < i / W; a++)
            resp += popcount(this.data[a]);
        resp += popcount(this.data[i / W] & ((1 << (i & mask31)) - 1)); // safe
        return resp;
    }
}