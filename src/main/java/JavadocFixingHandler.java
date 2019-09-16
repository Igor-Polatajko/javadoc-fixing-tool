import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JavadocFixingHandler {

    private final static String START_PATTERN = "/**";
    private final static String END_PATTERN = "*/";

    public void fix(File file) {
        String fileContent = FileContentHandler.getFileContent(file);
        String fixedJavadoc = fixJavadocSyntaxOnlyProblems(fileContent);

        if (!fileContent.equals(fixedJavadoc)) {
            rewriteFile(file, fixedJavadoc);
        }

        System.gc();
    }

    /*package*/ String fixJavadocSyntaxOnlyProblems(String fileContentString) {
        int javadocStart = 0;
        int javadocEnd = 0;

        StringBuilder fileContent = new StringBuilder(fileContentString);

        while (true) {
            javadocStart = fileContent.indexOf(START_PATTERN, javadocStart + 1);
            javadocEnd = fileContent.indexOf(END_PATTERN, javadocStart + 1);

            if (javadocStart < 0 || javadocEnd < 0) {
                break;
            }

            String javadoc = fileContent.substring(javadocStart, javadocEnd);

            // Fixing
            String fixedJavadoc = fixAmpersands(javadoc);
            fixedJavadoc = fixBadUseOfAngleBrackets(fixedJavadoc);
            fixedJavadoc = fixGenerics(fixedJavadoc);

            fileContent.replace(javadocStart, javadocEnd, fixedJavadoc);
        }

        return fileContent.toString();
    }

    // Visible for testing
    String fixAmpersands(String javadoc) {
        javadoc = javadoc.replaceAll("(&lt;|&lt)+", "<"); // "&lt;" -> "<"
        javadoc = javadoc.replaceAll("(&gt;|&gt)+", ">"); // "&gt;" -> ">"
        javadoc = javadoc.replaceAll("(&+)|([ ]+[&]+[ ]+)", " and "); // "&" -> "and"
        return javadoc;
    }

    // Visible for testing
    String fixBadUseOfAngleBrackets(String javadoc) {
        javadoc = javadoc.replaceAll("[ ]+[>][ ]+", " greater than "); // ">" -> "greater than"
        javadoc = javadoc.replaceAll("[ ]+[<][ ]+", " less than "); // "<" -> "less than"
        javadoc = javadoc.replaceAll("[ ]+[>=][ ]+", " equal or greater than "); // ">=" -> "equal or greater than"
        javadoc = javadoc.replaceAll("[ ]+[<=][ ]+", " equal or less than "); // "<=" -> "equal or less than"
        javadoc = javadoc.replaceAll("[-]+[>]", "---"); // "->" -> "---"

        return javadoc;
    }

    // Visible for testing
    String fixGenerics(String javadoc) {
        Pattern pattern = Pattern.compile("[A-Z]+[A-Za-z0-9]*[ ]?[<].*?[>]");
        Matcher matcher = pattern.matcher(javadoc);

        while (matcher.find()) {
            String generics = matcher.group();

            if (noGenerics(generics)) {
                continue;
            }

            int paramsStart = generics.indexOf("<");
            int paramsEnd = generics.lastIndexOf(">");

            String classType = generics.substring(0, paramsStart);
            String[] params = generics.substring(paramsStart + 1, paramsEnd).split(",|, ");

            String replacement = null;
            boolean replacementReady = false;

            boolean removeGenericsAtAllArea =
                    insideBlock(javadoc, "@link", matcher.start()) ||
                            insideBlock(javadoc, "@code", matcher.start()) ||
                            insideTag(javadoc, "code", matcher.start()) ||
                            afterWordInOneLine(javadoc, "@see", matcher.start());

            if (removeGenericsAtAllArea) {
                replacement = classType;
                replacementReady = true;
            }

            if (params.length == 0 && !replacementReady) {
                replacement = classType + " - generics";
                replacementReady = true;
            }

            // Specific case for Map
            if (classType.contains("Map") && params.length == 2 && !replacementReady) {
                replacement = classType + " (" + params[0] + " - key, " + params[1] + " - value)";
                replacementReady = true;
            }

            if (params.length == 1 && !replacementReady) {
                replacement = classType + " of generics type " + params[0];
                replacementReady = true;
            }

            if (!replacementReady) {
                replacement = classType + " - of generics types: ";

                for (int i = 0; i < params.length; i++) {
                    replacement += params[i];

                    if (i < params.length - 2) {
                        replacement += ", ";
                    }
                }
            }

            // To prevent bug when generics is <?>
            generics = generics.replaceAll("[?]", "");
            javadoc = javadoc.replaceAll("<[?]>", "");

            javadoc = javadoc.replaceFirst(generics, replacement);
            matcher = pattern.matcher(javadoc);
        }
        return javadoc;
    }

    private boolean insideBlock(String javadoc, String blockType, int index) {
        int indexOfBlockTypeStartInsideJavadoc = 0;

        while (true) {
            indexOfBlockTypeStartInsideJavadoc = javadoc.indexOf(blockType, indexOfBlockTypeStartInsideJavadoc + 1);
            int indexOfBlockTypeEndInsideJavadoc = javadoc.indexOf("}", indexOfBlockTypeStartInsideJavadoc + 1);

            if (indexOfBlockTypeStartInsideJavadoc < 0 || indexOfBlockTypeEndInsideJavadoc < 0) {
                return false;
            }

            if (index > indexOfBlockTypeStartInsideJavadoc && index < indexOfBlockTypeEndInsideJavadoc) {
                return true;
            }
        }
    }

    private boolean insideTag(String javadoc, String tag, int index) {
        int indexOfOpeningTag = 0;

        while (true) {
            indexOfOpeningTag = javadoc.indexOf("<" + tag + ">", indexOfOpeningTag + 1);
            int indexOfClosingTag = javadoc.indexOf("</" + tag + ">", indexOfOpeningTag + 1);

            if (indexOfOpeningTag < 0 || indexOfClosingTag < 0) {
                return false;
            }

            if (index > indexOfOpeningTag && index < indexOfClosingTag) {
                return true;
            }
        }
    }

    private boolean afterWordInOneLine(String javadoc, String word, int index) {
        int indexOfWordStart = 0;

        while (true) {
            indexOfWordStart = javadoc.indexOf(word, indexOfWordStart + 1);
            int indexOfNewLine = javadoc.indexOf("\n", indexOfWordStart + 1);

            if (indexOfWordStart < 0 || indexOfNewLine < 0) {
                return false;
            }

            if (index > indexOfWordStart && index < indexOfNewLine) {
                return true;
            }
        }
    }

    private boolean noGenerics(String generics) {
        return generics.matches(".*</.+?>.*");
    }

    private void rewriteFile(File file, String newContent) {
        try {
            FileWriter fooWriter = new FileWriter(file, false);
            fooWriter.write(newContent);
            fooWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
