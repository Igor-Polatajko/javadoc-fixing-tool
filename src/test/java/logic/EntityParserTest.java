package logic;

import entity.ConstructorDescription;
import entity.DescribedEntity;
import entity.MethodDescription;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static logic.TestUtils.assertListEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

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
                "    @Annotation\n" +
                "    void m(@AnotherAnnotation(\"a\") Param param, @AndAnotherOne Object o) throws Exception {\n" +
                "\n" +
                "\n" +
                "    }\n" +
                "\n" +
                "}\n" +
                "\n";

        String expectedData = "*/        void m(Param param, Object o) throws Exception ";

        DescribedEntity resultDescribedEntity = EntityParser.getDescribedEntity(167, testValue);

        assertTrue(resultDescribedEntity.isPresent());
        assertEquals(DescribedEntity.Type.METHOD, resultDescribedEntity.getType());
        assertEquals(expectedData, resultDescribedEntity.getData());
    }

    @Test
    public void getDescribedEntity_successFlow_method_curlyBracketOnNewLine() {
        String testValue = "public class App {\n" +
                "\n" +
                "\n" +
                "    /**\n" +
                "     * a and b\n" +
                "     * Map  (String - key,  String - value)\n" +
                "     *     {@code Map }\n" +
                "     *         Collection of generics type String\n" +
                "     */\n" +
                "    @Annotation\n" +
                "    void m()\n" +
                " throws Exception {\n" +
                "\n" +
                "\n" +
                "    }\n" +
                "\n" +
                "}\n" +
                "\n";

        String expectedData = "*/        void m() throws Exception ";

        DescribedEntity resultDescribedEntity = EntityParser.getDescribedEntity(167, testValue);

        assertTrue(resultDescribedEntity.isPresent());
        assertEquals(DescribedEntity.Type.METHOD, resultDescribedEntity.getType());
        assertEquals(expectedData, resultDescribedEntity.getData());
    }

    @Test
    public void getDescribedEntity_successFlow_method_declarationOnly() {
        String testValue = "public class App {\n" +
                "\n" +
                "\n" +
                "    /**\n" +
                "     * a and b\n" +
                "     * Map  (String - key,  String - value)\n" +
                "     *     {@code Map }\n" +
                "     *         Collection of generics type String\n" +
                "     */\n" +
                "    @Annotation\n" +
                "    void m() throws Exception;\n" +
                "\n" +
                "\n" +
                "    }\n" +
                "\n" +
                "}\n" +
                "\n";

        String expectedData = "*/        void m() throws Exception";

        DescribedEntity resultDescribedEntity = EntityParser.getDescribedEntity(167, testValue);

        assertTrue(resultDescribedEntity.isPresent());
        assertEquals(DescribedEntity.Type.METHOD, resultDescribedEntity.getType());
        assertEquals(expectedData, resultDescribedEntity.getData());
    }

    @Test
    public void getDescribedEntity_successFlow_method_varargs() {
        String testValue = "public class App {\n" +
                "\n" +
                "\n" +
                "    /**\n" +
                "     * a and b\n" +
                "     * Map  (String - key,  String - value)\n" +
                "     *     {@code Map }\n" +
                "     *         Collection of generics type String\n" +
                "     */\n" +
                "    @Annotation\n" +
                "    void m(String... args) throws Exception {\n" +
                "}\n" +
                "\n" +
                "    }\n" +
                "\n" +
                "}\n" +
                "\n";

        String expectedData = "*/        void m(String... args) throws Exception ";

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

        String expectedData = "*/    String field";

        DescribedEntity resultDescribedEntity = EntityParser.getDescribedEntity(65, testValue);

        assertTrue(resultDescribedEntity.isPresent());
        assertEquals(DescribedEntity.Type.FIELD, resultDescribedEntity.getType());
        assertEquals(expectedData, resultDescribedEntity.getData());
    }

    @Test
    public void getDescribedEntity_successFlow_fieldOnly() {
        String testValue = "public class App {\n" +
                "\n" +
                "\n" +
                "    /**\n" +
                "    some field documentation * \n" +
                "    */\n" +
                "    String field; \n";


        String expectedData = "*/    String field";

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
        String expectedData = "*/public class App ";

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
                "@Annotation\n" +
                "public interface App {\n" +
                "\n" +
                "    void m() throws Exception ;\n" +
                "}\n" +
                "\n";

        String expectedData = "*/public interface App ";

        DescribedEntity resultDescribedEntity = EntityParser.getDescribedEntity(16, testValue);

        assertTrue(resultDescribedEntity.isPresent());
        assertEquals(DescribedEntity.Type.INTERFACE, resultDescribedEntity.getType());
        assertEquals(expectedData, resultDescribedEntity.getData());
    }

    @Test
    public void getDescribedEntity_successFlow_constructor() {
        String testValue = "public class App {\n" +
                "\n" +
                "\n" +
                "    /**\n" +
                "     * a and b\n" +
                "     * Map  (String - key,  String - value)\n" +
                "     *     {@code Map }\n" +
                "     *         Collection of generics type String\n" +
                "     */\n" +
                "    @Annotation\n" +
                "    public Class() {\n" +
                "\n" +
                "\n" +
                "    }\n" +
                "\n" +
                "}\n" +
                "\n";

        String expectedData = "*/        public Class() ";

        DescribedEntity resultDescribedEntity = EntityParser.getDescribedEntity(167, testValue);

        assertTrue(resultDescribedEntity.isPresent());
        assertEquals(DescribedEntity.Type.CONSTRUCTOR, resultDescribedEntity.getType());
        assertEquals(expectedData, resultDescribedEntity.getData());
    }

    @Test
    public void getDescribedEntity_successFlow_constructor_case2() {
        String testValue = "public class App {\n" +
                "\n" +
                "\n" +
                "    /**\n" +
                "     * a and b\n" +
                "     * Map  (String - key,  String - value)\n" +
                "     *     {@code Map }\n" +
                "     *         Collection of generics type String\n" +
                "     */\n" +
                "    @Annotation\n" +
                "    public Class(String string) throws Exception {\n" +
                "\n" +
                "\n" +
                "    }\n" +
                "\n" +
                "}\n" +
                "\n";

        String expectedData = "*/        public Class(String string) throws Exception ";

        DescribedEntity resultDescribedEntity = EntityParser.getDescribedEntity(167, testValue);

        assertTrue(resultDescribedEntity.isPresent());
        assertEquals(DescribedEntity.Type.CONSTRUCTOR, resultDescribedEntity.getType());
        assertEquals(expectedData, resultDescribedEntity.getData());
    }

    @Test
    public void getDescribedEntity_successFlow_another() {
        String testValue = "/**\n" +
                " * Javadoc\n" +
                " */\n" +
                "context.checking(new Expectations() {}";

        String expectedData = "*/context.checking(new Expectations() ";

        DescribedEntity resultDescribedEntity = EntityParser.getDescribedEntity(16, testValue);

        assertTrue(resultDescribedEntity.isPresent());
        assertEquals(DescribedEntity.Type.ANOTHER, resultDescribedEntity.getType());
        assertEquals(expectedData, resultDescribedEntity.getData());
    }

    @Test
    public void getDescribedEntity_successFlow_another_objectCreationNotResolvedAsConstructorDescription() {
        String testValue = "/**\n" +
                " * Javadoc\n" +
                " */\n" +
                "public static final ref = new Class(\"string\");\n";

        String expectedData = "*/public static final ref = new Class(\"string\")";

        DescribedEntity resultDescribedEntity = EntityParser.getDescribedEntity(16, testValue);

        assertTrue(resultDescribedEntity.isPresent());
        assertEquals(DescribedEntity.Type.ANOTHER, resultDescribedEntity.getType());
        assertEquals(expectedData, resultDescribedEntity.getData());
    }

    @Test
    public void getDescribedEntity_successFlow_another_enumProblem1() {
        String testValue = "/** 123 */\n" +
                "    XX1(\"123\"),\n" +
                "    /** 123 */\n" +
                "    XX2(\"123\"),\n" +
                "    /** 123 */\n" +
                "    XX3(\"123\"),\n" +
                "    /** 123 */\n" +
                "    XX4(\"123\"),\n" +
                "    /** 123 */\n" +
                "    XX5(\"123\");";

        DescribedEntity resultDescribedEntity = EntityParser.getDescribedEntity(10, testValue);

        assertTrue(resultDescribedEntity.isPresent());
        assertEquals(DescribedEntity.Type.ANOTHER, resultDescribedEntity.getType());
    }

    @Test
    public void getDescribedEntity_successFlow_another_enumProblem2() {
        String testValue = "/**\n" +
                "     * xxx\n" +
                "     */\n" +
                "    XXX(xxxx);";

        DescribedEntity resultDescribedEntity = EntityParser.getDescribedEntity(24, testValue);

        assertTrue(resultDescribedEntity.isPresent());
        assertEquals(DescribedEntity.Type.ANOTHER, resultDescribedEntity.getType());
    }

    @Test
    public void getEntityDetailDescription_method() {
        DescribedEntity testDescribedEntity = new DescribedEntity();
        testDescribedEntity.setType(DescribedEntity.Type.METHOD);
        testDescribedEntity.setPresent(true);
        testDescribedEntity.setData("public Set<String> method(List<String> strings,\n    Integer integer)" +
                " throws Exception, IOException,SQLException,     FileNotFoundException");
        List<String> expectedParams = Arrays.asList("List<String> strings", "Integer integer");
        List<String> expectedExceptionsThrown = Arrays.asList("Exception", "IOException", "SQLException", "FileNotFoundException");

        MethodDescription resultMethodDescription = (MethodDescription) EntityParser.getEntityDetailDescription(testDescribedEntity);

        assertTrue(resultMethodDescription.isPresent());
        assertEquals("Set<String>", resultMethodDescription.getReturnType());
        assertListEquals(expectedParams, resultMethodDescription.getParams());
        assertListEquals(expectedExceptionsThrown, resultMethodDescription.getExceptionsThrown());
    }

    @Test
    public void getEntityDetailDescription_method_twoArgsGenericsReturnType() {
        DescribedEntity testDescribedEntity = new DescribedEntity();
        testDescribedEntity.setType(DescribedEntity.Type.METHOD);
        testDescribedEntity.setPresent(true);
        testDescribedEntity.setData("Map<String, String> method(Map<Integer, String> map)" +
                " throws Exception, IOException");
        List<String> expectedParams = Collections.singletonList("Map<Integer, String> map");
        List<String> expectedExceptionsThrown = Arrays.asList("Exception", "IOException");

        MethodDescription resultMethodDescription = (MethodDescription) EntityParser.getEntityDetailDescription(testDescribedEntity);

        assertTrue(resultMethodDescription.isPresent());
        assertEquals("Map<String, String>", resultMethodDescription.getReturnType());
        assertListEquals(expectedParams, resultMethodDescription.getParams());
        assertListEquals(expectedExceptionsThrown, resultMethodDescription.getExceptionsThrown());
    }

    @Test
    public void getEntityDetailDescription_method_noParams() {
        DescribedEntity testDescribedEntity = new DescribedEntity();
        testDescribedEntity.setType(DescribedEntity.Type.METHOD);
        testDescribedEntity.setPresent(true);
        testDescribedEntity.setData("Map<String, String> method()" +
                " throws Exception, IOException");
        List<String> expectedParams = Collections.emptyList();
        List<String> expectedExceptionsThrown = Arrays.asList("Exception", "IOException");

        MethodDescription resultMethodDescription = (MethodDescription) EntityParser.getEntityDetailDescription(testDescribedEntity);

        assertTrue(resultMethodDescription.isPresent());
        assertEquals("Map<String, String>", resultMethodDescription.getReturnType());
        assertListEquals(expectedParams, resultMethodDescription.getParams());
        assertListEquals(expectedExceptionsThrown, resultMethodDescription.getExceptionsThrown());
    }

    @Test
    public void getEntityDetailDescription_method_void() {
        DescribedEntity testDescribedEntity = new DescribedEntity();
        testDescribedEntity.setType(DescribedEntity.Type.METHOD);
        testDescribedEntity.setPresent(true);
        testDescribedEntity.setData("void method(Integer integer)" +
                " throws Exception");
        List<String> expectedParams = Collections.singletonList("Integer integer");
        List<String> expectedExceptionsThrown = Collections.singletonList("Exception");

        MethodDescription resultMethodDescription = (MethodDescription) EntityParser.getEntityDetailDescription(testDescribedEntity);

        assertTrue(resultMethodDescription.isPresent());
        assertEquals("void", resultMethodDescription.getReturnType());
        assertListEquals(expectedParams, resultMethodDescription.getParams());
        assertListEquals(expectedExceptionsThrown, resultMethodDescription.getExceptionsThrown());
    }

    @Test
    public void getEntityDetailDescription_method_complexGenerics_case1() {
        DescribedEntity testDescribedEntity = new DescribedEntity();
        testDescribedEntity.setType(DescribedEntity.Type.METHOD);
        testDescribedEntity.setPresent(true);
        testDescribedEntity.setData("void method(Map<Class<?>, SomeClass> par)" +
                " throws Exception");
        List<String> expectedParams = Collections.singletonList("Map<Class<?>, SomeClass> par");
        List<String> expectedExceptionsThrown = Collections.singletonList("Exception");

        MethodDescription resultMethodDescription = (MethodDescription) EntityParser.getEntityDetailDescription(testDescribedEntity);

        assertTrue(resultMethodDescription.isPresent());
        assertEquals("void", resultMethodDescription.getReturnType());
        assertListEquals(expectedParams, resultMethodDescription.getParams());
        assertListEquals(expectedExceptionsThrown, resultMethodDescription.getExceptionsThrown());
    }


    @Test
    public void getEntityDetailDescription_method_complexGenerics_case2() {
        DescribedEntity testDescribedEntity = new DescribedEntity();
        testDescribedEntity.setType(DescribedEntity.Type.METHOD);
        testDescribedEntity.setPresent(true);
        testDescribedEntity.setData("void method(Map<Class<?>, SomeClass<List<String>," +
                " Map<Integer, String>>> par) throws Exception");
        List<String> expectedParams = Collections.singletonList("Map<Class<?>, SomeClass<List<String>," +
                " Map<Integer, String>>> par");
        List<String> expectedExceptionsThrown = Collections.singletonList("Exception");

        MethodDescription resultMethodDescription = (MethodDescription) EntityParser.getEntityDetailDescription(testDescribedEntity);

        assertTrue(resultMethodDescription.isPresent());
        assertEquals("void", resultMethodDescription.getReturnType());
        assertListEquals(expectedParams, resultMethodDescription.getParams());
        assertListEquals(expectedExceptionsThrown, resultMethodDescription.getExceptionsThrown());
    }

    @Test
    public void getEntityDetailDescription_constructor() {
        DescribedEntity testDescribedEntity = new DescribedEntity();
        testDescribedEntity.setType(DescribedEntity.Type.CONSTRUCTOR);
        testDescribedEntity.setPresent(true);
        testDescribedEntity.setData("Constructor(Map<Class<?>, SomeClass<List<String>," +
                " Map<Integer, String>>> par) throws Exception");
        List<String> expectedParams = Collections.singletonList("Map<Class<?>, SomeClass<List<String>," +
                " Map<Integer, String>>> par");
        List<String> expectedExceptionsThrown = Collections.singletonList("Exception");

        ConstructorDescription resultMethodDescription = (ConstructorDescription) EntityParser.getEntityDetailDescription(testDescribedEntity);

        assertTrue(resultMethodDescription.isPresent());
        assertListEquals(expectedParams, resultMethodDescription.getParams());
        assertListEquals(expectedExceptionsThrown, resultMethodDescription.getExceptionsThrown());
    }

    @Test
    public void parseReturnType() {
        String testSignature = "Map<String, String> method(Map<Class<?>, SomeClass<List<String>,\n" +
                " Map<Integer, String>>> par) throws Exception";
        String expected = "Map<String, String>";

        String actual = EntityParser.parseReturnType(testSignature);
        assertEquals(expected, actual);
    }

    @Test
    public void parseReturnType_accessModifier() {
        String testSignature = "public void method(Map<Class<?>, SomeClass<List<String>,\n" +
                " Map<Integer, String>>> par) throws Exception";
        String expected = "void";

        String actual = EntityParser.parseReturnType(testSignature);
        assertEquals(expected, actual);
    }

    @Test
    public void parseReturnType_constructor() {
        String testSignature = "Method(Map<Class<?>, SomeClass<List<String>,\n" +
                " Map<Integer, String>>> par) throws Exception";

        String actual = EntityParser.parseReturnType(testSignature);
        assertNull(actual);
    }

    @Test
    public void parseParams() {
        String testSignature = "void m(Map<Class<?>, SomeClass<List<String>,\n" +
                " Map<Integer, String>>> par,  List<String> strings, Map<Integer, List<String>>) throws Exception";
        List<String> expected = Arrays.asList("Map<Class<?>, SomeClass<List<String>," +
                " Map<Integer, String>>> par", "List<String> strings", "Map<Integer, List<String>>");

        List<String> actual = EntityParser.parseParams(testSignature);

        assertListEquals(expected, actual);
    }

    @Test
    public void parseParams_noParams() {
        String testSignature = "void m( ) throws Exception";
        List<String> expected = Collections.emptyList();
        List<String> actual = EntityParser.parseParams(testSignature);

        assertListEquals(expected, actual);
    }

    @Test
    public void parseExceptionsThrown() {
        String testSignature = "void m() throws Exception, IOException, FileNotFoundException";
        List<String> expected = Arrays.asList("Exception", "IOException", "FileNotFoundException");
        List<String> actual = EntityParser.parseExceptionsThrown(testSignature);

        assertListEquals(expected, actual);
    }

}