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

    static final String pathToHeadOnScenario = "./src/test/resources/scenarios/head-on-scenario.json";
    static final String pathToCrossingScenario = "./src/test/resources/scenarios/crossing-scenario.json";
    static final String pathToOvertakingScenario = "./src/test/resources/scenarios/overtaking-scenario.json";
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

    @Test
    public void testHeadOnSituation() {
        JsonObject headOnScenario = getScenarioJson(pathToHeadOnScenario);

        JsonObject result = classifier.classify(headOnScenario);

        JsonObject classifierResult = (JsonObject) result.get("classification");
        String scenarioCategory = (String) classifierResult.get("category");
        String ownshipBehavior = (String) classifierResult.get("ownship-behavior");
        String targetBehavior = (String) classifierResult.get("target-behavior");

        assertEquals("HeadOn", scenarioCategory);
        assertEquals("alter_course", ownshipBehavior);
        assertEquals("alter_course", targetBehavior);
    }

    @Test
    public void testCrossingSituation() {
        JsonObject CrossingScenario = getScenarioJson(pathToCrossingScenario);

        JsonObject result = classifier.classify(CrossingScenario);
        System.out.println(result);

        JsonObject classifierResult = (JsonObject) result.get("classification");
        String scenarioCategory = (String) classifierResult.get("category");
        String ownshipBehavior = (String) classifierResult.get("ownship-behavior");
        String targetBehavior = (String) classifierResult.get("target-behavior");

        assertEquals("Crossing", scenarioCategory);
        assertEquals("keep_course", ownshipBehavior);
        assertEquals("alter_course", targetBehavior);
    }

    @Test
    public void testOvertakingSituation() {
        JsonObject OvertakingScenario = getScenarioJson(pathToOvertakingScenario);

        JsonObject result = classifier.classify(OvertakingScenario);

        JsonObject classifierResult = (JsonObject) result.get("classification");
        String scenarioCategory = (String) classifierResult.get("category");
        String ownshipBehavior = (String) classifierResult.get("ownship-behavior");
        String targetBehavior = (String) classifierResult.get("target-behavior");

        assertEquals("Overtaking", scenarioCategory);
        assertEquals("keep_course", ownshipBehavior);
        assertEquals("alter_course", targetBehavior);
    }
}
