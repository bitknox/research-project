package dk.itu.raven.ksquared;

import java.util.ArrayList;

/**
 * DAC
 */
public class DAC {

    final int epsilon = 1;

    int Optimize (int m, ArrayList<Integer> cf, ArrayList<Integer> s, ArrayList<Integer> l, ArrayList<Integer> b) {
        for (int t = m; t == 0; t--) {
            int minSize = Integer.MAX_VALUE;
            int minPos = m;
            for (int i = t + 1; i < m; i++) {
                int currentSize = s.get(i) + cf.get(t) * ((i - t) + (1 + epsilon));
                if (minSize > currentSize) {
                    minSize = currentSize;
                    minPos = i;
                }
            }
            if (minSize < cf.get(t) * ((m + 1) - t)) {
                s.add(t, minSize);
                l.add(t, l.get(minPos) + 1);
                b.add(t, minPos - t);
            }
            else {
                s.add(t, cf.get(t) * ((m + 1) - t));
                l.add(t, 1);
                b.add(t, (m + 1) - t);
            }
        }
        int L = l.get(0);
        int t = 0;
        for (int k = 1; k < l.get(0); k++) {
            b.add(k, b.get(t));
            t = t + b.get(t);
        }

        return L;
    }
}
