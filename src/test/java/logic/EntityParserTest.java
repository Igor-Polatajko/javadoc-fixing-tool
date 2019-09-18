package logic;

import entity.DescribedEntity;
import entity.MethodDescription;
import logic.EntityParser;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class EntityParserTest {
    @Test
    public void getDescribedEntity_successFlow_method() {
        String testValue = "public class App {\n" +
                "\n" +
                "\n" +
                "    /**\n" +
                "     * a and b\n" +
                "     * Map  (String - key,  String - value)\n" +
                "     *     {@code Map }\n" +
                "     *         Collection of generics type String\n" +
                "     */\n" +
                "    void m() throws Exception {\n" +
                "\n" +
                "\n" +
                "    }\n" +
                "\n" +
                "}\n" +
                "\n";

        String expectedData = "*/\n" +
                "    void m() throws Exception ";

        DescribedEntity resultDescribedEntity = EntityParser.getDescribedEntity(167, testValue);

        assertTrue(resultDescribedEntity.isPresent());
        assertEquals(DescribedEntity.Type.METHOD, resultDescribedEntity.getType());
        assertEquals(expectedData, resultDescribedEntity.getData());
    }

    @Test
    public void getDescribedEntity_successFlow_field() {
        String testValue = "public class App {\n" +
                "\n" +
                "\n" +
                "    /**\n" +
                "    some field documentation * \n" +
                "    */\n" +
                "    String field; \n" +
                "    \n" +
                "    void m() throws Exception {\n" +
                "\n" +
                "\n" +
                "    }\n" +
                "\n" +
                "}\n" +
                "\n";

        String expectedData = "*/\n" +
                "    String field";

        DescribedEntity resultDescribedEntity = EntityParser.getDescribedEntity(65, testValue);

        assertTrue(resultDescribedEntity.isPresent());
        assertEquals(DescribedEntity.Type.FIELD, resultDescribedEntity.getType());
        assertEquals(expectedData, resultDescribedEntity.getData());
    }

    @Test
    public void getDescribedEntity_successFlow_class() {
        String testValue = "/**\n" +
                " * Javadoc\n" +
                " */\n" +
                "public class App {\n" +
                "\n" +
                "    void m() throws Exception {\n" +
                "\n" +
                "\n" +
                "    }\n" +
                "}\n" +
                "\n";
        String expectedData = "*/\n" +
                "public class App ";

        DescribedEntity resultDescribedEntity = EntityParser.getDescribedEntity(16, testValue);

        assertTrue(resultDescribedEntity.isPresent());
        assertEquals(DescribedEntity.Type.CLASS, resultDescribedEntity.getType());
        assertEquals(expectedData, resultDescribedEntity.getData());
    }

    @Test
    public void getDescribedEntity_successFlow_interface() {
        String testValue = "/**\n" +
                " * Javadoc\n" +
                " */\n" +
                "public interface App {\n" +
                "\n" +
                "    void m() throws Exception ;\n" +
                "}\n" +
                "\n";

        String expectedData = "*/\n" +
                "public interface App ";

        DescribedEntity resultDescribedEntity = EntityParser.getDescribedEntity(16, testValue);

        assertTrue(resultDescribedEntity.isPresent());
        assertEquals(DescribedEntity.Type.INTERFACE, resultDescribedEntity.getType());
        assertEquals(expectedData, resultDescribedEntity.getData());
    }

    @Test
    public void getMethodDescription_successFlow() {
        DescribedEntity testDescribedEntity = new DescribedEntity();
        testDescribedEntity.setType(DescribedEntity.Type.METHOD);
        testDescribedEntity.setPresent(true);
        testDescribedEntity.setData("public Set<String> method(List<String> strings, Integer integer)" +
                " throws Exception, SQLException, FileNotFoundException");
        List<String> expectedParams = Arrays.asList("List<String> strings", "Integer integer");
        List<String> expectedExceptionsThrown = Arrays.asList("Exception", "SQLException", "FileNotFoundException");

        MethodDescription resultMethodDescription = EntityParser.getMethodDescription(testDescribedEntity);

        assertTrue(resultMethodDescription.isPresent());
        assertEquals("Set<String>", resultMethodDescription.getReturnType());
        assertListEquals(expectedParams, resultMethodDescription.getParams());
        assertListEquals(expectedExceptionsThrown, resultMethodDescription.getExceptionsThrown());
    }

    @Test
    public void getMethodDescription_successFlow_void() {
        DescribedEntity testDescribedEntity = new DescribedEntity();
        testDescribedEntity.setType(DescribedEntity.Type.METHOD);
        testDescribedEntity.setPresent(true);
        testDescribedEntity.setData("void method(Integer integer)" +
                " throws Exception");
        List<String> expectedParams = Collections.singletonList("Integer integer");
        List<String> expectedExceptionsThrown = Collections.singletonList("Exception");

        MethodDescription resultMethodDescription = EntityParser.getMethodDescription(testDescribedEntity);

        assertTrue(resultMethodDescription.isPresent());
        assertEquals("void", resultMethodDescription.getReturnType());
        assertListEquals(expectedParams, resultMethodDescription.getParams());
        assertListEquals(expectedExceptionsThrown, resultMethodDescription.getExceptionsThrown());
    }

    private <T> void assertListEquals(List<T> expected, List<T> actual) {
        if (expected == null && actual == null) {
            return;
        }

        if (expected == null || actual == null || expected.size() != actual.size()) {
            fail("List are not equal");
        }

        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i), actual.get(i));
        }
    }
}