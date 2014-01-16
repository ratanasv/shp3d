package edu.oregonstate.eecs.shp3d.processor;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

public class ShapefileHeaderZRepair {
	
	private final double minZ;
	private final double maxZ;
	
	public ShapefileHeaderZRepair(Builder builder) {
		this.minZ = builder.minZ;
		this.maxZ = builder.maxZ;
	}
	
	public static class Builder {
		private double minZ;
		private double maxZ;
		
		public Builder() {
			this.minZ = 0.0;
			this.maxZ = 0.0;
		}
		
		public Builder withMinZ(double val) {
			this.minZ = val;
			return this;
		}
		
		public Builder withMaxZ(double val) {
			this.maxZ = val;
			return this;
		}
		
		public ShapefileHeaderZRepair build() {
			return new ShapefileHeaderZRepair(this);
		}
	}
	
	public void writeToFile(File shpFile) throws IOException {
		RandomAccessFile file = new RandomAccessFile(shpFile, "rw");;
		try {
			if (file.getFilePointer() != 0) {
				throw new RuntimeException("file pointer not initialized to zero");
			}
			ByteBuffer byteBuffer = ByteBuffer.allocate(16);
			byteBuffer.putDouble(minZ);
			byteBuffer.putDouble(maxZ);
			byteBuffer.rewind();
			byte[] byteArray = new byte[byteBuffer.remaining()];
			byteBuffer.get(byteArray);
			file.seek(68);
			file.write(byteArray);
		} finally {
			file.close();
		}
	}
	
}
