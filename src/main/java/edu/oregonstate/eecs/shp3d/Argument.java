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
	VERBOSE("verbose", "verbose, duh", false),
	CONNECT("connect", "connect to DEM server", false),
	PRINT_GEOMETRY("print_geometry", "print geometry info", false),
	PRINT_ATTRIBUTE("print_attribute", "print attribute info", false),
	PRINT_SCHEMA("print_schema", "print schema", false),
	PRINT_HEADER("print_header", "print header", false),
	DEM_RESOLUTION("dem_resolution", "specify the resolution of DEM", true),
	CHECK("check", "check shapefile for correctness", false);


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
		if (!isActive()) {
			throw new RuntimeException(stringRep + " is never specified by the user.");
		}
		if (!hasValue){
			throw new UnsupportedOperationException(stringRep + " is not supposed to have a value");
		}
		return this.value;
	}

	boolean isActive() {
		return this.isActive;
	}
}
