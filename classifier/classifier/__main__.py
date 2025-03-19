import argparse
import os
from .classifier import Classifier

parser = argparse.ArgumentParser(prog='python -m classifier',
                                 description='COLREG classifier')

parser.add_argument('scenario', 
                    help='scenario file', 
                    metavar='<scenario>', 
                    type=str)
parser.add_argument('-o', 
                    '--output', 
                    help='output file', 
                    metavar='FILE', 
                    type=str, 
                    required=True)

if __name__ == '__main__':
    args = parser.parse_args()

    classifier = Classifier(os.path.dirname(__file__) + '/COLREG_ontology_no_exactly.owl')
    classifier.classify(args.scenario)

