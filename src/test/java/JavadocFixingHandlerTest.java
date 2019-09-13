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
        String testValue = "public class Class {\n" +
                "\n" +
                "    /**\n" +
                "     * a & b\n" +
                "     * a&b\n" +
                "     * &lt;tag>\n" +
                "     * &lttag>\n" +
                "     * <tag&gt;\n" +
                "     * &ltt&gt\n" +
                "     */\n" +
                "    // a & b\n" +
                "    public void m(){\n" +
                "\n" +
                "    }\n" +
                "}\n";

        String expectedValue = "public class Class {\n" +
                "\n" +
                "    /**\n" +
                "     * a and b\n" +
                "     * a and b\n" +
                "     * <tag>\n" +
                "     * <tag>\n" +
                "     * <tag>\n" +
                "     * <t>\n" +
                "     */\n" +
                "    // a & b\n" +
                "    public void m(){\n" +
                "\n" +
                "    }\n" +
                "}\n";

        String actualValue = javadocFixingHandler.fixAmpersands(new StringBuilder(testValue));

        assertEquals(expectedValue, actualValue);
    }
}