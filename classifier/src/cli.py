import argparse
import pprint
import json
from .classifier import Classifier

def main():
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
                        type=str)
    parser.add_argument('-v', 
                        '--verbose', 
                        help='increase verbosity', 
                        action='store_true')
    
    args = parser.parse_args()

    classifier = Classifier(is_verbose=args.verbose)
    result = classifier.classify(args.scenario)

    if args.output:
        with open(args.output, 'w') as f:
            f.write(json.dumps(result))
    else:
        pretty_print(result)

def pretty_print(result):
    pprint.pprint(result)

