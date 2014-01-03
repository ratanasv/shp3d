package edu.oregonstate.eecs.shp3d;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.ParseException;

/**
 * Hello world!
 *
 */
public class App  {
	public static void main( String[] args ) {
		System.out.println( "shp3d..." );
		CommandLine commandLine = null;
		try {
			commandLine = parseOption(args);
		} catch (ParseException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		
		String shp3dOutputPath = commandLine.getOptionValue("shp3d");
	}

	private static CommandLine parseOption(String[] args) throws ParseException {
		Options options = new Options();
		options.addOption("help", false, "print usage");
		options.addOption("shp3d", true, "fully-qualified path (with filename) for the output 3D shapefile");
		options.addOption("shp2d", true, "fully-qualified path (with filename) for the input 2D shapefile");
		CommandLineParser parser = new BasicParser();
		CommandLine commandLine = parser.parse(options, args);
		
		if (commandLine.hasOption("help")) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp( "shp3d", options );
			System.exit(0);
		}

		return commandLine;
	}
}
