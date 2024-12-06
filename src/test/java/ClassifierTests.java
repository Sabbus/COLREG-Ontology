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

    static final String pathToHeadOnScenario = "./src/test/resources/scenarios/head_on_scenario.json";
    static final String pathToCrossingScenarioOwnshipStandOn = "./src/test/resources/scenarios/crossing_scenario_ownship_standon.json";
    static final String pathToOvertakingScenarioOwnshipStandOn = "./src/test/resources/scenarios/overtaking_scenario_ownship_standon.json";
    static final String pathToCrossingScenarioOwnshipGiveWay = "./src/test/resources/scenarios/crossing_scenario_ownship_giveway.json";
    static final String pathToOvertakingScenarioOwnshipGiveWay = "./src/test/resources/scenarios/overtaking_scenario_ownship_giveway.json";
    static final String pathToSailingVesselsDifferentSideScenarioOwnshipStandOn = "./src/test/resources/scenarios/sailing_vessels_different_side_ownship_standon.json";
    static final String pathToSailingVesselsDifferentSideScenarioOwnshipGiveWay = "./src/test/resources/scenarios/sailing_vessels_different_side_ownship_giveway.json";
    static final String pathToSailingVesselsSameSideScenarioOwnshipStandOn = "./src/test/resources/scenarios/sailing_vessels_same_side_ownship_standon.json";
    static final String pathToDifferentVesselsPowerVsSailing = "./src/test/resources/scenarios/different_vessel_scenario_power_sailing.json";
    static final String pathToDifferentVesselsPowerVsFishing = "./src/test/resources/scenarios/different_vessel_scenario_power_fishing.json";
    static final String pathToDifferentVesselsPowerVsManoeuvre = "./src/test/resources/scenarios/different_vessel_scenario_power_manoeuvre.json";
    static final String pathToDifferentVesselsPowerVsDraught = "./src/test/resources/scenarios/different_vessel_scenario_power_draught.json";
    static final String pathToDifferentVesselsPowerVsCommand = "./src/test/resources/scenarios/different_vessel_scenario_power_command.json";
    static final String pathToDifferentVesselsSailingVsFishing = "./src/test/resources/scenarios/different_vessel_scenario_sailing_fishing.json";
    static final String pathToDifferentVesselsSailingVsManoeuvre = "./src/test/resources/scenarios/different_vessel_scenario_sailing_manoeuvre.json";
    static final String pathToDifferentVesselsSailingVsDraught = "./src/test/resources/scenarios/different_vessel_scenario_sailing_draught.json";
    static final String pathToDifferentVesselsSailingVsCommand = "./src/test/resources/scenarios/different_vessel_scenario_sailing_command.json";
    static final String pathToDifferentVesselsFishingVsManoeuvre = "./src/test/resources/scenarios/different_vessel_scenario_fishing_manoeuvre.json";
    static final String pathToDifferentVesselsFishingVsDraught = "./src/test/resources/scenarios/different_vessel_scenario_fishing_draught.json";
    static final String pathToDifferentVesselsFishingVsCommand = "./src/test/resources/scenarios/different_vessel_scenario_fishing_command.json";
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

    // @Test
    // public void testHeadOnSituation() {
    //     JsonObject headOnScenario = getScenarioJson(pathToHeadOnScenario);
    //
    //     JsonObject result = classifier.classify(headOnScenario);
    //
    //     JsonObject classifierResult = (JsonObject) result.get("classification");
    //     String scenarioCategory = (String) classifierResult.get("category");
    //     String ownshipBehavior = (String) classifierResult.get("ownship-behavior");
    //     String targetBehavior = (String) classifierResult.get("target-behavior");
    //
    //     assertEquals("HeadOn", scenarioCategory);
    //     assertEquals("alter_course", ownshipBehavior);
    //     assertEquals("alter_course", targetBehavior);
    // }

    @Test
    public void testCrossingOwnshipStandOnSituation() {
        JsonObject CrossingScenario = getScenarioJson(pathToCrossingScenarioOwnshipStandOn);

        JsonObject result = classifier.classify(CrossingScenario);

        JsonObject classifierResult = (JsonObject) result.get("classification");
        String scenarioCategory = (String) classifierResult.get("category");
        String ownshipBehavior = (String) classifierResult.get("ownship-behavior");
        String targetBehavior = (String) classifierResult.get("target-behavior");

        assertEquals("Crossing", scenarioCategory);
        assertEquals("keep_course", ownshipBehavior);
        assertEquals("alter_course", targetBehavior);
    }

    // @Test
    // public void testCrossingOwnshipGiveWaySituation() {
    //     JsonObject CrossingScenario = getScenarioJson(pathToCrossingScenarioOwnshipGiveWay);
    // 
    //     JsonObject result = classifier.classify(CrossingScenario);
    // 
    //     JsonObject classifierResult = (JsonObject) result.get("classification");
    //     String scenarioCategory = (String) classifierResult.get("category");
    //     String ownshipBehavior = (String) classifierResult.get("ownship-behavior");
    //     String targetBehavior = (String) classifierResult.get("target-behavior");
    // 
    //     assertEquals("Crossing", scenarioCategory);
    //     assertEquals("alter_course", ownshipBehavior);
    //     assertEquals("keep_course", targetBehavior);
    // }
    //
    // @Test
    // public void testOvertakingOwnshipStandOnSituation() {
    //     JsonObject OvertakingScenario = getScenarioJson(pathToOvertakingScenarioOwnshipStandOn);
    // 
    //     JsonObject result = classifier.classify(OvertakingScenario);
    // 
    //     JsonObject classifierResult = (JsonObject) result.get("classification");
    //     String scenarioCategory = (String) classifierResult.get("category");
    //     String ownshipBehavior = (String) classifierResult.get("ownship-behavior");
    //     String targetBehavior = (String) classifierResult.get("target-behavior");
    // 
    //     assertEquals("Overtaking", scenarioCategory);
    //     assertEquals("keep_course", ownshipBehavior);
    //     assertEquals("alter_course", targetBehavior);
    // }
    //
    // @Test
    // public void testOvertakingOwnshipGiveWaySituation() {
    //     JsonObject OvertakingScenario = getScenarioJson(pathToOvertakingScenarioOwnshipGiveWay);
    //
    //     JsonObject result = classifier.classify(OvertakingScenario);
    //
    //     JsonObject classifierResult = (JsonObject) result.get("classification");
    //     String scenarioCategory = (String) classifierResult.get("category");
    //     String ownshipBehavior = (String) classifierResult.get("ownship-behavior");
    //     String targetBehavior = (String) classifierResult.get("target-behavior");
    //
    //     assertEquals("Overtaking", scenarioCategory);
    //     assertEquals("alter_course", ownshipBehavior);
    //     assertEquals("keep_course", targetBehavior);
    // }
    //
    // @Test
    // public void testSailingVesselsDifferentSideSituationOwnshipStandOn() {
    //     JsonObject SailingVesselsDifferentSideScenarioOwnshipStandOn = getScenarioJson(pathToSailingVesselsDifferentSideScenarioOwnshipStandOn);
    //
    //     JsonObject result = classifier.classify(SailingVesselsDifferentSideScenarioOwnshipStandOn);
    //
    //     JsonObject classifierResult = (JsonObject) result.get("classification");
    //     String scenarioCategory = (String) classifierResult.get("category");
    //     String ownshipBehavior = (String) classifierResult.get("ownship-behavior");
    //     String targetBehavior = (String) classifierResult.get("target-behavior");
    //
    //     assertEquals("SailingVesselEncounter", scenarioCategory);
    //     assertEquals("keep_course", ownshipBehavior);
    //     assertEquals("alter_course", targetBehavior);
    // }
    //
    // @Test
    // public void testSailingVesselsDifferentSideSituationOwnshipGiveWay() {
    //     JsonObject SailingVesselsDifferentSideScenarioOwnshipGiveWay = getScenarioJson(pathToSailingVesselsDifferentSideScenarioOwnshipGiveWay);
    //
    //     JsonObject result = classifier.classify(SailingVesselsDifferentSideScenarioOwnshipGiveWay);
    //
    //     JsonObject classifierResult = (JsonObject) result.get("classification");
    //     String scenarioCategory = (String) classifierResult.get("category");
    //     String ownshipBehavior = (String) classifierResult.get("ownship-behavior");
    //     String targetBehavior = (String) classifierResult.get("target-behavior");
    //
    //     assertEquals("SailingVesselEncounter", scenarioCategory);
    //     assertEquals("alter_course", ownshipBehavior);
    //     assertEquals("keep_course", targetBehavior);
    // }
    //
    // @Test
    // public void testSailingVesselsSameSideSituationOwnshipStandOn() {
    //     JsonObject SailingVesselsSameSideScenarioOwnshipStandOn = getScenarioJson(pathToSailingVesselsSameSideScenarioOwnshipStandOn);
    //
    //     JsonObject result = classifier.classify(SailingVesselsSameSideScenarioOwnshipStandOn);
    //
    //     JsonObject classifierResult = (JsonObject) result.get("classification");
    //     String scenarioCategory = (String) classifierResult.get("category");
    //     String ownshipBehavior = (String) classifierResult.get("ownship-behavior");
    //     String targetBehavior = (String) classifierResult.get("target-behavior");
    //
    //     assertEquals("SailingVesselEncounter", scenarioCategory);
    //     assertEquals("keep_course", ownshipBehavior);
    //     assertEquals("alter_course", targetBehavior);
    // }
    //
    // @Test
    // public void testDifferentVesselsPowerVsSailing() {
    //     JsonObject DifferentVesselsPowerVsSailing = getScenarioJson(pathToDifferentVesselsPowerVsSailing);
    //
    //     JsonObject result = classifier.classify(DifferentVesselsPowerVsSailing);
    //
    //     JsonObject classifierResult = (JsonObject) result.get("classification");
    //     String scenarioCategory = (String) classifierResult.get("category");
    //     String ownshipBehavior = (String) classifierResult.get("ownship-behavior");
    //     String targetBehavior = (String) classifierResult.get("target-behavior");
    //
    //     assertEquals("DifferentVesselEncounter", scenarioCategory);
    //     assertEquals("keep_course", ownshipBehavior);
    //     assertEquals("alter_course", targetBehavior);
    // }
    //
    // @Test
    // public void testDifferentVesselsPowerVsFishing() {
    //     JsonObject DifferentVesselsPowerVsFishing = getScenarioJson(pathToDifferentVesselsPowerVsFishing);
    //
    //     JsonObject result = classifier.classify(DifferentVesselsPowerVsFishing);
    //
    //     JsonObject classifierResult = (JsonObject) result.get("classification");
    //     String scenarioCategory = (String) classifierResult.get("category");
    //     String ownshipBehavior = (String) classifierResult.get("ownship-behavior");
    //     String targetBehavior = (String) classifierResult.get("target-behavior");
    //
    //     assertEquals("DifferentVesselEncounter", scenarioCategory);
    //     assertEquals("keep_course", ownshipBehavior);
    //     assertEquals("alter_course", targetBehavior);
    // }
    //
    // @Test
    // public void testDifferentVesselsPowerVsManoeuvre() {
    //     JsonObject DifferentVesselsPowerVsManoeuvre = getScenarioJson(pathToDifferentVesselsPowerVsManoeuvre);
    //
    //     JsonObject result = classifier.classify(DifferentVesselsPowerVsManoeuvre);
    //
    //     JsonObject classifierResult = (JsonObject) result.get("classification");
    //     String scenarioCategory = (String) classifierResult.get("category");
    //     String ownshipBehavior = (String) classifierResult.get("ownship-behavior");
    //     String targetBehavior = (String) classifierResult.get("target-behavior");
    //
    //     assertEquals("DifferentVesselEncounter", scenarioCategory);
    //     assertEquals("keep_course", ownshipBehavior);
    //     assertEquals("alter_course", targetBehavior);
    // }
    //
    // @Test
    // public void testDifferentVesselsPowerVsDraught() {
    //     JsonObject DifferentVesselsPowerVsDraught = getScenarioJson(pathToDifferentVesselsPowerVsDraught);
    //
    //     JsonObject result = classifier.classify(DifferentVesselsPowerVsDraught);
    //
    //     JsonObject classifierResult = (JsonObject) result.get("classification");
    //     String scenarioCategory = (String) classifierResult.get("category");
    //     String ownshipBehavior = (String) classifierResult.get("ownship-behavior");
    //     String targetBehavior = (String) classifierResult.get("target-behavior");
    //
    //     assertEquals("DifferentVesselEncounter", scenarioCategory);
    //     assertEquals("keep_course", ownshipBehavior);
    //     assertEquals("alter_course", targetBehavior);
    // }
    //
    // @Test
    // public void testDifferentVesselsPowerVsCommand() {
    //     JsonObject DifferentVesselsPowerVsCommand = getScenarioJson(pathToDifferentVesselsPowerVsCommand);
    //
    //     JsonObject result = classifier.classify(DifferentVesselsPowerVsCommand);
    //
    //     JsonObject classifierResult = (JsonObject) result.get("classification");
    //     String scenarioCategory = (String) classifierResult.get("category");
    //     String ownshipBehavior = (String) classifierResult.get("ownship-behavior");
    //     String targetBehavior = (String) classifierResult.get("target-behavior");
    //
    //     assertEquals("DifferentVesselEncounter", scenarioCategory);
    //     assertEquals("keep_course", ownshipBehavior);
    //     assertEquals("alter_course", targetBehavior);
    // }
    //
    // @Test
    // public void testDifferentVesselsSailingVsFishing() {
    //     JsonObject DifferentVesselsSailingVsFishing = getScenarioJson(pathToDifferentVesselsSailingVsFishing);
    //
    //     JsonObject result = classifier.classify(DifferentVesselsSailingVsFishing);
    //
    //     JsonObject classifierResult = (JsonObject) result.get("classification");
    //     String scenarioCategory = (String) classifierResult.get("category");
    //     String ownshipBehavior = (String) classifierResult.get("ownship-behavior");
    //     String targetBehavior = (String) classifierResult.get("target-behavior");
    //
    //     assertEquals("DifferentVesselEncounter", scenarioCategory);
    //     assertEquals("keep_course", ownshipBehavior);
    //     assertEquals("alter_course", targetBehavior);
    // }
    //
    // @Test
    // public void testDifferentVesselsSailingVsManoeuvre() {
    //     JsonObject DifferentVesselsSailingVsManoeuvre = getScenarioJson(pathToDifferentVesselsSailingVsManoeuvre);
    //
    //     JsonObject result = classifier.classify(DifferentVesselsSailingVsManoeuvre);
    //
    //     JsonObject classifierResult = (JsonObject) result.get("classification");
    //     String scenarioCategory = (String) classifierResult.get("category");
    //     String ownshipBehavior = (String) classifierResult.get("ownship-behavior");
    //     String targetBehavior = (String) classifierResult.get("target-behavior");
    //
    //     assertEquals("DifferentVesselEncounter", scenarioCategory);
    //     assertEquals("keep_course", ownshipBehavior);
    //     assertEquals("alter_course", targetBehavior);
    // }
    //
    // @Test
    // public void testDifferentVesselsSailingVsDraught() {
    //     JsonObject DifferentVesselsSailingVsDraught = getScenarioJson(pathToDifferentVesselsSailingVsDraught);
    //
    //     JsonObject result = classifier.classify(DifferentVesselsSailingVsDraught);
    //
    //     JsonObject classifierResult = (JsonObject) result.get("classification");
    //     String scenarioCategory = (String) classifierResult.get("category");
    //     String ownshipBehavior = (String) classifierResult.get("ownship-behavior");
    //     String targetBehavior = (String) classifierResult.get("target-behavior");
    //
    //     assertEquals("DifferentVesselEncounter", scenarioCategory);
    //     assertEquals("keep_course", ownshipBehavior);
    //     assertEquals("alter_course", targetBehavior);
    // }
    //
    // @Test
    // public void testDifferentVesselsSailingVsCommand() {
    //     JsonObject DifferentVesselsSailingVsCommand = getScenarioJson(pathToDifferentVesselsSailingVsCommand);
    //
    //     JsonObject result = classifier.classify(DifferentVesselsSailingVsCommand);
    //
    //     JsonObject classifierResult = (JsonObject) result.get("classification");
    //     String scenarioCategory = (String) classifierResult.get("category");
    //     String ownshipBehavior = (String) classifierResult.get("ownship-behavior");
    //     String targetBehavior = (String) classifierResult.get("target-behavior");
    //
    //     assertEquals("DifferentVesselEncounter", scenarioCategory);
    //     assertEquals("keep_course", ownshipBehavior);
    //     assertEquals("alter_course", targetBehavior);
    // }
    //
    // @Test
    // public void testDifferentVesselsFishingVsManoeuvre() {
    //     JsonObject DifferentVesselsFishingVsManoeuvre = getScenarioJson(pathToDifferentVesselsFishingVsManoeuvre);
    //
    //     JsonObject result = classifier.classify(DifferentVesselsFishingVsManoeuvre);
    //
    //     JsonObject classifierResult = (JsonObject) result.get("classification");
    //     String scenarioCategory = (String) classifierResult.get("category");
    //     String ownshipBehavior = (String) classifierResult.get("ownship-behavior");
    //     String targetBehavior = (String) classifierResult.get("target-behavior");
    //
    //     assertEquals("DifferentVesselEncounter", scenarioCategory);
    //     assertEquals("keep_course", ownshipBehavior);
    //     assertEquals("alter_course", targetBehavior);
    // }
    //
    // @Test
    // public void testDifferentVesselsFishingVsDraught() {
    //     JsonObject DifferentVesselsFishingVsDraught = getScenarioJson(pathToDifferentVesselsFishingVsDraught);
    //
    //     JsonObject result = classifier.classify(DifferentVesselsFishingVsDraught);
    //
    //     JsonObject classifierResult = (JsonObject) result.get("classification");
    //     String scenarioCategory = (String) classifierResult.get("category");
    //     String ownshipBehavior = (String) classifierResult.get("ownship-behavior");
    //     String targetBehavior = (String) classifierResult.get("target-behavior");
    //
    //     assertEquals("DifferentVesselEncounter", scenarioCategory);
    //     assertEquals("keep_course", ownshipBehavior);
    //     assertEquals("alter_course", targetBehavior);
    // }
    //
    // @Test
    // public void testDifferentVesselsFishingVsCommand() {
    //     JsonObject DifferentVesselsFishingVsCommand = getScenarioJson(pathToDifferentVesselsFishingVsCommand);
    //
    //     JsonObject result = classifier.classify(DifferentVesselsFishingVsCommand);
    //
    //     JsonObject classifierResult = (JsonObject) result.get("classification");
    //     String scenarioCategory = (String) classifierResult.get("category");
    //     String ownshipBehavior = (String) classifierResult.get("ownship-behavior");
    //     String targetBehavior = (String) classifierResult.get("target-behavior");
    //
    //     assertEquals("DifferentVesselEncounter", scenarioCategory);
    //     assertEquals("keep_course", ownshipBehavior);
    //     assertEquals("alter_course", targetBehavior);
    // }
}
