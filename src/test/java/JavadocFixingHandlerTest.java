import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class JavadocFixingHandlerTest {

    private JavadocFixingHandler javadocFixingHandler;

    @Before
    public void setUp() {
        javadocFixingHandler = new JavadocFixingHandler();
    }

    @Test
    public void fixAmpersands_successFlow() {
        String testValue = "/**\n" +
                "     * a & b\n" +
                "     * a&b\n" +
                "     * &gt;tag&lt;\n" +
                "*/";

        String expectedValue = "/**\n" +
                "     * a and b\n" +
                "     * a and b\n" +
                "     * &gt;tag&lt;\n" +
                "*/";

        String actualValue = javadocFixingHandler.fixAmpersands(testValue);

        assertEquals(expectedValue, actualValue);
    }

    @Test
    public void fixBadUseOfAngleBrackets_successFlow() {
        String testValue = "/**\n" +
                "     * a > b\n" +
                "     * a >= b\n" +
                "     * a < b\n" +
                "     * a <= b\n" +
                "     * a -> b\n" +
                "*/";

        String expectedValue = "/**\n" +
                "     * a greater than b\n" +
                "     * a equal or greater than b\n" +
                "     * a less than b\n" +
                "     * a equal or less than b\n" +
                "     * a --- b\n" +
                "*/";

        String actualValue = javadocFixingHandler.fixBadUseOfAngleBrackets(testValue);

        assertEquals(expectedValue, actualValue);
    }

    @Test
    public void fixIncompleteTags() {
        String testValue = "/**\n" +
                "     * &lt;complexContent>\n" +
                "     * &lt;complexContent{}>\n" +
                "     * &lt;complexContent\"\">\n" +
                "*/";

        String expectedValue = "/**\n" +
                "     * &lt;complexContent&gt;\n" +
                "     * &lt;complexContent{}&gt;\n" +
                "     * &lt;complexContent\"\"&gt;\n" +
                "*/";

        String actualValue = javadocFixingHandler.fixIncompleteTags(testValue);

        assertEquals(expectedValue, actualValue);
    }

    @Test
    public void fixGenerics_successFlow() {
        String testValue = "/**\n" +
                "     * @param map - Map<String, String> {@link Map<String, String>}\n" +
                "     * @param list - List<String>\n" +
                "     * {@link List<String>#containsAll(Collection<String>)}\n" +
                "     * {@link List<String>#containsAll(Collection<?>)}\n" +
                "     * {@code List<String>#containsAll(Collection<?>)}\n" +
                "     * <code> List<String>#containsAll(Collection<?>) </code>\n" +
                "     * @see List<String>#containsAll(Collection<?>) \n" +
                "     * <code>String</code> \n";

        String expectedValue = "/**\n" +
                "     * @param map - Map (String - key,  String - value) {@link Map}\n" +
                "     * @param list - List of generics type String\n" +
                "     * {@link List#containsAll(Collection)}\n" +
                "     * {@link List#containsAll(Collection)}\n" +
                "     * {@code List#containsAll(Collection)}\n" +
                "     * <code> List#containsAll(Collection) </code>\n" +
                "     * @see List#containsAll(Collection) \n" +
                "     * <code>String</code> \n";

        String actualValue = javadocFixingHandler.fixGenerics(testValue);

        assertEquals(expectedValue, actualValue);
    }

    @Test
    public void fixSelfEnclosingAndEmptyTags_successFlow() {
        String testValue = "/**\n" +
                "     * <p/>\n" +
                "     * <tag/>\n" +
                "     * <tag></tag><another tag>\n" +
                "     * <tag>Tag-body</tag>\"\">\n" +
                "*/";

        String expectedValue = "/**\n" +
                "     * \n" +
                "     * <tag>\n" +
                "     * <another tag>\n" +
                "     * <tag>Tag-body</tag>\"\">\n" +
                "*/";

        String actualValue = javadocFixingHandler.fixSelfEnclosingAndEmptyTags(testValue);

        assertEquals(expectedValue, actualValue);
    }

    @Test
    public void fixSelfInventedAnnotations_successFlow() {
        String testValue = "/**\n" +
                "     * @see" +
                "     * @custom-annotation\n" +
                "     * @Annotation\n" +
                "     * <a href=\"mailto:me@gmail.com\">me</a>\n" +
                "*/";

        String expectedValue = "/**\n" +
                "     * @see" +
                "     * Custom-annotation\n" +
                "     * Annotation\n" +
                "     * <a href=\"mailto:me@gmail.com\">me</a>\n" +
                "*/";

        String actualValue = javadocFixingHandler.fixSelfInventedAnnotations(testValue);

        assertEquals(expectedValue, actualValue);
    }

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

        DescribedEntity resultDescribedEntity = javadocFixingHandler.getDescribedEntity(167, testValue);

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

        DescribedEntity resultDescribedEntity = javadocFixingHandler.getDescribedEntity(65, testValue);

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

        DescribedEntity resultDescribedEntity = javadocFixingHandler.getDescribedEntity(16, testValue);

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

        DescribedEntity resultDescribedEntity = javadocFixingHandler.getDescribedEntity(16, testValue);

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

        MethodDescription resultMethodDescription = javadocFixingHandler.getMethodDescription(testDescribedEntity);

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

        MethodDescription resultMethodDescription = javadocFixingHandler.getMethodDescription(testDescribedEntity);

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