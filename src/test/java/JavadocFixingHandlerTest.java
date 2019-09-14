import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class JavadocFixingHandlerTest {

    private JavadocFixingHandler javadocFixingHandler;

    @Before
    public void setUp() {
        javadocFixingHandler = new JavadocFixingHandler();
    }

    @Test
    public void fixAmpersands_successFlow() {
        String testValue = "/**\n\r" +
                "     * a & b\n\r" +
                "     * a&b\n\r" +
                "     * &lt;tag>\n\r" +
                "     * &lttag>\n\r" +
                "     * <tag&gt;\n\r" +
                "     * &ltt&gt\n\r";

        String expectedValue = "/**\n\r" +
                "     * a and b\n\r" +
                "     * a and b\n\r" +
                "     * <tag>\n\r" +
                "     * <tag>\n\r" +
                "     * <tag>\n\r" +
                "     * <t>\n\r";

        String actualValue = javadocFixingHandler.fixAmpersands(testValue);

        assertEquals(expectedValue, actualValue);
    }

    @Test
    public void fixGenerics_successFlow() {
        String testValue = "/**\n\r" +
                "     * @param map - Map<String, String> {@link Map<String, String>}\n\r" +
                "     * @param list - List<String>\n\r" +
                "     * {@link List<String>#containsAll(Collection<String>)}\n\r" +
                "     * {@link List<String>#containsAll(Collection<?>)}\n\r";

        String expectedValue = "/**\n\r" +
                "     * @param map - Map (String - key,  String - value) {@link Map}\n\r" +
                "     * @param list - List of generics type String\n\r" +
                "     * {@link List#containsAll(Collection)}\n\r" +
                "     * {@link List#containsAll(Collection)}\n\r";

        String actualValue = javadocFixingHandler.fixGenerics(testValue);

        assertEquals(expectedValue, actualValue);
    }
}