package edu.oregonstate.eecs.shp3d.cppinterface;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.commons.io.FilenameUtils;
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

	public void run() throws IOException, URISyntaxException {
		URL location = getClass().getProtectionDomain().getCodeSource().getLocation();
		String path = FilenameUtils.getFullPath(location.getPath());
		
		ProcessBuilder builder = new ProcessBuilder(path + "A", 
				 xtrPath, normalsPath, shp3dPath, tttPath);
		builder.redirectErrorStream(true);
		logger.info("Attempting to spawn a cpp process...");
		Process process = builder.start();
		InputStream stdout = process.getInputStream();
		
		String line;
		BufferedReader reader = new BufferedReader(new InputStreamReader(stdout));
		while ((line = reader.readLine()) != null) {
		    logger.info("stdout: {}", line);
		}
		
		logger.info("Cpp process exits with code {}", process.exitValue());
	}

}
