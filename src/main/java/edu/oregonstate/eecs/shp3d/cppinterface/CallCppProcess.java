package edu.oregonstate.eecs.shp3d.cppinterface;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CallCppProcess {
	private static Logger logger = LogManager.getLogger();
	
	private final String normalsPath;
	private final String xtrPath;
	private final String tttPath;
	private final String shp3dPath;

	public CallCppProcess(String normalsPath, String xtrPath, String tttPath, String shp3dPath) {
		this.normalsPath = normalsPath;
		this.xtrPath = xtrPath;
		this.tttPath = tttPath;
		this.shp3dPath = shp3dPath;
	}

	public void run() throws IOException {
		ProcessBuilder builder = new ProcessBuilder("./A", 
				normalsPath, xtrPath, tttPath, shp3dPath);
		builder.redirectErrorStream(true);
		logger.info("attempting to spawn a cpp process...");
		Process process = builder.start();
		InputStream stdout = process.getInputStream();
		
		String line;
		BufferedReader reader = new BufferedReader(new InputStreamReader(stdout));
		while ((line = reader.readLine()) != null) {
		    logger.info("stdout: {}", line);
		}
		
		logger.info("cpp process exits with code {}", process.exitValue());
	}

}
