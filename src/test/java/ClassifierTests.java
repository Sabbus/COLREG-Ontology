import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.io.IOException;

import java.util.Properties;

import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.Jsoner;
import com.github.cliftonlabs.json_simple.JsonException;
import com.github.cliftonlabs.json_simple.JsonObject;

import io.sabbus.COLREGClassifier;

public class ClassifierTests {

    COLREGClassifier classifier;

    @Before
    public void createCOLREGClassifierInstance() {
        Properties properties = new Properties();

        try {
            InputStream configFile = new FileInputStream("./src/main/resources/config.properties");
            properties.load(configFile);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        
        final String pathToOntology = properties.getProperty("ontologyFilePath");
        final String ontologyIRI = properties.getProperty("ontologyIRI");

        classifier = new COLREGClassifier(pathToOntology, ontologyIRI);
    }

    private static JsonObject getScenarioJson(String pathToScenarioJsonFile) {
        JsonObject scenarioJson;

        try {
            FileReader fileReader = new FileReader(pathToScenarioJsonFile);

            scenarioJson = (JsonObject) Jsoner.deserialize(fileReader);
        }
        catch (FileNotFoundException | JsonException e) {
            throw new RuntimeException(e);
        }

        return scenarioJson;
    }

    private void testScenario(String pathToScenario, String expectedScenarioCategory, String expectedOwnShipBehavior, String expectedTargetShipBehavior) {
        JsonObject scenario = getScenarioJson(pathToScenario);
    
        JsonObject result = classifier.classify(scenario);
    
        JsonObject classifierResult = (JsonObject) result.get("classification");
        String scenarioCategory = (String) classifierResult.get("category");
        String ownshipBehavior = (String) classifierResult.get("ownship-behavior");
        String targetBehavior = (String) classifierResult.get("target-behavior");
    
        assertEquals(expectedScenarioCategory, scenarioCategory);
        assertEquals(expectedOwnShipBehavior, ownshipBehavior);
        assertEquals(expectedTargetShipBehavior, targetBehavior);
    }

//region Cornerstone scenarios
//------------------------------------------------------------------------------
    @Test
    public void testHeadOnSituation() {
        testScenario("./src/test/resources/scenarios/head_on_scenario.json", "HeadOn", "alter_course", "alter_course");
    }

    @Test
    public void testCrossingOwnshipStandOnSituation() {
        testScenario("./src/test/resources/scenarios/crossing_scenario_ownship_standon.json", "Crossing", "keep_course", "alter_course");
    }
    
    @Test
    public void testCrossingOwnshipGiveWaySituation() {
        testScenario("./src/test/resources/scenarios/crossing_scenario_ownship_giveway.json", "Crossing", "alter_course", "keep_course");
    }

    @Test
    public void testOvertakingOwnshipStandOnSituation() {
        testScenario("./src/test/resources/scenarios/overtaking_scenario_ownship_standon.json", "Overtaking", "keep_course", "alter_course");
    }
    
    @Test
    public void testOvertakingOwnshipGiveWaySituation() {
        testScenario("./src/test/resources/scenarios/overtaking_scenario_ownship_giveway.json", "Overtaking", "alter_course", "keep_course");
    }
//------------------------------------------------------------------------------
//endregion

//region Sailing vessel encounter scenarios
//------------------------------------------------------------------------------
    @Test
    public void testSailingVesselsDifferentSideSituationOwnshipStandOn() {
        testScenario("./src/test/resources/scenarios/sailing_vessels_different_side_ownship_standon.json", "SailingVesselEncounter", "keep_course", "alter_course");
    }

    @Test
    public void testSailingVesselsDifferentSideSituationOwnshipGiveWay() {
        testScenario("./src/test/resources/scenarios/sailing_vessels_different_side_ownship_giveway.json", "SailingVesselEncounter", "alter_course", "keep_course");
    }

    @Test
    public void testSailingVesselsSameSideSituationOwnshipStandOn() {
        testScenario("./src/test/resources/scenarios/sailing_vessels_same_side_ownship_standon.json", "SailingVesselEncounter", "keep_course", "alter_course");
    }
//------------------------------------------------------------------------------
//endregion
    
//region Different vessels encounter
//------------------------------------------------------------------------------
    @Test
    public void testDifferentVesselsPowerVsSailing() {
        testScenario("./src/test/resources/scenarios/different_vessel_scenario_power_sailing.json", "DifferentVesselEncounter", "keep_course", "alter_course");
    }
    
    @Test
    public void testDifferentVesselsPowerVsFishing() {
        testScenario("./src/test/resources/scenarios/different_vessel_scenario_power_fishing.json", "DifferentVesselEncounter", "keep_course", "alter_course");
    }
    
    @Test
    public void testDifferentVesselsPowerVsManoeuvre() {
        testScenario("./src/test/resources/scenarios/different_vessel_scenario_power_manoeuvre.json", "DifferentVesselEncounter", "keep_course", "alter_course");
    }
    
    @Test
    public void testDifferentVesselsPowerVsDraught() {
        testScenario("./src/test/resources/scenarios/different_vessel_scenario_power_draught.json", "DifferentVesselEncounter", "keep_course", "alter_course");
    }
    
    @Test
    public void testDifferentVesselsPowerVsCommand() {
        testScenario("./src/test/resources/scenarios/different_vessel_scenario_power_command.json", "DifferentVesselEncounter", "keep_course", "alter_course");
    }
    
    @Test
    public void testDifferentVesselsSailingVsFishing() {
        testScenario("./src/test/resources/scenarios/different_vessel_scenario_sailing_fishing.json", "DifferentVesselEncounter", "keep_course", "alter_course");
    }
    
    @Test
    public void testDifferentVesselsSailingVsManoeuvre() {
        testScenario("./src/test/resources/scenarios/different_vessel_scenario_sailing_manoeuvre.json", "DifferentVesselEncounter", "keep_course", "alter_course");
    }
    
    @Test
    public void testDifferentVesselsSailingVsDraught() {
        testScenario("./src/test/resources/scenarios/different_vessel_scenario_sailing_draught.json", "DifferentVesselEncounter", "keep_course", "alter_course");
    }
    
    @Test
    public void testDifferentVesselsSailingVsCommand() {
        testScenario("./src/test/resources/scenarios/different_vessel_scenario_sailing_command.json", "DifferentVesselEncounter", "keep_course", "alter_course");
    }
    
    @Test
    public void testDifferentVesselsFishingVsManoeuvre() {
        testScenario("./src/test/resources/scenarios/different_vessel_scenario_fishing_manoeuvre.json", "DifferentVesselEncounter", "keep_course", "alter_course");
    }
    
    @Test
    public void testDifferentVesselsFishingVsDraught() {
        testScenario("./src/test/resources/scenarios/different_vessel_scenario_fishing_draught.json", "DifferentVesselEncounter", "keep_course", "alter_course");
    }
    
    @Test
    public void testDifferentVesselsFishingVsCommand() {
        testScenario("./src/test/resources/scenarios/different_vessel_scenario_fishing_command.json", "DifferentVesselEncounter", "keep_course", "alter_course");
    }
//------------------------------------------------------------------------------
//endregion

//region Encounters with lights
//------------------------------------------------------------------------------
    @Test
    public void testHeadOnSituationWithLightsAndBearing() {
        testScenario("./src/test/resources/scenarios/head_on_scenario_with_lights_and_bearing.json", "HeadOn", "alter_course", "alter_course");
    }
//------------------------------------------------------------------------------
//endregion
}
