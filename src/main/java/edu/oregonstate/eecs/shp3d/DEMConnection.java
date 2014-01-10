package edu.oregonstate.eecs.shp3d;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

final class DEMConnection {
	DEMConnection(final String urlString) throws IOException {
		URL oracle = new URL(urlString);
		URLConnection yc = oracle.openConnection();
		BufferedReader in = new BufferedReader(new InputStreamReader(
				yc.getInputStream()));
		String inputLine;
		while ((inputLine = in.readLine()) != null) 
			System.out.println(inputLine);
		in.close();
	}
}
