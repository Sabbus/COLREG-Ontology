package io.sabbus.colregclassifier;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import java.util.HashMap;
import java.util.Set;

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

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.IRI;

import org.semanticweb.owlapi.apibinding.OWLManager;

class COLREGClassifier {

    static String helpMessage = "Ontology Based COLREG Classifier";

    public static void main(String[] args) {
        Option help = Option.builder("h")
                                .longOpt("help")
                                .desc("print help message")
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
                try {
                    OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
                    File ontologyFile = new File("./src/test/resources/owl/colreg-ontology.owl");
                    OWLOntology ontology = manager.loadOntologyFromOntologyDocument(ontologyFile);
                    OWLDataFactory factory = manager.getOWLDataFactory();
                    IRI ontologyIRI = IRI.create("http://unige.it/nicola-sabatino/2024/7/8/colreg-otology/0.0.1");

                    FileReader scenarioReader = new FileReader(cmd.getOptionValue("file"));
                    JsonObject json = (JsonObject) Jsoner.deserialize(scenarioReader);

                    JsonToAxiomsConverter jsonToAxiomsConverter = new JsonToAxiomsConverter(ontologyIRI, factory);
                    Set<OWLAxiom> axioms = jsonToAxiomsConverter.convertToAxioms(json);

                    manager.addAxioms(ontology, axioms);
                    // try {
                    //     manager.saveOntology(ontology);
                    // }
                    // catch (OWLOntologyStorageException e) {
                    //     throw new RuntimeException(e);
                    // }

                    ScenarioClassifier scenarioClassifier = new ScenarioClassifier(ontology, ontologyIRI, factory);
                    HashMap<String, String> scenarioCategory = scenarioClassifier.categorizeScenario();
                    // HashMap<String, String> behaviors = scenarioClassifier.inferBehaviors();

                    System.out.println(axioms);
                    System.out.println(scenarioCategory);
                    // System.out.println(behaviors);

                }
                catch (IOException | JsonException | OWLOntologyCreationException e) {
                    throw new RuntimeException(e);
                }

                if (cmd.hasOption("p")) {
                    // Pretty print output
                    // TODO
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
