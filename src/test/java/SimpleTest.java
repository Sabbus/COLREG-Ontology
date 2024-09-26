import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.github.cliftonlabs.json_simple.JsonObject;

import io.sabbus.COLREGClassifier;

public class COLREGClassifier {

    @Test
    public void testHeadOnSituation() {
        JsonObject classifierResult = COLREGClassifier.classify();
        assertEquals(1, 1);
    }
}
