package ksp;

import java.util.ArrayList;
import ksp.utilities.IDGenerator;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class TrackerApp {
  RequestGraph requestGraph = new RequestGraph(new ArrayList<>());
  boolean successfulIDGenCreation = IDGenerator.init(0);

  public TrackerApp() {
  }

  /**
     * Runs the application
     *
     * @param args an array of String arguments to be parsed
     */
    public void run(String[] args) {

      CommandLine line = parseArguments(args);

      if (line.hasOption("display")) {

        System.out.println(line.getOptionValue("display"));
        String fileName = line.getOptionValue("filename");


      } else {
        printAppHelp();
      }
    }

    /**
     * Parses application arguments
     *
     * @param args application arguments
     * @return <code>CommandLine</code> which represents a list of application
     * arguments.
     */
    private CommandLine parseArguments(String[] args) {

      Options options = getOptions();
      CommandLine line = null;

      CommandLineParser parser = new DefaultParser();

      try {
        line = parser.parse(options, args);

      } catch (ParseException ex) {

        System.err.println("Failed to parse command line arguments");
        System.err.println(ex.toString());
        printAppHelp();

        System.exit(1);
      }

      return line;
    }

    /**
     * Generates application command line options
     *
     * @return application <code>Options</code>
     */
    private Options getOptions() {

      var options = new Options();
      options.addOption("s", "set",true, "set team that is using the tracker");
      options.addOption("r", "request",true, "file name to load data from");
      options.addOption("d", "display",true, "show request graph");

      return options;
    }

    /**
     * Prints application help
     */
    private void printAppHelp() {

      Options options = getOptions();

      var formatter = new HelpFormatter();
      formatter.printHelp("Karman Space Request Tracker", options, true);
    }

  }
