package logic;

import entity.DescribedEntity;
import entity.MethodDescription;
import fileHandler.FileContentHandler;

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
            DescribedEntity describedEntity = EntityParser.getDescribedEntity(javadocEnd, fileContent.toString());

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
        MethodDescription methodDescription = EntityParser.getMethodDescription(describedEntity);

        String fixedJavadoc = fixReturnWithVoidMethod(javadoc, methodDescription);
        // ToDo add another fixes
        return fixedJavadoc;
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
    String fixReturnWithVoidMethod(String javadoc, MethodDescription methodDescription){
        if (!methodDescription.isPresent() || !methodDescription.getReturnType().equals("void")) {
            return javadoc;
        }

        return javadoc.replaceAll("[@]return.*", "");
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

            if (!allowedAnnotations.contains(annotation) && !ParserUtils.insideTag(javadoc, "a", matcher.start())) {
                String replacement =
                        annotation.substring(1, 2).toUpperCase() + annotation.substring(2);
                javadoc = javadoc.replaceFirst(ParserUtils.eliminateSpecialChars(annotation), replacement);
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
            javadoc = javadoc.replaceFirst(ParserUtils.eliminateSpecialChars(notCorrectlyClosedTag), fixedTag);
        }

        return javadoc;
    }

    // Visible for testing
    String fixGenerics(String javadoc) {
        Pattern pattern = Pattern.compile("[A-Z]+[A-Za-z0-9]*[ ]?[<].*?[>]");
        Matcher matcher = pattern.matcher(javadoc);

        while (matcher.find()) {
            String generics = matcher.group();

            if (ParserUtils.noGenerics(generics)) {
                continue;
            }

            int paramsStart = generics.indexOf("<");
            int paramsEnd = generics.lastIndexOf(">");

            String classType = generics.substring(0, paramsStart);
            String[] params = generics.substring(paramsStart + 1, paramsEnd).split(",|, ");

            String replacement = null;
            boolean replacementReady = false;

            boolean removeGenericsAtAllArea =
                    ParserUtils.insideBlock(javadoc, "@link", matcher.start()) ||
                            ParserUtils.insideBlock(javadoc, "@code", matcher.start()) ||
                            ParserUtils.insideTag(javadoc, "code", matcher.start()) ||
                            ParserUtils.afterWordInOneLine(javadoc, "@see", matcher.start());

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

            javadoc = javadoc.replaceFirst(ParserUtils.eliminateSpecialChars(generics), replacement);
            matcher = pattern.matcher(javadoc);
        }
        return javadoc;
    }
}
