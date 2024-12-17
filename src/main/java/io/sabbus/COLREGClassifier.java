package io.sabbus;

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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

import de.derivo.sparqldlapi.Query;
import de.derivo.sparqldlapi.QueryEngine;
import de.derivo.sparqldlapi.QueryResult;
import de.derivo.sparqldlapi.exceptions.QueryEngineException;
import de.derivo.sparqldlapi.exceptions.QueryParserException;

public class COLREGClassifier {

    enum VesselType {
        OWNSHIP,
        TARGET
    }

    IRI ontologyIRI;
    OWLOntology ontology;
    OWLDataFactory factory; OWLOntologyManager manager;
    String[] vesselCategories = {"PowerDrivenVessel", "SailingVessel", "VesselEngagedInFishing", "VesselRestrictedInHerAbilityToManoeuvre", "VesselConstrainedByHerDraught", "VesselNotUnderCommand"};
    String[] situationCategories = {
        "HeadOn", 
        "Crossing", 
        "CrossingOwnshipStandOn", 
        "CrossingOwnshipGiveWay", 
        "Overtaking", 
        "OvertakingOwnshipStandOn", 
        "OvertakingOwnshipGiveWay", 
        "SailingVesselEncounter", 
        "SailingVesselEncounterDifferentSideOwnshipStandOn",
        "SailingVesselEncounterDifferentSideOwnshipGiveWay",
        "SailingVesselEncounterSameSideStarboardOwnshipStandOn",
        "SailingVesselEncounterSameSideStarboardOwnshipGiveWay",
        "SailingVesselEncounterSameSidePortsideOwnshipStandOn",
        "SailingVesselEncounterSameSidePortsideOwnshipGiveWay",
        "DifferentVesselEncounter"};
    String[] availableLights = {
        "masthead_light", 
        "upper_masthead_light", 
        "green_sidelight", 
        "red_sidelight",
        "sternlight", 
        "all_round_white_light", 
        "all_round_green_light", 
        "all_round_red_light"
    };

    public COLREGClassifier(String pathToOntology, String ontologyIRI) {
        try {
            File ontologyFile = new File(pathToOntology);

            this.ontologyIRI = IRI.create(ontologyIRI);
            this.manager = OWLManager.createOWLOntologyManager();
            this.ontology = this.manager.loadOntologyFromOntologyDocument(ontologyFile); this.factory = this.manager.getOWLDataFactory(); }
        catch (OWLOntologyCreationException e) {
            throw new RuntimeException(e);
        }
    }

    public JsonObject classify(JsonObject scenarioJson, boolean printInferences, String sparqlQuery) {
        Set<OWLAxiom> axioms = getScenarioAxioms(scenarioJson);
        this.manager.addAxioms(this.ontology, axioms);

        // try {
        //     manager.saveOntology(ontology);
        // }
        // catch (OWLOntologyStorageException e) {
        //     throw new RuntimeException(e);
        // }

        OpenlletReasoner reasoner = OpenlletReasonerFactory.getInstance().createReasoner(ontology);

        if (sparqlQuery != "") {
            QueryEngine engine = QueryEngine.create(this.manager, reasoner);
            try {
                QueryResult queryResult = engine.execute(Query.create(sparqlQuery));
                System.out.println(queryResult.toString());
            }
            catch(QueryParserException | QueryEngineException e) {
                throw new RuntimeException(e);
            }
        }

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

        if (printInferences) {
            System.out.println(scenarioJson.get("name"));
            System.out.println(scenarioTypes);
            System.out.println(ownshipBehavior);
            System.out.println(targetBehavior);
            System.out.println("\n");
        }

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

    public JsonObject classify(JsonObject scenarioJson, boolean printInferences) {
        return this.classify(scenarioJson, printInferences, "");
    }

    public JsonObject classify(JsonObject scenarioJson, String sparqlQuery) {
        return this.classify(scenarioJson, false, sparqlQuery);
    }

    public JsonObject classify(JsonObject scenarioJson) {
        return this.classify(scenarioJson, false, "");
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

        try {
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
        } catch(Exception e) {
            throw new RuntimeException(e);
        }

        return axioms;
    }

    private Set<OWLAxiom> getVesselAxioms(JsonObject scenarioJson, JsonObject vesselJson, OWLNamedIndividual scenario, VesselType vesselType) throws Exception {
        Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();

        OWLDatatype decimalDatatype = this.factory.getOWLDatatype(OWL2Datatype.XSD_DECIMAL.getIRI());
        OWLDatatype stringDatatype = this.factory.getOWLDatatype(OWL2Datatype.XSD_STRING.getIRI());

        // Get vessel name
        String name = (String) vesselJson.get("name");

        IRI vesselIRI = IRI.create(ontologyIRI + name);
        IRI vesselClassIRI = IRI.create(ontologyIRI + "Vessel");
        IRI hasOwnshipOrTargetIRI = IRI.create(ontologyIRI + ( (vesselType == VesselType.OWNSHIP) ? "hasOwnship" : "hasTarget" ));

        OWLNamedIndividual vessel = this.factory.getOWLNamedIndividual(vesselIRI);
        OWLClass vesselClass = this.factory.getOWLClass(vesselClassIRI);
        OWLObjectProperty hasOwnshipOrTarget = this.factory.getOWLObjectProperty(hasOwnshipOrTargetIRI);

        axioms.add(this.factory.getOWLClassAssertionAxiom(vesselClass, vessel));
        axioms.add(this.factory.getOWLObjectPropertyAssertionAxiom(hasOwnshipOrTarget, scenario, vessel));

        // Get vessel position
        BigDecimal x = (BigDecimal) vesselJson.get("x");
        BigDecimal y = (BigDecimal) vesselJson.get("y");

        if (x != null && y != null) {
            IRI hasXIRI = IRI.create(ontologyIRI + "hasXPosition");
            IRI hasYIRI = IRI.create(ontologyIRI + "hasYPosition");

            OWLDataProperty hasX = this.factory.getOWLDataProperty(hasXIRI);
            OWLDataProperty hasY = this.factory.getOWLDataProperty(hasYIRI);
            OWLLiteral xLit = this.factory.getOWLLiteral(x.toString(), decimalDatatype);
            OWLLiteral yLit = this.factory.getOWLLiteral(y.toString(), decimalDatatype);

            axioms.add(this.factory.getOWLDataPropertyAssertionAxiom(hasX, vessel, xLit));
            axioms.add(this.factory.getOWLDataPropertyAssertionAxiom(hasY, vessel, yLit));
        } else if ((x == null && y != null) || (x != null && y == null)) {
            throw new Exception("Incomplete information in json");
        }

        // Get vessel speed
        BigDecimal spd = (BigDecimal) vesselJson.get("spd");

        if (spd != null) {
            IRI hasSpdIRI = IRI.create(ontologyIRI + "hasSpeed");

            OWLDataProperty hasSpd = this.factory.getOWLDataProperty(hasSpdIRI);
            OWLLiteral spdLit = this.factory.getOWLLiteral(spd.toString(), decimalDatatype);

            axioms.add(this.factory.getOWLDataPropertyAssertionAxiom(hasSpd, vessel, spdLit));
        }

        // Get vessel heading
        BigDecimal hdg = (BigDecimal) vesselJson.get("hdg");

        if (hdg != null) {
            IRI hasHdgIRI = IRI.create(ontologyIRI + "hasHeadingDeg");
            
            OWLDataProperty hasHdg = this.factory.getOWLDataProperty(hasHdgIRI);
            OWLLiteral hdgLit = this.factory.getOWLLiteral(hdg.toString(), decimalDatatype); 

            axioms.add(this.factory.getOWLDataPropertyAssertionAxiom(hasHdg, vessel, hdgLit));
        }

        // Get vessel category
        String vesselCategory = (String) vesselJson.get("category");

        if (vesselCategory != null) {

            List<String> vesselCategories = Arrays.asList(this.vesselCategories);
            if (!vesselCategories.contains(vesselCategory)) {
                throw new Exception("Wrong vessel category");
            }

            IRI vesselCategoryIRI = IRI.create(ontologyIRI + vesselCategory);

            OWLClass vesselCategoryClass = this.factory.getOWLClass(vesselCategoryIRI);

            axioms.add(this.factory.getOWLClassAssertionAxiom(vesselCategoryClass, vessel));
        } else if (vesselCategory == null && vesselType == VesselType.OWNSHIP) {
            throw new Exception("Own ship without a category");
        }

        // Get target ship bearing with respect to own ship
        BigDecimal absBearing = (BigDecimal) vesselJson.get("absolute-bearing-to-ownship");

        if (absBearing != null && vesselType == VesselType.TARGET) {
            IRI hasAbsBearingIRI = IRI.create(ontologyIRI + "hasAbsoluteBearingWithRespectToOwnShip");
            
            OWLDataProperty hasAbsBearing = this.factory.getOWLDataProperty(hasAbsBearingIRI);
            OWLLiteral absBearingLit = this.factory.getOWLLiteral(absBearing.toString(), decimalDatatype); 

            axioms.add(this.factory.getOWLDataPropertyAssertionAxiom(hasAbsBearing, vessel, absBearingLit));
        }

        // Get target ship bearing with respect to own ship
        List<String> lights = (List<String>) vesselJson.get("lights-in-sight");

        if (lights != null && vesselType == VesselType.OWNSHIP) {
            IRI hasLightIRI = IRI.create(ontologyIRI + "hasLightInSight");

            Collections.sort(lights);
            
            for (String light : lights) {

                List<String> availableLights = Arrays.asList(this.availableLights);
                if (!availableLights.contains(light)) {
                    throw new Exception("Wrong light");
                }
            }

            String light = String.join("-", lights);

            OWLDataProperty hasLight = this.factory.getOWLDataProperty(hasLightIRI);
            OWLLiteral lightLit = this.factory.getOWLLiteral(light, stringDatatype); 

            axioms.add(this.factory.getOWLDataPropertyAssertionAxiom(hasLight, vessel, lightLit));
        }

        return axioms;
    }
}
