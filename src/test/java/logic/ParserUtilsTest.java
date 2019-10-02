package logic;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static logic.TestUtils.assertListEquals;

public class ParserUtilsTest {

    @Test
    public void completeGenerics() {
        String[] testInput = {"Map<String,", "String>", "a,", "List<Map<Integer,", "Class<?>>>", "k"};
        List<String> expected =
                Arrays.asList("Map<String, String>", "a,", "List<Map<Integer, Class<?>>>", "k");

        List<String> actual = ParserUtils.completeGenerics(testInput);

        assertListEquals(expected, actual);
    }

    @Test
    public void completeGenerics_case2() {
        String[] testInput = {"List<Class<?>", "String>", "list"};
        List<String> expected = Arrays.asList("List<Class<?>, String>", "list");

        List<String> actual = ParserUtils.completeGenerics(testInput);

        assertListEquals(expected, actual);
    }
}