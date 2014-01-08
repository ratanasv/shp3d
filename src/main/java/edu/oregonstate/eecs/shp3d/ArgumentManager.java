package edu.oregonstate.eecs.shp3d;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

final class ArgumentManager {
	private static CommandLine commandLineInstance;

	private ArgumentManager() {

	}

	public static CommandLine GetInstance() {
		return commandLineInstance;
	}

	static void Init(String[] args) throws ParseException {
		Options options = new Options();
		options.addOption("help", false, "print usage");
		options.addOption("shp3d", true, "fully-qualified path (with filename) for the output " +
				"3D shapefile");
		options.addOption("shp2d", true, "fully-qualified path (with filename) for the input " +
				"2D shapefile");
		options.addOption("verbose", false, "verbose, duh");

		CommandLineParser parser = new BasicParser();
		CommandLine commandLine = parser.parse(options, args);

		if (commandLine.hasOption("help")) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp( "shp3d", options );
			System.exit(0);
		}

		commandLineInstance = commandLine;
	}
}
