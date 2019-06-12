package com.nicksdesk.mle;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;

public class MLE {

	private static double version = 0.0;
	
	private static Double[] versions = {
		0.1,
		0.2,
		0.3,
		0.4,
		0.5
	};
	
	private static String[] reqAttribs = {
		"correspond",
		"var",
		"version",
		"AS",
		"key",
		"val",
		"&",
		"(",
		"\"",
		":",
		";"
	};

	
	public static String[] percentModifierAttribs = {
		"DEFER_MODIFY_VAR",
		"DEFER_MODIFY_CORRESPONDER",
		"DEFER_MODIFY_ENV"
	};
	
	public static String[] atControlHeaders = {
		"ignore_case",
		"ignore_errs"
	};
	
	public MLE(double version) {
		for(int i = 0; i < versions.length; i++) {
			if(Double.parseDouble(String.valueOf(version)) == versions[i]) {
				MLE.version = version;
			}
		}
	}
	
	public static double getVersion() {
		return MLE.version;
	}
	
	private static String parseWebMLE(URL url) {
		try {
			String response = Reader.readMLE(url);
			return response;
		} catch(Exception e) {
			e.printStackTrace();
			return e.getMessage();
		}
	}
	
	private static String parseOfflineMLE(String path) {
		try {
			File in = new File(path);
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(in)));
			return Reader.readAll(reader);
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private String prepareParser(String unparsed) {
		if(Arrays.stream(reqAttribs).anyMatch(unparsed::contains)) {
			if(unparsed.startsWith("<!")) {
				if(unparsed.endsWith("!>")) {
					String version = unparsed.substring(unparsed.indexOf("version:") + 8, unparsed.indexOf(";"));
					if(Double.parseDouble(version) == MLE.version) {
						Parser parser = new Parser();
						String response = parser.parseBody(unparsed);
						return response;
					} else 
						return "Error: Version Control - Versions out of sync, make sure MLE file version is the same as the provided version!";
				} else {
					return "Error: Parser - Make sure your MLE file ends correctly!";
				}
			} else {
				return "Error: Parser - Make sure your MLE file starts correctly!";
			}
		} else {
			return "Error: Parser - Make sure your MLE file contains valid characters!";
		}
	}
	
	public String parse(URL url) {
		String unparsed = parseWebMLE(url).replaceAll("null", "").toLowerCase().trim();
		return this.prepareParser(unparsed);
	}
	
	public String parse(String file) {
		String unparsed = parseOfflineMLE(file).replaceAll("null", "").toLowerCase().trim();
		return this.prepareParser(unparsed);
	}
	
	public String getCorresponderData(Object corresponder) {
		return Parser.getCorresponderData(corresponder);
	}
	
	public String getVar(Object corresponder, Object var) {
		return Parser.getVar(corresponder, var);
	}
	
	public String getRaw() {
		return Parser.getRaw();
	}
	
}
