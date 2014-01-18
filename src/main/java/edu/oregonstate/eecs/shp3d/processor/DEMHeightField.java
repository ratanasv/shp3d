package edu.oregonstate.eecs.shp3d.processor;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;


public final class DEMHeightField implements HeightField {
	class XTRHeader {
		final float maxLat;
		final float maxLng;
		final float minLat;
		final float minLng;
		final int numLats;
		final int numLngs;

		XTRHeader(byte[] header) throws UnsupportedEncodingException {
			String headerText = new String(header, 0, header.length, "ASCII");
			String[] values = headerText.split(" |\n");
			if (values.length != 6) {
				throw new RuntimeException("XTRHeader if not of length 6");
			}
			minLng = Float.parseFloat(values[0]);
			maxLng = Float.parseFloat(values[1]);
			numLngs = Integer.parseInt(values[2]);
			minLat = Float.parseFloat(values[3]);
			maxLat = Float.parseFloat(values[4]);
			numLats = Integer.parseInt(values[5]);
		}
	}

	static int getFormFeedLocation(byte[] byteArray) {
		for (int i=0; i<byteArray.length; i++) {
			if (byteArray[i] == 0x0c) {
				return i;
			}
		}
		throw new RuntimeException("data doesn't contain form feed char");
	}
	
	// okay like seriously this is the most ridiculous bug i've ever seen.
	// EDIT: found the root cause: http://en.wikipedia.org/wiki/Endianness
	static byte[] ridiculousReverse(byte[] weird) {
		final int length = weird.length;
		byte[] correct = new byte[length];
		for (int i=0; i<length; i=i+4) {
			byte a = weird[i+0];
			byte b = weird[i+1];
			byte c = weird[i+2];
			byte d = weird[i+3];
			correct[i+0] = d;
			correct[i+1] = c;
			correct[i+2] = b;
			correct[i+3] = a;
		}
		return correct;
	}

	private final List<Float> heights;
	private final XTRHeader xtrHeader;

	public DEMHeightField(DEMConnection connection) throws IOException {
		byte[] byteArray = connection.getByteArray();
		
		final int splitLoc = getFormFeedLocation(byteArray); 
		final int arrayLength = byteArray.length;
		xtrHeader = new XTRHeader(Arrays.copyOfRange(byteArray, 0, splitLoc));
		byte[] weirdContent = Arrays.copyOfRange(byteArray, splitLoc + 1, arrayLength);
		byte[] xtrContent = ridiculousReverse(weirdContent);
		ByteBuffer byteBuffer = ByteBuffer.wrap(xtrContent);
		
		

		final int totalPoints = xtrHeader.numLats*xtrHeader.numLngs;
		final int capacity = byteBuffer.capacity();

		if (totalPoints*4 != capacity) {
			throw new RuntimeException("totalPoints*4 != capacity");
		}

		heights = new ArrayList<Float>(); 
		for (int i=0; i<totalPoints; i++) {
			heights.add(byteBuffer.getFloat());
		}

		if (byteBuffer.position() != capacity) {
			throw new RuntimeException("Parsing of ByteBuffer of xtrContent failed, " + 
					String.valueOf(byteBuffer.position()) + " != " +
					String.valueOf(capacity) );
		}
	}

	@Override
	public float heightAt(double x, double y) {
		double latRange = xtrHeader.maxLat-xtrHeader.minLat;
		double lngRange = xtrHeader.maxLng-xtrHeader.minLng;
		int latI = (int)(((y-xtrHeader.minLat)/latRange)*((double)xtrHeader.numLats-1.0));
		int lngI = (int)(((x-xtrHeader.minLng)/lngRange)*((double)xtrHeader.numLngs-1.0));
		return heightAtIndex(latI, lngI);
	}

	float heightAtIndex(int i, int j) {
		return heights.get(xtrHeader.numLngs*i + j);
	}

	XTRHeader getXTRHeader() {
		return xtrHeader;
	}
}
