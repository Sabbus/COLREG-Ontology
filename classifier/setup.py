from setuptools import setup

setup(
    entry_points = {
        'console_scripts': [
                'classifier=classifier.cli:main'
        ]
    },
    name="classifier",
    version="0.1.0",
    description="COLREG classifier",
    author="Nicola Sabatino",
    author_email="nicola.sabatino@edu.unige.it",
    package_dir={'classifier': './src'},
    packages=["classifier"],
    install_requires=[
        "owlready2",
        "jsonschema"
    ],
    include_package_data=True
)
