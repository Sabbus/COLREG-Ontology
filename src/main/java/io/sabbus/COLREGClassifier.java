package io.sabbus;

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

import java.math.BigDecimal;

import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.JsonException;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;

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

public class COLREGClassifier {

    enum VesselType {
        OWNSHIP,
        TARGET
    }

    IRI ontologyIRI;
    OWLOntology ontology;
    OWLDataFactory factory;
    OWLOntologyManager manager;
    String[] vesselCategories = {"PowerDrivenVessel", "SailingVessel", "VesselEngagedInFishing", "VesselRestrictedInHerAbilityToManoeuvre", "VesselConstrainedByHerDraught", "VesselN otUnderCommand"};
    String[] situationCategories = {"HeadOn", "Crossing", "Overtaking", "SailingVesselEncounter", "DifferentVesselsEncounter"};

    public COLREGClassifier(String pathToOntology, String ontologyIRI) {
        try {
            File ontologyFile = new File(pathToOntology);

            this.ontologyIRI = IRI.create(ontologyIRI);
            this.manager = OWLManager.createOWLOntologyManager();
            this.ontology = this.manager.loadOntologyFromOntologyDocument(ontologyFile);
            this.factory = this.manager.getOWLDataFactory();
        }
        catch (OWLOntologyCreationException e) {
            throw new RuntimeException(e);
        }
    }

    public JsonObject classify(JsonObject scenarioJson) {
        Set<OWLAxiom> axioms = getScenarioAxioms(scenarioJson);
        this.manager.addAxioms(this.ontology, axioms);

        // try {
        //     manager.saveOntology(ontology);
        // }
        // catch (OWLOntologyStorageException e) {
        //     throw new RuntimeException(e);
        // }

        OpenlletReasoner reasoner = OpenlletReasonerFactory.getInstance().createReasoner(ontology);

        JsonObject ownshipJson = (JsonObject) scenarioJson.get("ownship");
        JsonObject targetJson = (JsonObject) scenarioJson.get("target");
        String ownshipName = (String) ownshipJson.get("name");
        String targetName = (String) targetJson.get("name");
        String scenarioName = (String) scenarioJson.get("name");

        OWLNamedIndividual scenario = this.factory.getOWLNamedIndividual(IRI.create(this.ontologyIRI + scenarioName));
        OWLNamedIndividual ownship = this.factory.getOWLNamedIndividual(IRI.create(this.ontologyIRI + ownshipName));
        OWLNamedIndividual target = this.factory.getOWLNamedIndividual(IRI.create(this.ontologyIRI + targetName));
        OWLDataProperty hasBehavior = this.factory.getOWLDataProperty(IRI.create(this.ontologyIRI + "hasBehavior"));

        JsonObject categorizedScenario = new JsonObject();

        NodeSet<OWLClass> scenarioTypes = reasoner.getTypes(scenario, true);
        Set<OWLLiteral> ownshipBehavior = reasoner.getDataPropertyValues(ownship, hasBehavior);
        Set<OWLLiteral> targetBehavior = reasoner.getDataPropertyValues(target, hasBehavior);

        System.out.println(scenarioTypes);
        System.out.println(ownshipBehavior);
        System.out.println(targetBehavior);

        String scenarioType = new String();
        List<String> situationCategories = Arrays.asList(this.situationCategories);
        for (Node<OWLClass> node : scenarioTypes) {
            Set<OWLClass> set = node.getEntities();
            for (OWLClass scenarioType1 : set) {
                String stringID = scenarioType1.toStringID();
                String typeName = stringID.replaceAll(this.ontologyIRI.toString(), "");
                if (situationCategories.contains(typeName)) {
                    scenarioType = typeName;
                    break;
                }
            }
        }
        categorizedScenario.put("category", scenarioType);

        OWLLiteral[] ownshipBehaviorArray = new OWLLiteral[1];
        ownshipBehaviorArray = ownshipBehavior.toArray(ownshipBehaviorArray);
        categorizedScenario.put("ownship-behavior", ownshipBehaviorArray[0].getLiteral());

        OWLLiteral[] targetBehaviorArray = new OWLLiteral[1];
        targetBehaviorArray = targetBehavior.toArray(targetBehaviorArray);
        categorizedScenario.put("target-behavior", targetBehaviorArray[0].getLiteral());

        JsonObject result = new JsonObject();
        result.put("scenario", scenarioJson);
        result.put("classification", categorizedScenario);

        // this.manager.removeAxioms(this.ontology, axioms);

        return result;
    }

    private Set<OWLAxiom> getScenarioAxioms(JsonObject scenarioJson) {
        Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();

        JsonObject ownshipJson = (JsonObject) scenarioJson.get("ownship");
        JsonObject targetJson = (JsonObject) scenarioJson.get("target");
        String ownshipName = (String) ownshipJson.get("name");
        String targetName = (String) targetJson.get("name");
        BigDecimal windDir = (BigDecimal) scenarioJson.get("wind-dir");
        String scenarioName = (String) scenarioJson.get("name");

        IRI ownshipIRI = IRI.create(ontologyIRI + ownshipName);
        IRI targetIRI = IRI.create(ontologyIRI + targetName);
        IRI scenarioClassIRI = IRI.create(ontologyIRI + "Scenario");
        IRI scenarioIRI = IRI.create(ontologyIRI + scenarioName);
        IRI hasWindDirIRI = IRI.create(ontologyIRI + "hasWindDirDeg");

        OWLNamedIndividual ownship = this.factory.getOWLNamedIndividual(ownshipIRI);
        OWLNamedIndividual target = this.factory.getOWLNamedIndividual(targetIRI);
        OWLClass scenarioClass = this.factory.getOWLClass(scenarioClassIRI);
        OWLNamedIndividual scenario = this.factory.getOWLNamedIndividual(scenarioIRI);
        OWLDataProperty hasWindDir = this.factory.getOWLDataProperty(hasWindDirIRI);
        OWLDatatype decimalDatatype = this.factory.getOWLDatatype(OWL2Datatype.XSD_DECIMAL.getIRI());
        OWLLiteral windDirLit = this.factory.getOWLLiteral(windDir.toString(), decimalDatatype);

        Set<OWLAxiom> ownshipAxioms = getVesselAxioms(scenarioJson, ownshipJson, scenario, VesselType.OWNSHIP);
        Set<OWLAxiom> targetAxioms = getVesselAxioms(scenarioJson, targetJson, scenario, VesselType.TARGET);

        // Instantiate scenario
        axioms.add(this.factory.getOWLClassAssertionAxiom(scenarioClass, scenario));
        // Add wind to scenario
        axioms.add(this.factory.getOWLDataPropertyAssertionAxiom(hasWindDir, scenario, windDirLit));
        // Add ownship axioms
        axioms.addAll(ownshipAxioms);
        // Add target axioms
        axioms.addAll(targetAxioms);
        // Add ownship != target != scenario axiom
        axioms.add(this.factory.getOWLDifferentIndividualsAxiom(ownship, target, scenario));

        return axioms;
    }

    private Set<OWLAxiom> getVesselAxioms(JsonObject scenarioJson, JsonObject vesselJson, OWLNamedIndividual scenario, VesselType vesselType) {
        Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();

        String name = (String) vesselJson.get("name");
        BigDecimal x = (BigDecimal) vesselJson.get("x");
        BigDecimal y = (BigDecimal) vesselJson.get("y");
        BigDecimal spd = (BigDecimal) vesselJson.get("spd");
        BigDecimal hdg = (BigDecimal) vesselJson.get("hdg");
        String vesselCategory = (String) vesselJson.get("category");

        List<String> vesselCategories = Arrays.asList(this.vesselCategories);
        if (!vesselCategories.contains(vesselCategory)) {
            System.out.println("[-] Wrong vessel category");
            System.exit(1);
        }

        IRI vesselClassIRI = IRI.create(ontologyIRI + "Vessel");
        IRI vesselCategoryIRI = IRI.create(ontologyIRI + vesselCategory);
        IRI hasXIRI = IRI.create(ontologyIRI + "hasXPosition");
        IRI hasYIRI = IRI.create(ontologyIRI + "hasYPosition");
        IRI hasSpdIRI = IRI.create(ontologyIRI + "hasSpeed");
        IRI hasHdgIRI = IRI.create(ontologyIRI + "hasHeadingDeg");
        IRI vesselIRI = IRI.create(ontologyIRI + name);
        IRI hasOwnshipOrTargetIRI = IRI.create(ontologyIRI + ( (vesselType == VesselType.OWNSHIP) ? "hasOwnship" : "hasTarget" ));

        OWLClass vesselClass = this.factory.getOWLClass(vesselClassIRI);
        OWLClass vesselCategoryClass = this.factory.getOWLClass(vesselCategoryIRI);
        OWLNamedIndividual vessel = this.factory.getOWLNamedIndividual(vesselIRI);
        OWLObjectProperty hasOwnshipOrTarget = this.factory.getOWLObjectProperty(hasOwnshipOrTargetIRI);
        OWLDataProperty hasX = this.factory.getOWLDataProperty(hasXIRI);
        OWLDataProperty hasY = this.factory.getOWLDataProperty(hasYIRI);
        OWLDataProperty hasSpd = this.factory.getOWLDataProperty(hasSpdIRI);
        OWLDataProperty hasHdg = this.factory.getOWLDataProperty(hasHdgIRI);
        OWLDatatype decimalDatatype = this.factory.getOWLDatatype(OWL2Datatype.XSD_DECIMAL.getIRI());
        OWLLiteral xLit = this.factory.getOWLLiteral(x.toString(), decimalDatatype);
        OWLLiteral yLit = this.factory.getOWLLiteral(y.toString(), decimalDatatype);
        OWLLiteral spdLit = this.factory.getOWLLiteral(spd.toString(), decimalDatatype);
        OWLLiteral hdgLit = this.factory.getOWLLiteral(hdg.toString(), decimalDatatype); 

        // Ship instantiation in the ontology
        axioms.add(this.factory.getOWLClassAssertionAxiom(vesselClass, vessel));
        axioms.add(this.factory.getOWLClassAssertionAxiom(vesselCategoryClass, vessel));
        // Ship propertyies
        axioms.add(this.factory.getOWLDataPropertyAssertionAxiom(hasX, vessel, xLit));
        axioms.add(this.factory.getOWLDataPropertyAssertionAxiom(hasY, vessel, yLit));
        axioms.add(this.factory.getOWLDataPropertyAssertionAxiom(hasSpd, vessel, spdLit));
        axioms.add(this.factory.getOWLDataPropertyAssertionAxiom(hasHdg, vessel, hdgLit));
        // Add vessel to scenario
        axioms.add(this.factory.getOWLObjectPropertyAssertionAxiom(hasOwnshipOrTarget, scenario, vessel));

        return axioms;
    }
}
