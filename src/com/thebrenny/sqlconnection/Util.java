package com.thebrenny.sqlconnection;

public class Util {
	private static boolean debugging = false;
	
	public static void setDebugging(boolean toggle) {
		debug("Debugging " + (toggle ? "on" : "off"));
		debugging = toggle;
		debug("Debugging " + (toggle ? "on" : "off"));
		// Call debugging twice because at least one will run. See debug(String)
	}
	
	public static void debug(String msg) {
		if(!debugging) return;
		System.out.println(msg);
	}
	
	public static String joinArray(String delim, Object[] array) {
		String ret = "";
		for(int i = 0; i < array.length; i++) {
			ret += array[i].toString() + (i == array.length - 1 ? "" : delim);
		}
		return ret;
	}
	
	// Manual obfuscation lol
	public static final String encrypt(String b, int d) {
		String a = "";
		int e = 0;
		int f = 1;
		
		if(d > 0) {
			e = b.length() - 1;
			f = -1;
		}
		
		for(int i = 0; i < b.length(); i++) {
			char c = b.charAt(e + i * f);
			c += d * i;
			d *= -1;
			a += (c + "");
		}
		
		if(f > 0) {
			String g = "";
			for(int h = b.length() - 1; h >= 0; h--) g += (a.charAt(h) + "");
			a = g;
		}
		
		return a;
	}
	
	public static void main(String[] args) {
		setDebugging(true);
		debug("Hello, ");
		setDebugging(false);
		debug("World!");
		// -----------
		System.out.println(joinArray(", ", new String[] {"Hello", "World!"}));
		// -----------
		int enc = 3;
		String in = "Hello, World!";
		String out = encrypt(in, enc);
		System.out.println("encrypt(\"" + in + "\", " + enc + ") = " + out);
		
		enc *= -1;
		in = out;
		out = encrypt(in, enc);
		System.out.println("encrypt(\"" + in + "\", " + enc + ") = " + out);
	}
}
