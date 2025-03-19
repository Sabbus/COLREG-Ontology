import json

import jsonschema
import os

import owlready2 as owl

class Classifier:
    def __init__(self, ontology):
        self.ontology = owl.get_ontology(os.path.dirname(__file__) + '/COLREG_ontology_no_exactly.owl').load()
        self.__inferences = owl.get_ontology("http://my-inferences.io")

    def classify(self, situation):
        with open(situation, 'r') as f:
            situation = json.loads(f.read())

        try:
            self.__check_situation(situation)
        except Exception:
            print("[-] Wrong format for situation file")
            return
        else:
            print("[+] Scenario file loaded")

        try:
            self.__instantiate_situation(situation)
        except Exception as e:
            print(f"[-] Failed to add situation to ontology with the following exception: {e}")
            return
        else:
            print("[+] Situation added to ontology")

        result = self.__classify(situation)

        print(result)

    def __check_situation(self, situation):
        with open(os.path.dirname(__file__) + '/situation_schema.json', 'r') as f:
            schema = json.loads(f.read())

        jsonschema.validate(instance=situation, schema=schema)

    def __instantiate_situation(self, situation):
        # Instantiate own ship
        ownship = self.ontology.Vessel(situation['own-ship']['name'],
                                       namespace=self.ontology)

        # Speed
        ownship.hasSpeedOverGround = situation['own-ship']['sog']

        # Heading
        ownship.hasHeadingDeg = situation['own-ship']['heading']

        # Bearing
        ownship.hasRelativeBearingWithRespectToTargetShip = situation['target-ship']['bearing-of-other-vessel']

        # Add catgeory, if present
        if 'category' in situation['own-ship']:
            ownship.is_a.append(self.ontology[situation['own-ship']['category']])

        # Instantiate target ship
        targetship = self.ontology.Vessel(situation['target-ship']['name'],
                                       namespace=self.ontology)

        # Speed
        targetship.hasSpeedOverGround = situation['target-ship']['sog']

        # Heading
        targetship.hasHeadingDeg = situation['target-ship']['heading']

        # Bearing
        targetship.hasRelativeBearingWithRespectToTargetShip = situation['own-ship']['bearing-of-other-vessel']

        # Add catgeory, if present
        if 'category' in situation['target-ship']:
            targetship.is_a.append(self.ontology[situation['target-ship']['category']])

        # Instantiate situation
        situation_ind = self.ontology.Situation(situation['name'],
                                                namespace=self.ontology)

        # Assign own ship to situation
        situation_ind.hasOwnShip = ownship

        # Assign target ship to situation
        situation_ind.hasTargetShip = targetship

    def __classify(self, situation):
        result = {}

        with self.__inferences:
            owl.sync_reasoner_pellet(infer_property_values=True,
                                     infer_data_property_values=True,
                                     debug=2)
        
        print(self.__inferences[situation['name']].is_a)
        
        return result

