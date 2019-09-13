import java.io.File;

public class JavadocFixingHandler {

    private final static String START_PATTERN = "/**";
    private final static String END_PATTERN = "*/";

    public void fix(File file) {
        StringBuilder fileContent = new StringBuilder(FileContentHandler.getFileContent(file));

        fixAmpersands(fileContent);
    }

    // Visible for testing
    String fixAmpersands(StringBuilder fileContent) {
        int javadocStart = 0;
        int javadocEnd = 0;

        while (true) {
             javadocStart = fileContent.indexOf(START_PATTERN, javadocStart);
             javadocEnd = fileContent.indexOf(END_PATTERN, javadocEnd);

            if (javadocStart < 0 || javadocEnd < 0) {
                break;
            }

            String javadoc = fileContent.substring(javadocStart, javadocEnd);
            javadoc = javadoc.replaceAll("(&lt;|&lt)+", "<");
            javadoc = javadoc.replaceAll("(&gt;|&gt)+", ">");
            javadoc = javadoc.replaceAll("[ ]+[&]+[ ]+", " and ");
            javadoc = javadoc.replaceAll("&+", " and ");
            fileContent.replace(javadocStart, javadocEnd, javadoc);
        }

        System.out.println(fileContent);

        return fileContent.toString();
    }

}
