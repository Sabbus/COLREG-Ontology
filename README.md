# COLREG Ontology repository for FOIS 2025

This repository stores an ontology speficication for the Convention on the International Regulations for Preventing Collisions at Sea, 1972 (COLREGs).

## Repository structure

```bash
.
|   |-- classifier
|       |-- src/
|           |-- ...
|       |-- test/
|           |-- test.py
|           |-- scenarios/
|               |-- ...
|       |-- ...
|-- colreg_ontology.owl
|-- LICENSE
|-- README.md
```

## Classification tests

Download this repository, then from the root folder run the following commands to install the classifier in a python virtual environment:

```bash
python -m venv .venv
source .venv/bin/activate     # use `.venv\Scripts\activate.ps1` on windows
pip install classifier/
```

To check if the classifier is correctly installed and to wiev usage options use the command below:

```bash
classifier --help
```

To perform the classification of a specific test scenario use the following command:

```bash
classifier <path/to/scenario>
```

If instead you want to test all the test scenarios, go to the classifier root folder and run the command below:

```bash
python -m unittest test/test.py
```

## Dependencies

The following dependencies are automatically installed in the virtual environment:
- Python >= 3.12
- owlready2 >= 0.47
- jsonchema >= 4.23.0
