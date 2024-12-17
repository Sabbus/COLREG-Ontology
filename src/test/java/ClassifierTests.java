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

    private void testScenario(String pathToScenario, boolean printInferences, String sparqlQuery) {
        JsonObject scenario = getScenarioJson(pathToScenario);
    
        JsonObject result = classifier.classify(scenario, printInferences, sparqlQuery);
    
        JsonObject classifierResult = (JsonObject) result.get("classification");
        String scenarioCategory = (String) classifierResult.get("category");
        String ownshipBehavior = (String) classifierResult.get("ownship-behavior");
        String targetBehavior = (String) classifierResult.get("target-behavior");

        String expectedScenarioCategory = (String) scenario.get("category");
        String expectedOwnShipBehavior = (String) scenario.get("own-ship-behavior");
        String expectedTargetShipBehavior = (String) scenario.get("target-ship-behavior");
    
        assertEquals(expectedScenarioCategory, scenarioCategory);
        assertEquals(expectedOwnShipBehavior, ownshipBehavior);
        assertEquals(expectedTargetShipBehavior, targetBehavior);
    }

    private void testScenario(String pathToScenario, boolean printInferences) {
        this.testScenario(pathToScenario, printInferences, "");
    }

    private void testScenario(String pathToScenario, String sparqlQuery) {
        this.testScenario(pathToScenario, false, sparqlQuery);
    }

    private void testScenario(String pathToScenario) {
        this.testScenario(pathToScenario, false, "");
    }

//region Cornerstone scenarios
//------------------------------------------------------------------------------
    @Test
    public void testHeadOnSituation() {
        testScenario("./src/test/resources/scenarios/head_on_scenario.json");
    }

    @Test
    public void testCrossingOwnshipStandOnSituation() {
        // String query = "PREFIX a: <http://unige.it/nicola-sabatino/2024/7/8/colreg-ontology#>\n" +
        //                    "SELECT ?1 ?2 WHERE {\n" +
        //                    "PropertyValue(?1, a:hasWindOnSide, ?2) \n" +
        //                "}";
        testScenario("./src/test/resources/scenarios/crossing_scenario_ownship_standon.json");
    }
    
    @Test
    public void testCrossingOwnshipGiveWaySituation() {
        testScenario("./src/test/resources/scenarios/crossing_scenario_ownship_giveway.json");
    }

    @Test
    public void testOvertakingOwnshipStandOnSituation() {
        testScenario("./src/test/resources/scenarios/overtaking_scenario_ownship_standon.json");
    }
    
    @Test
    public void testOvertakingOwnshipGiveWaySituation() {
        testScenario("./src/test/resources/scenarios/overtaking_scenario_ownship_giveway.json");
    }
//------------------------------------------------------------------------------
//endregion

//region Sailing vessel encounter scenarios
//------------------------------------------------------------------------------
    @Test
    public void testSailingVesselsDifferentSideSituationOwnshipStandOn() {
        testScenario("./src/test/resources/scenarios/sailing_vessels_different_side_ownship_standon.json");
    }

    @Test
    public void testSailingVesselsDifferentSideSituationOwnshipGiveWay() {
        testScenario("./src/test/resources/scenarios/sailing_vessels_different_side_ownship_giveway.json");
    }

    @Test
    public void testSailingVesselsSameSideSituationOwnshipStandOn() {
        testScenario("./src/test/resources/scenarios/sailing_vessels_same_side_ownship_standon.json");
    }

    @Test
    public void testSailingVesselsSameSideSituationOwnshipGiveWay() {
        testScenario("./src/test/resources/scenarios/sailing_vessels_same_side_ownship_giveway.json");
    }
//------------------------------------------------------------------------------
//endregion
    
//region Different vessels encounter
//------------------------------------------------------------------------------
    @Test
    public void testDifferentVesselsPowerVsSailing() {
        testScenario("./src/test/resources/scenarios/different_vessel_scenario_power_sailing.json");
    }
    
    @Test
    public void testDifferentVesselsPowerVsFishing() {
        testScenario("./src/test/resources/scenarios/different_vessel_scenario_power_fishing.json");
    }
    
    @Test
    public void testDifferentVesselsPowerVsManoeuvre() {
        testScenario("./src/test/resources/scenarios/different_vessel_scenario_power_manoeuvre.json");
    }
    
    @Test
    public void testDifferentVesselsPowerVsDraught() {
        testScenario("./src/test/resources/scenarios/different_vessel_scenario_power_draught.json");
    }
    
    @Test
    public void testDifferentVesselsPowerVsCommand() {
        testScenario("./src/test/resources/scenarios/different_vessel_scenario_power_command.json");
    }
    
    @Test
    public void testDifferentVesselsSailingVsFishing() {
        testScenario("./src/test/resources/scenarios/different_vessel_scenario_sailing_fishing.json");
    }
    
    @Test
    public void testDifferentVesselsSailingVsManoeuvre() {
        testScenario("./src/test/resources/scenarios/different_vessel_scenario_sailing_manoeuvre.json");
    }
    
    @Test
    public void testDifferentVesselsSailingVsDraught() {
        testScenario("./src/test/resources/scenarios/different_vessel_scenario_sailing_draught.json");
    }
    
    @Test
    public void testDifferentVesselsSailingVsCommand() {
        testScenario("./src/test/resources/scenarios/different_vessel_scenario_sailing_command.json");
    }
    
    @Test
    public void testDifferentVesselsFishingVsManoeuvre() {
        testScenario("./src/test/resources/scenarios/different_vessel_scenario_fishing_manoeuvre.json");
    }
    
    @Test
    public void testDifferentVesselsFishingVsDraught() {
        testScenario("./src/test/resources/scenarios/different_vessel_scenario_fishing_draught.json");
    }
    
    @Test
    public void testDifferentVesselsFishingVsCommand() {
        testScenario("./src/test/resources/scenarios/different_vessel_scenario_fishing_command.json");
    }
//------------------------------------------------------------------------------
//endregion

//region Encounters with lights
//------------------------------------------------------------------------------
    @Test
    public void testHeadOnSituationWithLightsAndBearingBigShip() {
        testScenario("./src/test/resources/scenarios/head_on_scenario_with_lights_and_bearing_big_ship.json");
    }

    @Test
    public void testCrossingSituationOwnshipStandOnWithLightsAndBearingBigShip() {
        testScenario("./src/test/resources/scenarios/crossing_scenario_ownship_standon_with_lights_and_bearing_big_ship.json");
    }

    @Test
    public void testCrossingSituationOwnshipGiveWayWithLightsAndBearingBigShip() {
        testScenario("./src/test/resources/scenarios/crossing_scenario_ownship_giveway_with_lights_and_bearing_big_ship.json");
    }

    @Test
    public void testOvertakingSituationOwnshipStandOnWithLightsAndBearingBigShip() {
        testScenario("./src/test/resources/scenarios/overtaking_scenario_ownship_standon_with_lights_and_bearing_big_ship.json");
    }

    @Test
    public void testHeadOnSituationWithLightsAndBearingMediumShip() {
        testScenario("./src/test/resources/scenarios/head_on_scenario_with_lights_and_bearing_medium_ship.json");
    }

    @Test
    public void testCrossingSituationOwnshipStandOnWithLightsAndBearingMediumShip() {
        testScenario("./src/test/resources/scenarios/crossing_scenario_ownship_standon_with_lights_and_bearing_medium_ship.json");
    }

    @Test
    public void testCrossingSituationOwnshipGiveWayWithLightsAndBearingMediumShip() {
        testScenario("./src/test/resources/scenarios/crossing_scenario_ownship_giveway_with_lights_and_bearing_medium_ship.json");
    }

    @Test
    public void testOvertakingSituationOwnshipStandOnWithLightsAndBearingMediumShip() {
        testScenario("./src/test/resources/scenarios/overtaking_scenario_ownship_standon_with_lights_and_bearing_medium_ship.json");
    }

    @Test
    public void testHeadOnSituationWithLightsAndBearingSmallShip() {
        testScenario("./src/test/resources/scenarios/head_on_scenario_with_lights_and_bearing_small_ship.json");
    }

    @Test
    public void testCrossingSituationOwnshipStandOnWithLightsAndBearingSmallShip() {
        testScenario("./src/test/resources/scenarios/crossing_scenario_ownship_standon_with_lights_and_bearing_small_ship.json");
    }

    @Test
    public void testCrossingSituationOwnshipGiveWayWithLightsAndBearingSmallShip() {
        testScenario("./src/test/resources/scenarios/crossing_scenario_ownship_giveway_with_lights_and_bearing_small_ship.json");
    }

    @Test
    public void testOvertakingSituationOwnshipStandOnWithLightsAndBearingSmallShip() {
        testScenario("./src/test/resources/scenarios/overtaking_scenario_ownship_standon_with_lights_and_bearing_small_ship.json");
    }

    @Test
    public void testSailingVesselsDifferentSideSituationOwnshipStandOnWithLightsAndBearing() {
        testScenario("./src/test/resources/scenarios/sailing_vessels_different_side_ownship_standon_with_bearing_and_lights.json");
    }

    @Test
    public void testSailingVesselsDifferentSideSituationOwnshipGiveWayWithLightsAndBearing() {
        testScenario("./src/test/resources/scenarios/sailing_vessels_different_side_ownship_giveway_with_bearing_and_lights.json");
    }

    @Test
    public void testSailingVesselsSameSideSituationOwnshipStandOnWithLightsAndBearing() {
        testScenario("./src/test/resources/scenarios/sailing_vessels_same_side_ownship_standon_with_bearing_and_lights.json");
    }

    @Test
    public void testSailingVesselsSameSideSituationOwnshipGiveWayWithLightsAndBearing() {
        testScenario("./src/test/resources/scenarios/sailing_vessels_same_side_ownship_giveway_with_bearing_and_lights.json");
    }

    @Test
    public void testSailingVesselsDifferentSideSituationOwnshipStandOnWithLightsAndBearingWithAllRoundLights() {
        testScenario("./src/test/resources/scenarios/sailing_vessels_different_side_ownship_standon_with_bearing_and_lights_with_all_round_lights.json");
    }

    @Test
    public void testSailingVesselsDifferentSideSituationOwnshipGiveWayWithLightsAndBearingWithAllRoundLights() {
        testScenario("./src/test/resources/scenarios/sailing_vessels_different_side_ownship_giveway_with_bearing_and_lights_with_all_round_lights.json");
    }

    @Test
    public void testSailingVesselsSameSideSituationOwnshipStandOnWithLightsAndBearingWithAllRoundLights() {
        testScenario("./src/test/resources/scenarios/sailing_vessels_same_side_ownship_standon_with_bearing_and_lights_with_all_round_lights.json");
    }

    @Test
    public void testSailingVesselsSameSideSituationOwnshipGiveWayWithLightsAndBearingWithAllRoundLights() {
        testScenario("./src/test/resources/scenarios/sailing_vessels_same_side_ownship_giveway_with_bearing_and_lights_with_all_round_lights.json");
    }

    @Test
    public void testDifferentVesselsWithBearingAndLightsPowerVsFishingTrawlingBigShip() {
        testScenario("./src/test/resources/scenarios/different_vessel_scenario_power_fishing_trawling_with_bearing_and_lights_big_ship.json");
    }

    @Test
    public void testDifferentVesselsWithBearingAndLightsPowerVsFishingTrawlingSmallShip() {
        testScenario("./src/test/resources/scenarios/different_vessel_scenario_power_fishing_trawling_with_bearing_and_lights_small_ship.json");
    }

    @Test
    public void testDifferentVesselsWithBearingAndLightsPowerVsFishingNotTrawlingWithoutGear() {
        testScenario("./src/test/resources/scenarios/different_vessel_scenario_power_fishing_not_trawling_with_bearing_and_lights_without_gear.json");
    }

    @Test
    public void testDifferentVesselsWithBearingAndLightsPowerVsFishingNotTrawlingWithGear() {
        testScenario("./src/test/resources/scenarios/different_vessel_scenario_power_fishing_not_trawling_with_bearing_and_lights_with_gear.json");
    }
//------------------------------------------------------------------------------
//endregion
}
