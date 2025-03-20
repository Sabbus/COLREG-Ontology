import json
import os
import sys

import jsonschema
import owlready2 as owl

class Classifier:
    def __init__(self, is_verbose=False):
        self.ontology = owl.get_ontology(os.path.dirname(__file__) + '/colreg_ontology.owl').load()
        self.is_verbose = is_verbose
        self.ownship = None
        self.targetship = None
        self.situation = None

    def classify(self, situation):
        # Open situation file
        with open(situation, 'r') as f:
            situation = json.loads(f.read())

        # Check if situation file format is ok
        self.__check_situation(situation)

        # Instantiate situation in the ontology
        self.__instantiate_situation(situation)

        # Classify
        result = self.__classify(situation)

        return result

    def __check_situation(self, situation):
        with open(os.path.dirname(__file__) + '/situation_schema.json', 'r') as f:
            schema = json.loads(f.read())

        try:
            jsonschema.validate(instance=situation, schema=schema)
        except Exception:
            print("[-] Wrong format for situation file")
            sys.exit()
        else:
            if self.is_verbose:
                print("[+] Scenario file loaded")

    def __instantiate_situation(self, situation):
        try:
            # Instantiate own ship
            self.ownship = self.ontology.Vessel(situation['own-ship']['name'],
                                           namespace=self.ontology)

            # Speed
            self.ownship.hasSpeedOverGround = situation['own-ship']['sog']

            # Heading
            self.ownship.hasHeading = situation['own-ship']['heading']

            # Bearing
            self.ownship.hasRelativeBearingWithRespectToTargetShip = situation['target-ship']['bearing-of-other-vessel']

            # Add catgeory, if present
            if 'category' in situation['own-ship']:
                self.ownship.is_a.append(self.ontology[situation['own-ship']['category']])

            # Instantiate target ship
            self.targetship = self.ontology.Vessel(situation['target-ship']['name'],
                                           namespace=self.ontology)

            # Speed
            self.targetship.hasSpeedOverGround = situation['target-ship']['sog']

            # Heading
            self.targetship.hasHeading = situation['target-ship']['heading']

            # Bearing
            self.targetship.hasRelativeBearingWithRespectToOwnShip = situation['own-ship']['bearing-of-other-vessel']

            # Add catgeory, if present
            if 'category' in situation['target-ship']:
                self.targetship.is_a.append(self.ontology[situation['target-ship']['category']])

            # Instantiate situation
            self.situation = self.ontology.Situation(situation['name'],
                                                    namespace=self.ontology)

            # Assign own ship to situation
            self.situation.hasOwnShip = self.ownship

            # Assign target ship to situation
            self.situation.hasTargetShip = self.targetship
        except Exception as e:
            print(f"[-] Failed to instantiate the situation with following error: {e}")
            sys.exit()
        else:
            if self.is_verbose:
                print("[+] Situation instantiated")

    def __classify(self, situation):
        result = situation

        # Remove garbage from owlready2
        prev_stdout = sys.stdout
        prev_stderr = sys.stderr
        f = open('/dev/null', 'w')
        sys.stdout = f
        sys.stderr = f

        # Classification
        with self.ontology:
            owl.sync_reasoner_pellet(infer_property_values=True,
                                     infer_data_property_values=True,
                                     debug=1)

            # Mandatory inferences
            result['situation-category'] = str(self.situation.is_a[0]).split('.')[-1]

            result['own-ship']['behaviour'] = self.ownship.hasBehaviour

            result['target-ship']['behaviour'] = self.targetship.hasBehaviour

            # Optional inferences
            if 'category' not in result['own-ship']:
                result['own-ship']['category'] = str(self.ownship.is_a[0]).split('.')[-1]

            if 'category' not in result['target-ship']:
                result['target-ship']['category'] = str(self.targetship.is_a[0]).split('.')[-1]

        # Make stuff normal again
        sys.stdout = prev_stdout
        sys.stderr = prev_stderr
        f.close()
        
        return result

