package com.zhketech.mstapp.client.port.project.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class SipHttpUtils implements Runnable {

	String url;
	GetHttpData listern;

	public SipHttpUtils(String url, GetHttpData listern) {
		this.url = url;
		this.listern = listern;
	}

	@Override
	public void run() {
		synchronized (this) {
			try {

				HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
				con.setConnectTimeout(3000);
				con.setRequestMethod("GET");
				con.connect();
				if (con.getResponseCode() == 200) {
					InputStream in = con.getInputStream();
					String result = readTxt(in);
					if (listern != null) {
						listern.httpData(result);
					}
				}else{
					if (listern != null) {
						listern.httpData("Execption:code != 200");
					}
				}
				con.disconnect();
			} catch (Exception e) {
				if (listern != null) {
					listern.httpData("Execption:" + e.getMessage());
				}
			}
		}
	}

	
	public void start() {
		new Thread(this).start();
	}
	public String readTxt(InputStream in) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		StringBuffer sb = new StringBuffer();
		String line;
		while ((line = reader.readLine()) != null) {
			sb.append(line);
		}
		reader.close();
		return sb.toString();
	}

	public interface GetHttpData {
		void httpData(String result);
	}
}
