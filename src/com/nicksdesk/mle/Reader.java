package com.nicksdesk.mle;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;

public class Reader {
	
	public static String readAll(BufferedReader rd) throws Exception {
		StringBuilder str = new StringBuilder();
		int cp;
		while((cp = rd.read()) != -1) {
			str.append((char)cp);
		}
		return str.toString();
	}
	
	public static String readMLE(URL url) throws Exception {
		InputStream is = url.openStream();
		try {
			BufferedReader read = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
			String txt = readAll(read);
			return txt;
		} finally {
			is.close();
		}
	}
	
	public static boolean isNumeric(String str) {
		for(char c : str.toCharArray()) {
			if(!Character.isDigit(c)) return false;
		}
		return true;
	}
	
	public static String[] toStringArray(List<String> list) {
		String[] strArray = new String[list.size()];
		strArray = list.toArray(strArray);
		return strArray;
	}
}
