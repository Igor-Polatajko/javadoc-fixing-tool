package logic;

import entity.MethodDescription;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

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
    public void fixReturnWithVoidMethod() {
        String testValue = "/**\n" +
                "*     @return result\n" +
                "*/";

        String expectedValue = "/**\n" +
                "*/";

        MethodDescription testMethodDescription = new MethodDescription();
        testMethodDescription.setPresent(true);
        testMethodDescription.setReturnType("void");

        String actualValue = javadocFixingHandler.fixReturnWithVoidMethod(testValue, testMethodDescription);

        assertEquals(expectedValue, actualValue);
    }

    @Test
    public void fixThrowsStatements() {
        String testValue = "/**\n" +
                "     *\n" +
                "     * @throws Exception - occurs when something happened\n" +
                "     * @throws RuntimeException\n" +
                "     * @throws IOException\n" +
                "     */";

        String expectedValue = "/**\n" +
                "     *\n" +
                "     * @throws Exception - occurs when something happened\n" +
                "     * @throws IOException - exception\n" +
                "     * @throws FileNotFoundException - exception\n     " +
                "*/";

        MethodDescription testMethodDescription = new MethodDescription();
        testMethodDescription.setExceptionsThrown(Arrays.asList("Exception", "FileNotFoundException", "IOException"));

        String actualValue = javadocFixingHandler.fixThrowsStatements(testValue, testMethodDescription);

        assertEquals(expectedValue, actualValue);
    }
}