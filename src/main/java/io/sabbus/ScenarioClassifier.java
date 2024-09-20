package io.sabbus.colregclassifier;

import java.util.HashMap;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;

import openllet.owlapi.OpenlletReasoner;
import openllet.owlapi.OpenlletReasonerFactory;

public class ScenarioClassifier {

    OpenlletReasoner reasoner;
    OWLNamedIndividual scenario;

    public ScenarioClassifier(OWLOntology ontology, IRI ontologyIRI, OWLDataFactory factory) {
        scenario = factory.getOWLNamedIndividual(IRI.create(ontologyIRI + "#scenario"));
        reasoner = OpenlletReasonerFactory.getInstance().createReasoner(ontology);
    }

    public HashMap<String, String> categorizeScenario() {
        HashMap<String, String> category = new HashMap<String, String>();

        category.put("category", reasoner.getTypes(scenario, true).toString());

        return category;
    }

    // public HashMap<String, String> inferBehaviors() {
    // }
}
