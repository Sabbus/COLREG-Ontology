package io.sabbus.colregclassifier;

import java.util.HashSet;
import java.util.Set;

import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.JsonKey;
import com.github.cliftonlabs.json_simple.JsonObject;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectOneOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.IRI;

import org.semanticweb.owlapi.vocab.OWL2Datatype;

import java.math.BigDecimal;

public class JsonToAxiomsConverter {

    IRI ontologyIRI;
    OWLDataFactory factory;

    enum VesselType {
        OWNSHIP,
        TARGET
    }

    public JsonToAxiomsConverter(IRI ontologyIRIPar, OWLDataFactory factoryPar) {
        ontologyIRI = ontologyIRIPar;
        factory = factoryPar;
    }

    public Set<OWLAxiom> convertToAxioms(JsonObject json) {
        Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();

        JsonObject ownshipJson = (JsonObject) json.get("ownship");
        JsonObject targetJson = (JsonObject) json.get("target");
        BigDecimal windDir = (BigDecimal) json.get("wind-dir");

        IRI scenarioClassIRI = IRI.create(ontologyIRI + "#Scenario");
        IRI scenarioIRI = IRI.create(ontologyIRI + "#scenario");
        IRI hasWindDirIRI = IRI.create(ontologyIRI + "#hasWindDirDeg");

        Set<OWLAxiom> ownshipAxioms = getAxiomsFromVesselJson(ownshipJson, VesselType.OWNSHIP);
        Set<OWLAxiom> targetAxioms = getAxiomsFromVesselJson(targetJson, VesselType.TARGET);
        OWLClass scenarioClass = factory.getOWLClass(scenarioClassIRI);
        OWLNamedIndividual scenario = factory.getOWLNamedIndividual(scenarioIRI);
        OWLDataProperty hasWindDir = factory.getOWLDataProperty(hasWindDirIRI);
        OWLDatatype decimalDatatype = factory.getOWLDatatype(OWL2Datatype.XSD_DECIMAL.getIRI());
        OWLLiteral windDirLit = factory.getOWLLiteral(windDir.toString(), decimalDatatype);

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

    private Set<OWLAxiom> getAxiomsFromVesselJson(JsonObject vesselClassJson, VesselType vesselClassType) {
        Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();

        String name = (String) vesselClassJson.get("name");
        BigDecimal x = (BigDecimal) vesselClassJson.get("x");
        BigDecimal y = (BigDecimal) vesselClassJson.get("y");
        BigDecimal spd = (BigDecimal) vesselClassJson.get("spd");
        BigDecimal hdg = (BigDecimal) vesselClassJson.get("hdg");
        JsonArray eqs = (JsonArray) vesselClassJson.get("equipments");
        String status = (String) vesselClassJson.get("status");

        IRI vesselClassClassIRI = IRI.create(ontologyIRI + "#Vessel");
        IRI hasEqIRI = IRI.create(ontologyIRI + "#hasEquipment");
        IRI hasStIRI = IRI.create(ontologyIRI + "#hasStatus");
        IRI hasXIRI = IRI.create(ontologyIRI + "#hasXPosition");
        IRI hasYIRI = IRI.create(ontologyIRI + "#hasYPosition");
        IRI hasSpdIRI = IRI.create(ontologyIRI + "#hasSpeed");
        IRI hasHdgIRI = IRI.create(ontologyIRI + "#hasHeadingDeg");
        IRI scenarioIRI = IRI.create(ontologyIRI + "#scenario");
        IRI vesselIRI = IRI.create(ontologyIRI + "#" + name);
        IRI hasVesselTypeIRI = IRI.create(ontologyIRI + ( (vesselClassType == VesselType.OWNSHIP) ? "#hasOwnvessel" : "#hasTargetShip" ));

        OWLClass vesselClass = factory.getOWLClass(vesselClassClassIRI);
        OWLNamedIndividual vessel = factory.getOWLNamedIndividual(vesselIRI);
        OWLNamedIndividual scenario = factory.getOWLNamedIndividual(scenarioIRI);
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
        if (status != "") {
            axioms.add(factory.getOWLObjectPropertyAssertionAxiom(hasSt, vessel, factory.getOWLNamedIndividual(IRI.create(ontologyIRI + "#" + status))));
        }

        return axioms;
    }
}
