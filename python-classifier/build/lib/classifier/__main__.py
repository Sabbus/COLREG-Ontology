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
parser.add_argument('-v', 
                    '--verbose', 
                    help='increase verbosity', 
                    action='store_true')

if __name__ == '__main__':
    args = parser.parse_args()

    classifier = Classifier(is_verbose=args.verbose)
    classifier.classify(args.scenario)

