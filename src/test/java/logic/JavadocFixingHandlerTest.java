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
                "     * &lt;complexContent\"123\">\n" +
                "*/";

        String expectedValue = "/**\n" +
                "     * &lt;complexContent&gt;\n" +
                "     * &lt;complexContent{}&gt;\n" +
                "     * &lt;complexContent\"123\"&gt;\n" +
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
    public void fixReturnStatements_void() {
        String testValue = "/**\n" +
                "*     @return result\n" +
                "*/";

        String expectedValue = "/**\n" +
                "*/";

        MethodDescription testMethodDescription = new MethodDescription();
        testMethodDescription.setPresent(true);
        testMethodDescription.setReturnType("void");

        String actualValue = javadocFixingHandler.fixReturnStatements(testValue, testMethodDescription);

        assertEquals(expectedValue, actualValue);
    }

    @Test
    public void fixReturnStatements_noReturn() {
        String testValue = "/**\n" +
                "     */";

        String expectedValue = "/**\n" +
                "     * @return Object\n" +
                "     */";

        MethodDescription testMethodDescription = new MethodDescription();
        testMethodDescription.setPresent(true);
        testMethodDescription.setReturnType("Object");

        String actualValue = javadocFixingHandler.fixReturnStatements(testValue, testMethodDescription);

        assertEquals(expectedValue, actualValue);
    }

    @Test
    public void fixReturnStatements_noReturn_throwsPresented() {
        String testValue = "/**\n" +
                "     * @throws IOException\n" +
                "     */";

        String expectedValue = "/**\n" +
                "     * @return Object\n" +
                "     * @throws IOException\n" +
                "     */";

        MethodDescription testMethodDescription = new MethodDescription();
        testMethodDescription.setPresent(true);
        testMethodDescription.setReturnType("Object");

        String actualValue = javadocFixingHandler.fixReturnStatements(testValue, testMethodDescription);

        assertEquals(expectedValue, actualValue);
    }

    @Test
    public void fixReturnStatements_noDescription() {
        String testValue = "/**\n" +
                "     * @return\n" +
                "     * @throws IOException\n" +
                "     */";

        String expectedValue = "/**\n" +
                "     * @return Object\n" +
                "     * @throws IOException\n" +
                "     */";

        MethodDescription testMethodDescription = new MethodDescription();
        testMethodDescription.setPresent(true);
        testMethodDescription.setReturnType("Object");

        String actualValue = javadocFixingHandler.fixReturnStatements(testValue, testMethodDescription);

        assertEquals(expectedValue, actualValue);
    }

    @Test
    public void fixReturnStatements_doubleReturn_case1() {
        String testValue = "/**\n" +
                "     * @return some result\n" +
                "     * @return\n" +
                "     * @throws IOException\n" +
                "     */";

        String expectedValue = "/**\n" +
                "     * @return some result\n" +
                "     *\n" +
                "     * @throws IOException\n" +
                "     */";

        MethodDescription testMethodDescription = new MethodDescription();
        testMethodDescription.setPresent(true);
        testMethodDescription.setReturnType("Object");

        String actualValue = javadocFixingHandler.fixReturnStatements(testValue, testMethodDescription);

        assertEquals(expectedValue, actualValue);
    }

    @Test
    public void fixReturnStatements_doubleReturn_case2() {
        String testValue = "/**\n" +
                "     * @return some result\n" +
                "     * @return some result\n" +
                "     * @throws IOException\n" +
                "     */";

        String expectedValue = "/**\n" +
                "     * @return some result\n" +
                "     * some result\n" +
                "     * @throws IOException\n" +
                "     */";

        MethodDescription testMethodDescription = new MethodDescription();
        testMethodDescription.setPresent(true);
        testMethodDescription.setReturnType("Object");

        String actualValue = javadocFixingHandler.fixReturnStatements(testValue, testMethodDescription);

        assertEquals(expectedValue, actualValue);
    }

    @Test
    public void fixReturnStatements_doubleReturn_case3() {
        String testValue = "/**\n" +
                "     * @return @return some result\n" +
                "     * @throws IOException\n" +
                "     */";

        String expectedValue = "/**\n" +
                "     * @return some result\n" +
                "     * @throws IOException\n" +
                "     */";

        MethodDescription testMethodDescription = new MethodDescription();
        testMethodDescription.setPresent(true);
        testMethodDescription.setReturnType("Object");

        String actualValue = javadocFixingHandler.fixReturnStatements(testValue, testMethodDescription);

        assertEquals(expectedValue, actualValue);
    }

    @Test
    public void fixReturnStatements_noChanges() {
        String testValue = "/**\n" +
                "     * @return Object\n" +
                "     * @throws IOException\n" +
                "     */";

        String expectedValue = "/**\n" +
                "     * @return Object\n" +
                "     * @throws IOException\n" +
                "     */";

        MethodDescription testMethodDescription = new MethodDescription();
        testMethodDescription.setPresent(true);
        testMethodDescription.setReturnType("Object");

        String actualValue = javadocFixingHandler.fixReturnStatements(testValue, testMethodDescription);

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

    @Test
    public void fixThrowsStatements_fullExceptionNameCase() {
        String testValue = "/**\n" +
                "     *\n" +
                "     * @throws java.io.IOException\n" +
                "     */";

        String expectedValue = "/**\n" +
                "     *\n" +
                "     * @throws java.io.IOException - exception\n" +
                "     * @throws Exception - exception\n" +
                "     */";

        MethodDescription testMethodDescription = new MethodDescription();
        testMethodDescription.setExceptionsThrown(Arrays.asList("Exception", "IOException"));

        String actualValue = javadocFixingHandler.fixThrowsStatements(testValue, testMethodDescription);

        assertEquals(expectedValue, actualValue);
    }
}