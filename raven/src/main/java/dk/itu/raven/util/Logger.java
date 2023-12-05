package dk.itu.raven.util;

public class Logger {
	static boolean debug = false;
	public static void log(Object o) {
		if(debug) System.out.println(o.toString());
	}
	public static void log() {
		if(debug) System.out.println("");
	}

	public static void log(Exception e) {
		if(debug) e.printStackTrace();
	}

	public static void setDebug(boolean debug) {
		Logger.debug = debug;
	}

	public static boolean getDebug() {
		return debug;
	}
}