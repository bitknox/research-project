package dk.itu.raven.util;

public class Tuple5<A, B, C, D, E> extends Tuple4<A, B, C, D> {
	public final E e;

	public Tuple5(A a, B b, C c, D d, E e) {
		super(a, b, c, d);
		this.e = e;
	}

}
