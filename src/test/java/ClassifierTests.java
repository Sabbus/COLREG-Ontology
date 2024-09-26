import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.FileReader;

import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.Jsoner;
import com.github.cliftonlabs.json_simple.JsonException;
import com.github.cliftonlabs.json_simple.JsonObject;

import io.sabbus.COLREGClassifier;

public class ClassifierTests {

    static final String pathToOntology = "./src/test/resources/owl/colreg-ontology.owl";

    static final String pathToHeadOnScenario = "./src/test/resources/scenarios/head-on-scenario.json";

    static COLREGClassifier classifier;

    @BeforeClass
    public static void createCOLREGClassifierInstance() {
        classifier = new COLREGClassifier(pathToOntology);
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
        JsonArray scenarioCategory = (JsonArray) classifierResult.get("category");

        assertEquals("[:HeadOn]", scenarioCategory.toString());
    }
}
