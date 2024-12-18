# This script runs the COLREG classifier from the command line

# java -jar target/colreg-classifier-0.2.0-jar-with-dependencies.jar $@
java -agentlib:jdwp=transport=dt_socket,address=8080,server=y,suspend=y -jar target/colreg-classifier-0.2.0-jar-with-dependencies.jar -f src/test/resources/scenarios/crossing_scenario_ownship_giveway.json -o a.json
