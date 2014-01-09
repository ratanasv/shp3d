package edu.oregonstate.eecs.shp3d;

import java.util.Map;
import java.util.HashMap;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

enum Argument {

	HELP("help", "print usage", false), 
	SHP_IN("shpin", "fully-qualified path (with filename) for the output shapefile", true),
	SHP_OUT("shpout", "fully-qualified path (with filename) for the input shapefile", true),
	VERBOSE("verbose", "verbose, duh", false);

	final static class ArgumentManager {
		static void init(String [] args) throws ParseException {
			Options options = new Options();
			for (Argument arg : Argument.values()) {
				options.addOption(arg.stringRep, arg.hasValue, arg.description);
			}

			CommandLineParser parser = new BasicParser();
			CommandLine commandLine = parser.parse(options, args);

			if (commandLine.hasOption(HELP.stringRep)) {
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp( "shp3d", options );
				System.exit(0);
			}

			for (Argument arg : Argument.values()) {
				if (commandLine.hasOption(arg.stringRep)) {
					arg.isActive = true;
					arg.value = commandLine.getOptionValue(arg.stringRep);
				} else {
					arg.isActive = false;
				}
			}
		}

		private ArgumentManager() {

		}
	}

	private static Map<String, Argument> stringToEnum;
	static {
		stringToEnum = new HashMap<String, Argument>();
		for (Argument arg : Argument.values()) {
			stringToEnum.put(arg.stringRep, arg);
		}
	} 
	public static Argument toEnum(String str) {
		return stringToEnum.get(str);
	}

	private final String stringRep;
	private String value;
	private boolean isActive;
	private final String description;
	private final boolean hasValue;

	Argument(String rep, String desc, boolean hasValue) {
		this.stringRep = rep;
		this.isActive = false;
		this.description = desc;
		this.hasValue = hasValue;
	}

	String getValue() {
		return this.value;
	}

	boolean isActive() {
		return this.isActive;
	}
}
