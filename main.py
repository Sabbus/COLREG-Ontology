import owlready2 as owl
import os

SCRIPT_DIR = os.path.dirname(__file__)

onto = owl.get_ontology(SCRIPT_DIR + '/colreg_ontology_DOLCE_rich.owl')
onto.load()

owl.sync_reasoner_pellet([onto], infer_property_values=True, infer_data_property_values=True)
print(onto.get_parents_of(onto.ownship1))
