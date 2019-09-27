package logic;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class TestUtils {

    public static <T> void assertListEquals(List<T> expected, List<T> actual) {
        if (expected == null && actual == null) {
            return;
        }

        if (expected == null || actual == null || expected.size() != actual.size()) {
            fail("List are not equal: \nExpected: " + expected + "\nActual: " + actual);
        }

        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i), actual.get(i));
        }
    }
}
