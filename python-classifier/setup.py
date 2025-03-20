from setuptools import setup

setup(name="classifier",
      version="0.1.0",
      description="COLREG classifier",
      author="Nicola Sabatino",
      author_email="nicola.sabatino@edu.unige.it",
      package_dir={'classifier': './classifier'},
      packages=["classifier"],
      install_requires=["owlready2",
                        "jsonschema"],
      include_package_data=True)
