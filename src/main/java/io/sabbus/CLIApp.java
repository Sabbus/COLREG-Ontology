package io.sabbus;

import java.util.Properties;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.IOException;

import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.JsonException;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;

class CLIApp {

    static final String helpMessage = "Ontology Based COLREG Classifier";

    // ANSI sequences
    static final String RESET = "\033[0m";
    static final String GREEN = "\033[1;32m";

    public static void main(String[] args) {
        Properties properties = new Properties();

        try {
            InputStream configFile = new FileInputStream("./src/main/resources/config.properties");
            properties.load(configFile);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }

        final String pathToOntology = properties.getProperty("ontologyFilePath");
        final String ontologyIRI = properties.getProperty("ontologyIRI");

        Option help = Option.builder("h")
                                .longOpt("help")
                                .desc("print this help message")
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
                                .longOpt("output")
                                .hasArg()
                                .desc("path to output JSON file")
                                .build();
        Options options = new Options();
        options.addOption(help);
        options.addOption(prettyPrint);
        options.addOption(file);
        options.addOption(output);

        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(options, args);

            if (cmd.hasOption("h")) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp(helpMessage, options);
            }
            else if (cmd.hasOption("file") && cmd.hasOption("o")) {
                    JsonObject result = new JsonObject();
                try {
                    FileReader scenarioFile = new FileReader(cmd.getOptionValue("file"));

                    JsonObject scenarioJson = (JsonObject) Jsoner.deserialize(scenarioFile);

                    COLREGClassifier classifier = new COLREGClassifier(pathToOntology, ontologyIRI);
                    result = classifier.classify(scenarioJson);

                    FileWriter fileWriter = new FileWriter(cmd.getOptionValue("output"));
                    Jsoner.serialize(result, fileWriter);
                    fileWriter.close();
                }
                catch (IOException | JsonException e) {
                    throw new RuntimeException(e);
                }
                
                if (cmd.hasOption("p")) {
                    JsonObject scenarioJson = (JsonObject) result.get("scenario");
                    JsonObject ownshipJson = (JsonObject) scenarioJson.get("ownship");
                    JsonObject targetJson = (JsonObject) scenarioJson.get("target");
                    JsonObject classificationJson = (JsonObject) result.get("classification");

                    String scenarioName = (String) scenarioJson.get("name");
                    String ownshipName = (String) ownshipJson.get("name");
                    String targetName = (String) targetJson.get("name");
                    JsonArray situation = (JsonArray) classificationJson.get("category");
                    JsonArray ownshipBehavior = (JsonArray) classificationJson.get("ownship-behavior");
                    JsonArray targetBehavior = (JsonArray) classificationJson.get("target-behavior");

                    System.out.println("Classification result for scenario: " + GREEN + scenarioName + RESET);
                    System.out.println("COLREG situation: " + GREEN + situation.toString() + RESET + "\n");
                    System.out.println("Classification result for vessel: " + GREEN + ownshipName + RESET);
                    System.out.println("Behavior: " + GREEN + ownshipBehavior.toString() + RESET + "\n");
                    System.out.println("Classification result for vessel: " + GREEN + targetName + RESET);
                    System.out.println("Behavior: " + GREEN + targetBehavior.toString() + RESET + "\n");
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
