package dk.itu.raven.util;

public class Tuple4<A,B,C,D> extends Tuple3<A,B,C> {
    public final D d;

    public Tuple4(A a, B b, C c, D d) {
        super(a, b, c);
        this.d = d;
    }
    
}
