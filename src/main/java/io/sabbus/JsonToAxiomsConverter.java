package io.sabbus.colregclassifier;

import java.util.HashSet;
import java.util.Set;

import com.github.cliftonlabs.json_simple.JsonKey;
import com.github.cliftonlabs.json_simple.JsonObject;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.IRI;

import org.semanticweb.owlapi.vocab.OWL2Datatype;

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
        Float windDir = (Float) json.get("wind-dir");

        IRI scenarioIRI = IRI.create(ontologyIRI + "#Scenario");
        IRI scenIRI = IRI.create(ontologyIRI + "#scenario");
        IRI hasWindDirIRI = IRI.create(ontologyIRI + "#hasWindDirDeg");

        Set<OWLAxiom> ownshipAxioms = getAxiomsFromVesselJson(ownshipJson, VesselType.OWNSHIP);
        Set<OWLAxiom> targetAxioms = getAxiomsFromVesselJson(targetJson, VesselType.TARGET);
        OWLNamedIndividual scenario = factory.getOWLNamedIndividual(scenarioIRI);
        OWLDataProperty hasWindDir = factory.getOWLDataProperty(hasWindDirIRI);
        OWLDatatype decimalDatatype = factory.getOWLDatatype(OWL2Datatype.XSD_DECIMAL.getIRI());
        OWLLiteral windDirLit = factory.getOWLLiteral(windDir.toString(), decimalDatatype);

        axioms.addAll(ownshipAxioms);
        axioms.addAll(targetAxioms);
        axioms.add(factory.getOWLDataPropertyAssertionAxiom(hasWindDir, scenario, windDirLit));

        return axioms;
    }

    private Set<OWLAxiom> getAxiomsFromVesselJson(JsonObject vesselJson, VesselType vesselType) {
        Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();

        String name = (String) vesselJson.get("name");
        Float x = (Float) vesselJson.get("x");
        Float y = (Float) vesselJson.get("y");
        Float spd = (Float) vesselJson.get("spd");
        Float hdg = (Float) vesselJson.get("hdg");
        Set<String> eqs = (Set<String>) vesselJson.get("equipments");
        String status = (String) vesselJson.get("status");

        IRI vesselIRI = IRI.create(ontologyIRI + "#Vessel");
        IRI hasEqIRI = IRI.create(ontologyIRI + "#hasEquipment");
        IRI hasStIRI = IRI.create(ontologyIRI + "#hasStatus");
        IRI hasXIRI = IRI.create(ontologyIRI + "#hasXPosition");
        IRI hasYIRI = IRI.create(ontologyIRI + "#hasYPosition");
        IRI hasSpdIRI = IRI.create(ontologyIRI + "#hasSpeed");
        IRI hasHdgIRI = IRI.create(ontologyIRI + "#hasHeadingDeg");
        IRI scenarioIRI = IRI.create(ontologyIRI + "#scenario");
        IRI shipIRI = IRI.create(ontologyIRI + "#" + name);
        IRI hasVesselTypeIRI = IRI.create(ontologyIRI + ( (vesselType == VesselType.OWNSHIP) ? "#hasOwnship" : "#hasTargetShip" ));

        OWLClass vessel = factory.getOWLClass(vesselIRI);
        OWLNamedIndividual ship = factory.getOWLNamedIndividual(shipIRI);
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
        axioms.add(factory.getOWLClassAssertionAxiom(vessel, ship));
        // Ship propertyies
        axioms.add(factory.getOWLDataPropertyAssertionAxiom(hasX, ship, xLit));
        axioms.add(factory.getOWLDataPropertyAssertionAxiom(hasY, ship, yLit));
        axioms.add(factory.getOWLDataPropertyAssertionAxiom(hasSpd, ship, spdLit));
        axioms.add(factory.getOWLDataPropertyAssertionAxiom(hasHdg, ship, hdgLit));
        // Add ship to scenario
        axioms.add(factory.getOWLObjectPropertyAssertionAxiom(hasVesselType, scenario, ship));

        // Add equipments to ship
        if (!eqs.isEmpty()) {
            for (String eq : eqs) {
                axioms.add(factory.getOWLObjectPropertyAssertionAxiom(hasEq, ship, factory.getOWLNamedIndividual(IRI.create(ontologyIRI + "#" + eq))));
            }
        }
        // Add Status to ship
        if (status != "") {
            axioms.add(factory.getOWLObjectPropertyAssertionAxiom(hasSt, ship, factory.getOWLNamedIndividual(IRI.create(ontologyIRI + "#" + status))));
        }

        return axioms;
    }
}
