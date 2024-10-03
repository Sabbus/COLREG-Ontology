package io.sabbus;

import java.io.File;
import java.io.IOException;

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

    final IRI ontologyIRI = IRI.create("http://unige.it/nicola-sabatino/2024/7/8/colreg-ontology/0.0.1");

    OWLOntology ontology;
    OWLDataFactory factory;
    OWLOntologyManager manager;

    OpenlletReasoner reasoner;

    public COLREGClassifier(String pathToOntology) {
        try {
            File ontologyFile = new File(pathToOntology);

            this.manager = OWLManager.createOWLOntologyManager();
            this.ontology = this.manager.loadOntologyFromOntologyDocument(ontologyFile);
            this.factory = this.manager.getOWLDataFactory();

            this.reasoner = OpenlletReasonerFactory.getInstance().createReasoner(ontology);
        }
        catch (OWLOntologyCreationException e) {
            throw new RuntimeException(e);
        }
    }

    public JsonObject classify(JsonObject scenarioJson) {
        Set<OWLAxiom> axioms = getScenarioAxioms(scenarioJson);
        this.manager.addAxioms(this.ontology, axioms);

        try {
            this.manager.saveOntology(ontology);
        }
        catch (OWLOntologyStorageException e) {
            throw new RuntimeException(e);
        }

        JsonObject ownshipJson = (JsonObject) scenarioJson.get("ownship");
        JsonObject targetJson = (JsonObject) scenarioJson.get("target");
        String ownshipName = (String) ownshipJson.get("name");
        String targetName = (String) targetJson.get("name");
        String scenarioName = (String) scenarioJson.get("name");

        OWLNamedIndividual scenario = this.factory.getOWLNamedIndividual(IRI.create(this.ontologyIRI + "#" + scenarioName));
        OWLNamedIndividual ownship = this.factory.getOWLNamedIndividual(IRI.create(this.ontologyIRI + "#" + ownshipName));
        OWLNamedIndividual target = this.factory.getOWLNamedIndividual(IRI.create(this.ontologyIRI + "#" + targetName));
        OWLObjectProperty hasBehavior = this.factory.getOWLObjectProperty(IRI.create(this.ontologyIRI + "#hasBehavior"));

        JsonObject categorizedScenario = new JsonObject();

        NodeSet<OWLClass> scenarioTypes = reasoner.getTypes(scenario, true);
        NodeSet<OWLNamedIndividual> ownshipBehaviors = reasoner.getObjectPropertyValues(ownship, hasBehavior);
        NodeSet<OWLNamedIndividual> targetBehaviors = reasoner.getObjectPropertyValues(target, hasBehavior);

        DefaultPrefixManager prefixManager = new DefaultPrefixManager(this.ontologyIRI.toString() + "#");

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

        JsonObject result = new JsonObject();
        result.put("scenario", scenarioJson);
        result.put("classification", categorizedScenario);

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

        IRI ownshipIRI = IRI.create(ontologyIRI + "#" + ownshipName);
        IRI targetIRI = IRI.create(ontologyIRI + "#" + targetName);
        IRI scenarioClassIRI = IRI.create(ontologyIRI + "#Scenario");
        IRI scenarioIRI = IRI.create(ontologyIRI + "#" + scenarioName);
        IRI hasWindDirIRI = IRI.create(ontologyIRI + "#hasWindDirDeg");

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
        // Add ownship != target axiom
        axioms.add(this.factory.getOWLDifferentIndividualsAxiom(ownship, target));

        return axioms;
    }

    private Set<OWLAxiom> getVesselAxioms(JsonObject scenarioJson, JsonObject vesselJson, OWLNamedIndividual scenario, VesselType vesselType) {
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

        OWLClass vesselClass = this.factory.getOWLClass(vesselClassIRI);
        OWLNamedIndividual vessel = this.factory.getOWLNamedIndividual(vesselIRI);
        OWLObjectProperty hasVesselType = this.factory.getOWLObjectProperty(hasVesselTypeIRI);
        OWLObjectProperty hasEq = this.factory.getOWLObjectProperty(hasEqIRI);
        OWLObjectProperty hasSt = this.factory.getOWLObjectProperty(hasStIRI);
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
        // Ship propertyies
        axioms.add(this.factory.getOWLDataPropertyAssertionAxiom(hasX, vessel, xLit));
        axioms.add(this.factory.getOWLDataPropertyAssertionAxiom(hasY, vessel, yLit));
        axioms.add(this.factory.getOWLDataPropertyAssertionAxiom(hasSpd, vessel, spdLit));
        axioms.add(this.factory.getOWLDataPropertyAssertionAxiom(hasHdg, vessel, hdgLit));
        // Add vessel to scenario
                axioms.add(this.factory.getOWLObjectPropertyAssertionAxiom(hasVesselType, scenario, vessel));

        // Add equipments to vessel
        Set<OWLNamedIndividual> eqsInds = new HashSet<OWLNamedIndividual>();
        if (!eqs.isEmpty()) {
            for (Object eq : eqs) {
                eqsInds.add(this.factory.getOWLNamedIndividual(IRI.create(ontologyIRI + "#" + eq.toString())));
                axioms.add(this.factory.getOWLObjectPropertyAssertionAxiom(hasEq, vessel, this.factory.getOWLNamedIndividual(IRI.create(ontologyIRI + "#" + eq.toString()))));
            }
        }
        OWLObjectOneOf eqsNom = this.factory.getOWLObjectOneOf(eqsInds);
        OWLObjectAllValuesFrom restriction = this.factory.getOWLObjectAllValuesFrom(hasEq, eqsNom);
        axioms.add(this.factory.getOWLClassAssertionAxiom(restriction, vessel));
        // Add Status to vessel
        if (!status.isEmpty()) {
            axioms.add(this.factory.getOWLObjectPropertyAssertionAxiom(hasSt, vessel, this.factory.getOWLNamedIndividual(IRI.create(ontologyIRI + "#" + status))));
        }
        else {
            IRI statusClassIRI = IRI.create(ontologyIRI + "#Status");
            OWLClass statusClass = this.factory.getOWLClass(statusClassIRI);
            OWLClassExpression notSt = this.factory.getOWLObjectComplementOf(statusClass);
            OWLObjectAllValuesFrom restrictionStatus = this.factory.getOWLObjectAllValuesFrom(hasSt, notSt);
            axioms.add(this.factory.getOWLClassAssertionAxiom(restrictionStatus, vessel));
        }

        return axioms;
    }
}
