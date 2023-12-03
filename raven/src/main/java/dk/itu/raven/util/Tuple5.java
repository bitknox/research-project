package dk.itu.raven.util;

public class Tuple5<A, B, C, D, E> extends Tuple3<A, B, C> {
	public final D d;
	public final E e;

	public Tuple5(A a, B b, C c, D d, E e) {
		super(a, b, c);
		this.d = d;
		this.e = e;
	}

}
