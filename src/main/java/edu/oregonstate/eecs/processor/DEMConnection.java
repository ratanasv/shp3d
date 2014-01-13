package edu.oregonstate.eecs.processor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

final class DEMConnection {
	private final byte[] byteArray;

	static enum Server {
		MIKES_DEM("http://maverick.coas.oregonstate.edu:11300/terrainextraction.ashx?");

		private final String urlString;

		Server(String urlString) {
			this.urlString = urlString;
		}

		String getURL() {
			return urlString;
		}

	}

	DEMConnection(final String urlString) throws IOException {
		URL url = new URL(urlString);
		URLConnection connection = url.openConnection();
		InputStream in = connection.getInputStream();
		int contentLength = connection.getContentLength();
		ByteArrayOutputStream output = new ByteArrayOutputStream(contentLength);

		byte[] buffer = new byte[512];
		while (true) {
			int length = in.read(buffer);
			if (length == -1) {
				break;
			}
			output.write(buffer, 0, length);
		}
		in.close();
		output.close();

		byteArray = output.toByteArray();
	}

	byte[] getByteArray() {
		return byteArray;
	}
}
