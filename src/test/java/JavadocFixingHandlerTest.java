import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
    public void fixSelfEnclosingAndEmptyTags() {
        String testValue = "/**\n" +
                "     * <p/>\n" +
                "     * <tag></tag>\n" +
                "     * <tag>Tag-body</tag>\"\">\n" +
                "*/";

        String expectedValue = "/**\n" +
                "     * \n" +
                "     * \n" +
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
                "*/";

        String expectedValue = "/**\n" +
                "     * @see" +
                "     * Custom-annotation\n" +
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

}