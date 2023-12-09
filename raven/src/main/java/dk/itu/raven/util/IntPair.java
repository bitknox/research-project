package dk.itu.raven.util;

public class IntPair implements Comparable<IntPair> {
    public int first,second;
    public IntPair(int first, int second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public int compareTo(IntPair o) {
        return first - o.first;
    }
    
}
