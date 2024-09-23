package io.sabbus.colregclassifier;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import java.math.BigDecimal;

import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.JsonKey;
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
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectComplementOf;
import org.semanticweb.owlapi.model.OWLObjectOneOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.IRI;

import org.semanticweb.owlapi.vocab.OWL2Datatype;

import org.semanticweb.owlapi.apibinding.OWLManager;

import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;

import org.semanticweb.owlapi.util.DefaultPrefixManager;

import openllet.owlapi.OpenlletReasoner;
import openllet.owlapi.OpenlletReasonerFactory;

class COLREGClassifier {

    static String helpMessage = "Ontology Based COLREG Classifier";
    static String pathToOntology = "./src/test/resources/owl/colreg-ontology.owl";
    static IRI ontologyIRI = IRI.create("http://unige.it/nicola-sabatino/2024/7/8/colreg-ontology/0.0.1");

    enum VesselType {
        OWNSHIP,
        TARGET
    }

    public static void main(String[] args) {
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
                JsonObject categorizedScenario = new JsonObject();
                JsonObject json = new JsonObject();
                JsonObject result = new JsonObject();
                try {
                    OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
                    File ontologyFile = new File(pathToOntology);
                    OWLOntology ontology = manager.loadOntologyFromOntologyDocument(ontologyFile);
                    OWLDataFactory factory = manager.getOWLDataFactory();

                    FileReader scenarioReader = new FileReader(cmd.getOptionValue("file"));
                    json = (JsonObject) Jsoner.deserialize(scenarioReader);

                    Set<OWLAxiom> axioms = getScenarioAxioms(factory, json);
                    manager.addAxioms(ontology, axioms);

                    categorizedScenario = classify(factory, ontology, json);

                    result.put("scenario", json);
                    result.put("classification", categorizedScenario);

                    FileWriter fileWriter = new FileWriter(cmd.getOptionValue("output"));
                    Jsoner.serialize(result, fileWriter);

                    System.out.println(result);
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

    private static Set<OWLAxiom> getScenarioAxioms(OWLDataFactory factory, JsonObject json) {
        Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();

        JsonObject ownshipJson = (JsonObject) json.get("ownship");
        JsonObject targetJson = (JsonObject) json.get("target");
        BigDecimal windDir = (BigDecimal) json.get("wind-dir");
        String scenarioName = (String) json.get("name");

        IRI scenarioClassIRI = IRI.create(ontologyIRI + "#Scenario");
        IRI scenarioIRI = IRI.create(ontologyIRI + "#" + scenarioName);
        IRI hasWindDirIRI = IRI.create(ontologyIRI + "#hasWindDirDeg");

        OWLClass scenarioClass = factory.getOWLClass(scenarioClassIRI);
        OWLNamedIndividual scenario = factory.getOWLNamedIndividual(scenarioIRI);
        OWLDataProperty hasWindDir = factory.getOWLDataProperty(hasWindDirIRI);
        OWLDatatype decimalDatatype = factory.getOWLDatatype(OWL2Datatype.XSD_DECIMAL.getIRI());
        OWLLiteral windDirLit = factory.getOWLLiteral(windDir.toString(), decimalDatatype);

        Set<OWLAxiom> ownshipAxioms = getVesselAxioms(factory, ownshipJson, scenario, VesselType.OWNSHIP);
        Set<OWLAxiom> targetAxioms = getVesselAxioms(factory, targetJson, scenario, VesselType.TARGET);

        // Instantiate scenario
        axioms.add(factory.getOWLClassAssertionAxiom(scenarioClass, scenario));
        // Add wind to scenario
        axioms.add(factory.getOWLDataPropertyAssertionAxiom(hasWindDir, scenario, windDirLit));
        // Add ownship axioms
        axioms.addAll(ownshipAxioms);
        // Add target axioms
        axioms.addAll(targetAxioms);

        return axioms;
    }

    private static Set<OWLAxiom> getVesselAxioms(OWLDataFactory factory, JsonObject vesselJson, OWLNamedIndividual scenario, VesselType vesselType) {
        Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();

        String name = (String) vesselJson.get("name");
        BigDecimal x = (BigDecimal) vesselJson.get("x");
        BigDecimal y = (BigDecimal) vesselJson.get("y");
        BigDecimal spd = (BigDecimal) vesselJson.get("spd");
        BigDecimal hdg = (BigDecimal) vesselJson.get("hdg");
        JsonArray eqs = (JsonArray) vesselJson.get("equipments");
        String status = (String) vesselJson.get("status");

        IRI vesselClassIRI = IRI.create(ontologyIRI + "#Vessel");
        IRI hasEqIRI = IRI.create(ontologyIRI + "#hasEquipment");
        IRI hasStIRI = IRI.create(ontologyIRI + "#hasStatus");
        IRI hasXIRI = IRI.create(ontologyIRI + "#hasXPosition");
        IRI hasYIRI = IRI.create(ontologyIRI + "#hasYPosition");
        IRI hasSpdIRI = IRI.create(ontologyIRI + "#hasSpeed");
        IRI hasHdgIRI = IRI.create(ontologyIRI + "#hasHeadingDeg");
        IRI vesselIRI = IRI.create(ontologyIRI + "#" + name);
        IRI hasVesselTypeIRI = IRI.create(ontologyIRI + ( (vesselType == VesselType.OWNSHIP) ? "#hasOwnship" : "#hasTargetShip" ));

        OWLClass vesselClass = factory.getOWLClass(vesselClassIRI);
        OWLNamedIndividual vessel = factory.getOWLNamedIndividual(vesselIRI);
        OWLObjectProperty hasVesselType = factory.getOWLObjectProperty(hasVesselTypeIRI);
        OWLObjectProperty hasEq = factory.getOWLObjectProperty(hasEqIRI);
        OWLObjectProperty hasSt = factory.getOWLObjectProperty(hasStIRI);
        OWLDataProperty hasX = factory.getOWLDataProperty(hasXIRI);
        OWLDataProperty hasY = factory.getOWLDataProperty(hasYIRI);
        OWLDataProperty hasSpd = factory.getOWLDataProperty(hasSpdIRI);
        OWLDataProperty hasHdg = factory.getOWLDataProperty(hasHdgIRI);
        OWLDatatype decimalDatatype = factory.getOWLDatatype(OWL2Datatype.XSD_DECIMAL.getIRI());
        OWLLiteral xLit = factory.getOWLLiteral(x.toString(), decimalDatatype);
        OWLLiteral yLit = factory.getOWLLiteral(y.toString(), decimalDatatype);
        OWLLiteral spdLit = factory.getOWLLiteral(spd.toString(), decimalDatatype);
        OWLLiteral hdgLit = factory.getOWLLiteral(hdg.toString(), decimalDatatype);

        // Ship instantiation in the ontology
        axioms.add(factory.getOWLClassAssertionAxiom(vesselClass, vessel));
        // Ship propertyies
        axioms.add(factory.getOWLDataPropertyAssertionAxiom(hasX, vessel, xLit));
        axioms.add(factory.getOWLDataPropertyAssertionAxiom(hasY, vessel, yLit));
        axioms.add(factory.getOWLDataPropertyAssertionAxiom(hasSpd, vessel, spdLit));
        axioms.add(factory.getOWLDataPropertyAssertionAxiom(hasHdg, vessel, hdgLit));
        // Add vessel to scenario
        axioms.add(factory.getOWLObjectPropertyAssertionAxiom(hasVesselType, scenario, vessel));

        // Add equipments to vessel
        Set<OWLNamedIndividual> eqsInds = new HashSet<OWLNamedIndividual>();
        if (!eqs.isEmpty()) {
            for (Object eq : eqs) {
                eqsInds.add(factory.getOWLNamedIndividual(IRI.create(ontologyIRI + "#" + eq.toString())));
                axioms.add(factory.getOWLObjectPropertyAssertionAxiom(hasEq, vessel, factory.getOWLNamedIndividual(IRI.create(ontologyIRI + "#" + eq.toString()))));
            }
        }
        OWLObjectOneOf eqsNom = factory.getOWLObjectOneOf(eqsInds);
        OWLObjectAllValuesFrom restriction = factory.getOWLObjectAllValuesFrom(hasEq, eqsNom);
        axioms.add(factory.getOWLClassAssertionAxiom(restriction, vessel));
        // Add Status to vessel
        if (!status.isEmpty()) {
            axioms.add(factory.getOWLObjectPropertyAssertionAxiom(hasSt, vessel, factory.getOWLNamedIndividual(IRI.create(ontologyIRI + "#" + status))));
        }
        else {
            IRI statusClassIRI = IRI.create(ontologyIRI + "#Status");
            OWLClass statusClass = factory.getOWLClass(statusClassIRI);
            OWLClassExpression notSt = factory.getOWLObjectComplementOf(statusClass);
            OWLObjectAllValuesFrom restrictionStatus = factory.getOWLObjectAllValuesFrom(hasSt, notSt);
            axioms.add(factory.getOWLClassAssertionAxiom(restrictionStatus, vessel));
        }

        return axioms;
    }
    private static JsonObject classify(OWLDataFactory factory, OWLOntology ontology, JsonObject json) {
        JsonObject ownshipJson = (JsonObject) json.get("ownship");
        JsonObject targetJson = (JsonObject) json.get("target");
        String ownshipName = (String) ownshipJson.get("name");
        String targetName = (String) targetJson.get("name");
        String scenarioName = (String) json.get("name");

        OWLNamedIndividual scenario = factory.getOWLNamedIndividual(IRI.create(ontologyIRI + "#" + scenarioName));
        OWLNamedIndividual ownship = factory.getOWLNamedIndividual(IRI.create(ontologyIRI + "#" + ownshipName));
        OWLNamedIndividual target = factory.getOWLNamedIndividual(IRI.create(ontologyIRI + "#" + targetName));
        OWLObjectProperty hasBehavior = factory.getOWLObjectProperty(IRI.create(ontologyIRI + "#hasBehavior"));

        OpenlletReasoner reasoner = OpenlletReasonerFactory.getInstance().createReasoner(ontology);
        JsonObject categorizedScenario = new JsonObject();

        NodeSet<OWLClass> scenarioTypes = reasoner.getTypes(scenario, true);
        NodeSet<OWLNamedIndividual> ownshipBehaviors = reasoner.getObjectPropertyValues(ownship, hasBehavior);
        NodeSet<OWLNamedIndividual> targetBehaviors = reasoner.getObjectPropertyValues(target, hasBehavior);

        DefaultPrefixManager prefixManager = new DefaultPrefixManager(ontologyIRI.toString() + "#");

        JsonArray types = new JsonArray();
        for (Node<OWLClass> node : scenarioTypes) {
            Set<OWLClass> set = node.getEntities();
            for (OWLClass scenarioType : set) {
                types.add(prefixManager.getShortForm(scenarioType));
            }
        }
        categorizedScenario.put("category", types);

        JsonArray oBehaviors = new JsonArray();
        for (Node<OWLNamedIndividual> node : ownshipBehaviors) {
            Set<OWLNamedIndividual> set = node.getEntities();
            for (OWLNamedIndividual behavior : set) {
                oBehaviors.add(prefixManager.getShortForm(behavior));
            }
        }
        categorizedScenario.put("ownship-behavior", oBehaviors);

        JsonArray tBehaviors = new JsonArray();
        for (Node<OWLNamedIndividual> node : targetBehaviors) {
            Set<OWLNamedIndividual> set = node.getEntities();
            for (OWLNamedIndividual behavior : set) {
                tBehaviors.add(prefixManager.getShortForm(behavior));
            }
        }
        categorizedScenario.put("target-behavior", tBehaviors);

        return categorizedScenario;
    }
}
