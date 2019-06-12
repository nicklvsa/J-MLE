package com.nicksdesk.mle;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.json.JSONObject;

public class Utils {

	private static Object nullptr = null;
	
	public static void log(Object msg, int severity) {
		switch(severity) {
			case 0:
				System.out.println(msg);
			break;
			case 1: 
				System.err.println(msg);
			break;
			default:
			break;
		}
	}
	
	public static boolean store(Object cor, Object var, Object value) {
		HttpURLConnection conn = (HttpURLConnection) nullptr;
		StringBuilder response = new StringBuilder();
		//TODO: PUT YOUR OWN BACKEND ENDPOINT HERE:
		String url = "https://nicksdesk.com/mle-java/api/index.php";
		String params = ("store=true&corresponder="+String.valueOf(cor)+"&var="+String.valueOf(var)+"&val="+String.valueOf(value)).replaceAll("\"", "").trim();
		byte[] data = params.getBytes(StandardCharsets.UTF_8);
		try {
			URL send = new URL(url);
			conn = (HttpURLConnection)send.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("User-Agent", "MLE Sender");
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			try(DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
				wr.write(data);
			} catch(Exception e) {
				return false;
			}
			try(BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
				String line;
				while((line = in.readLine()) != null) {
					response.append(line);
					response.append("\n");
				}
				String result = response.toString();
				JSONObject jsonResult = new JSONObject(result);
				return (jsonResult.has("success"));
			} catch(Exception e) {
				return false;
			}
		} catch(Exception e) {
			return false;
		} finally {
			conn.disconnect();
		}
	}
	
}
