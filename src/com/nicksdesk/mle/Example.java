package com.nicksdesk.mle;

public class Example {
	
	public static void main(String[] args) {
		MLE mle = new MLE(0.1);
		try {
			
			String response = mle.parse("C:\\Users\\Nicklvsa\\Desktop\\example.mle");
			//String response = mle.parse("C:\\Users\\121568\\Desktop\\example.mle");
			//String response = mle.parse(new URL("https://nicksdesk.com/example.mle"));
			//System.out.println(response);
			//Utils.log(getVar("something", "cool"), 0);
			System.out.println(response);
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private static Object getVar(Object corresponder, Object var) {
		return Parser.getVar(corresponder, var);
	}
	
 }
