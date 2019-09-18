import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JavadocFixingHandler {

    private final static String START_PATTERN = "/**";
    private final static String END_PATTERN = "*/";

    public void fix(File file) {
        String fileContent = FileContentHandler.getFileContent(file);
        String fixedJavadoc = fixJavadocSyntaxProblems(fileContent);

        if (!fileContent.equals(fixedJavadoc)) {
            FileContentHandler.rewriteFile(file, fixedJavadoc);
        }

        System.gc();
    }

    /*package*/ String fixJavadocSyntaxProblems(String fileContentString) {
        int javadocStart = -1;
        int javadocEnd;

        StringBuilder fileContent = new StringBuilder(fileContentString);

        while (true) {
            javadocStart = fileContent.indexOf(START_PATTERN, javadocStart + 1);
            javadocEnd = fileContent.indexOf(END_PATTERN, javadocStart + 1);

            if (javadocStart < 0 || javadocEnd < 0) {
                break;
            }

            String javadoc = fileContent.substring(javadocStart, javadocEnd);
            DescribedEntity describedEntity = getDescribedEntity(javadocEnd, fileContent.toString());

            String fixedJavadoc = javadoc;

            // Fixing javadoc based on described entity
            fixedJavadoc = fixJavadocBasedOnDescribedEntity(fixedJavadoc, describedEntity);

            // Fixing syntax problems
            fixedJavadoc = fixJavadocBasedOnSyntaxRequirements(fixedJavadoc);

            fileContent.replace(javadocStart, javadocEnd, fixedJavadoc);
        }

        return fileContent.toString();
    }

    /*package*/ String fixJavadocBasedOnDescribedEntity(String javadoc, DescribedEntity describedEntity) {
        if (!describedEntity.isPresent() || describedEntity.getType() != DescribedEntity.Type.METHOD) {
            return javadoc;
        }


        return javadoc;
    }

    /*package*/ String fixJavadocBasedOnSyntaxRequirements(String javadoc) {
        String fixedJavadoc = fixIncompleteTags(javadoc);
        fixedJavadoc = fixBadUseOfAngleBrackets(fixedJavadoc);
        fixedJavadoc = fixAmpersands(fixedJavadoc);
        fixedJavadoc = fixGenerics(fixedJavadoc);
        fixedJavadoc = fixSelfEnclosingAndEmptyTags(fixedJavadoc);
        fixedJavadoc = fixSelfInventedAnnotations(fixedJavadoc);

        return fixedJavadoc;
    }

    // Visible for testing
    MethodDescription getMethodDescription(DescribedEntity describedEntity) {
        MethodDescription methodDescription = new MethodDescription();
        methodDescription.setPresent(false);

        if (!describedEntity.isPresent() || describedEntity.getType() != DescribedEntity.Type.METHOD) {
            return methodDescription;
        }

        String methodSignature = describedEntity.getData();

        Matcher beforeParamsMatcher = Pattern.compile(".*?[(]").matcher(methodSignature);
        Matcher paramsMatcher = Pattern.compile("[(].*?[)]").matcher(methodSignature);
        Matcher afterParamsMatcher = Pattern.compile("[)].*").matcher(methodSignature);

        if (!beforeParamsMatcher.find() || !paramsMatcher.find()) {
            return methodDescription;
        }

        methodDescription.setPresent(true);

        String[] beforeParams = beforeParamsMatcher.group().trim().split(" ");
        String[] params = paramsMatcher.group().trim().split(", ");

        if (afterParamsMatcher.find()) {
            String[] afterParams = afterParamsMatcher.group().replaceAll("\\)|[,]", "").trim().split("\\s");

            if (afterParams[0].contains("throws")) {
                String[] exceptionsThrown = Arrays.copyOfRange(afterParams, 1, afterParams.length);
                methodDescription.setExceptionsThrown(Arrays.asList(exceptionsThrown));
            }
        }

        methodDescription.setReturnType(beforeParams[beforeParams.length - 2]);

        if (params.length > 0) {
            params[0] = params[0].replaceAll("\\(", "");
            params[params.length - 1] = params[params.length - 1].replaceAll("\\)", "");
        }

        methodDescription.setParams(Arrays.asList(params));

        return methodDescription;
    }

    // Visible for testing
    DescribedEntity getDescribedEntity(int javadocEndIndex, String fileContent) {
        DescribedEntity describedEntity = new DescribedEntity();

        int indexOfNextCurlyBracket = fileContent.indexOf("{", javadocEndIndex + 1);
        int indexOfNextSemicolon = fileContent.indexOf(";", javadocEndIndex + 1);

        if (indexOfNextCurlyBracket < 0 && indexOfNextSemicolon < 0) {
            describedEntity.setPresent(false);
            return describedEntity;
        }

        describedEntity.setPresent(true);

        if (indexOfNextCurlyBracket > indexOfNextSemicolon && indexOfNextCurlyBracket > 0 && indexOfNextSemicolon > 0) {
            describedEntity.setType(DescribedEntity.Type.FIELD);
            describedEntity.setData(fileContent.substring(javadocEndIndex, indexOfNextSemicolon));
            return describedEntity;
        }

        describedEntity.setData(fileContent.substring(javadocEndIndex, indexOfNextCurlyBracket));

        if (describedEntity.getData().contains(" class ")) {
            describedEntity.setType(DescribedEntity.Type.CLASS);
        } else if (describedEntity.getData().contains(" interface ")) {
            describedEntity.setType(DescribedEntity.Type.INTERFACE);
        } else {
            describedEntity.setType(DescribedEntity.Type.METHOD);
        }

        return describedEntity;
    }

    // Visible for testing
    String fixSelfEnclosingAndEmptyTags(String javadoc) {
        javadoc = javadoc.replaceAll("<[^\\/>][^>]*><\\/[^>]+>", ""); // <tag></tag> -> ""

        // Specific case for <p/>
        javadoc = javadoc.replaceAll("<p/>", "");

        Pattern pattern = Pattern.compile("<[^>]*?\\/>");
        Matcher matcher = pattern.matcher(javadoc);

        while (matcher.find()) {
            String tag = matcher.group();
            String fixedTag = tag.replaceAll("\\/", "");
            javadoc = javadoc.replace(tag, fixedTag);
            matcher = pattern.matcher(javadoc);
        }


        return javadoc;
    }

    // Visible for testing
    String fixSelfInventedAnnotations(String javadoc) {
        Set<String> allowedAnnotations = new HashSet<>(Arrays.asList(
                "@author", "@version", "@param",
                "@return", "@deprecated", "@since",
                "@throws", "@exception", "@see",
                "@serial", "@serialField", "@serialData",
                "@link", "@code"
        ));

        Pattern pattern = Pattern.compile("@+.+?\\b");
        Matcher matcher = pattern.matcher(javadoc);

        while (matcher.find()) {
            String annotation = matcher.group();

            if (!allowedAnnotations.contains(annotation) && !insideTag(javadoc, "a", matcher.start())) {
                String replacement =
                        annotation.substring(1, 2).toUpperCase() + annotation.substring(2);
                javadoc = javadoc.replaceFirst(eliminateSpecialChars(annotation), replacement);
                matcher = pattern.matcher(javadoc);
            }
        }

        return javadoc;
    }

    // Visible for testing
    String fixAmpersands(String javadoc) {
        return javadoc.replaceAll("(&+|[ ]+&+[ ]+)(?!(?:apos|quot|[gl]t|amp);|#)", " and "); // "&" -> "and"
    }

    // Visible for testing
    String fixBadUseOfAngleBrackets(String javadoc) {
        javadoc = javadoc.replaceAll("[ ]+>[ ]+", " greater than ") // ">" -> "greater than"
                .replaceAll("[ ]+<[ ]+", " less than ") // "<" -> "less than"
                .replaceAll("[ ]+>=[ ]+", " equal or greater than ") // ">=" -> "equal or greater than"
                .replaceAll("[ ]+<=[ ]+", " equal or less than ") // "<=" -> "equal or less than"
                .replaceAll("[-]+>", "---"); // "->" -> "---"

        return javadoc;
    }

    // Visible for testing
    String fixIncompleteTags(String javadoc) {
        Pattern pattern = Pattern.compile("&lt;[^<]*?>");
        Matcher matcher = pattern.matcher(javadoc);

        while (matcher.find()) {
            String notCorrectlyClosedTag = matcher.group();
            String fixedTag = notCorrectlyClosedTag.replaceAll(">", "&gt;");
            javadoc = javadoc.replaceFirst(eliminateSpecialChars(notCorrectlyClosedTag), fixedTag);
        }

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

            javadoc = javadoc.replaceFirst(eliminateSpecialChars(generics), replacement);
            matcher = pattern.matcher(javadoc);
        }
        return javadoc;
    }

    private boolean insideBlock(String javadoc, String blockType, int index) {
        int indexOfBlockTypeStartInsideJavadoc = -1;

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
        Pattern startTagPattern = Pattern.compile("<" + tag + ".*?>");
        Pattern endTagPattern = Pattern.compile("</" + tag + "");

        Matcher startTagMatcher = startTagPattern.matcher(javadoc);
        Matcher endTagMatcher = endTagPattern.matcher(javadoc);

        while (startTagMatcher.find() && endTagMatcher.find()) {
            if (endTagMatcher.start() > startTagMatcher.start() &&
                    endTagMatcher.start() > index && startTagMatcher.start() < index) {
                return true;
            }
        }

        return false;
    }

    private boolean afterWordInOneLine(String javadoc, String word, int index) {
        int indexOfWordStart = -1;

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

    private String eliminateSpecialChars(String regex) {
        String result = "";
        for (int i = 0; i < regex.length(); i++) {
            if (regex.charAt(i) == ']' || regex.charAt(i) == '[') {
                result += "[\\" + regex.charAt(i) + "]";
            } else {
                result += "[" + regex.charAt(i) + "]";
            }
        }

        return result;
    }

    private boolean noGenerics(String generics) {
        return generics.matches(".*</.+?>.*");
    }
}
