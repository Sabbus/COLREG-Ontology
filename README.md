# COLREG Ontology repository for FOIS 2025

This repository stores an ontology speficication for the Convention on the International Regulations for Preventing Collisions at Sea, 1972 (COLREGs).

## Repository structure

```txt
.
|-- docs
|   |-- docs.rst
|-- src
|   |-- colreg_ontology.owl
|-- test
|   |-- scenarios
|       |-- scenario_1.json
|       |-- ...
|   |-- classifier
|       |-- __main__.py
|       |-- __init__.py
|       |-- setup.py
|       |-- ...
|-- LICENSE
|-- README.md
```

## Classification tests

From the root folder of this project use the following commands to install the classifier in a python virtual environment:

```bash
python -m venv .venv
source .venv/bin/activate     # use `.venv\Scripts\activate.ps1` on windows
pip install test/classifier
```

Check if the classifier is correctly installed with:

```bash
python -m classifier --help
```

To perform the classification of a test scenario use the following command:

```bash
python -m classifier -s <path/to/scenario> -o results.xml
```

## Dependencies

To make the classifier work the following dependencies are required:
- Python >= 3.12
- owlready2
