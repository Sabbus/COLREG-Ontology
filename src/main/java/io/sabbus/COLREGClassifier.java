package io.sabbus.colregclassifier;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;

class COLREGClassifier {

    static String helpMessage = "Ontology Based COLREG Classifier";

    public static void main(String[] args) {
        Option help = Option.builder("h")
                                .longOpt("help")
                                .desc("print help message")
                                .build();
        Option situation = Option.builder("s")
                                .longOpt("situation")
                                .desc("tells the reasoner classify the input scenario")
                                .build();
        Option behavior = Option.builder("b")
                                .longOpt("behavior")
                                .desc("tells the reasoner to infer vessels behavior")
                                .build();
        Option explanation = Option.builder("e")
                                .longOpt("explaination")
                                .desc("tells the reasoner to explain the inferences")
                                .build();
        Option prettyPrint = Option.builder("p")
                                .longOpt("pretty-print")
                                .desc("pretty print the output file")
                                .build();
        Option file = Option.builder("f")
                                .argName("scenario")
                                .longOpt("file")
                                .hasArg()
                                .desc("path to JSON file describing the encounter scenario")
                                .build();
        Option output = Option.builder("o")
                                .argName("output")
                                .longOpt("file")
                                .hasArg()
                                .desc("path to output JSON file")
                                .build();
        Options options = new Options();
        options.addOption(help);
        options.addOption(situation);
        options.addOption(behavior);
        options.addOption(explanation);
        options.addOption(prettyPrint);
        options.addOption(file);
        options.addOption(output);

        CommandLineParser parser = new DefaultParser();
        
        try {
            CommandLine cmd = parser.parse(options, args);

            if (cmd.hasOption("h")) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp(helpMessage, options);
                System.exit(1);
            }

            if (cmd.hasOption("file")) {
                if (!cmd.hasOption("s") && !cmd.hasOption("b")) {
                    HelpFormatter formatter = new HelpFormatter();
                    formatter.printHelp(helpMessage, options);
                }
                System.out.println("Loading scenario...");
                if (cmd.hasOption("s")) {
                    System.out.println("Inferring the situation...");
                }
                if (cmd.hasOption("b")) {
                    System.out.println("Inferring vessel behaviors...");
                }
                if (cmd.hasOption("e")) {
                    if (cmd.hasOption("s")) {
                        System.out.println("Explaining inferred situation...");
                    }
                    if (cmd.hasOption("b")) {
                        System.out.println("Explaining inferred behaviors...");
                    }
                }
            }
            else {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp(helpMessage, options);
            }
        }
        catch (ParseException e) {
            System.out.println("Error parsing command-line arguments!");
            System.out.println("Please, follow the instructions below:");
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(helpMessage, options);
            System.exit(1);
        }
    }
}
