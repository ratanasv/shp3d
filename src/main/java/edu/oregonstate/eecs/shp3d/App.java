package edu.oregonstate.eecs.shp3d;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.ParseException;

/**
 * Hello world!
 *
 */
public class App  {
	public static void main( String[] args ) {
		System.out.println( "Hello World!" );
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
		Options option = new Options();
		option.addOption("shp3d", true, "fully-qualified path (with filename) for the output shp3d file");
		CommandLineParser parser = new BasicParser();

		return parser.parse(option, args);
	}
}
