package logic;

import custom.VisibleForTesting;
import entity.DescribedEntity;
import entity.EntityDetailDescription;
import entity.MethodDescription;
import fileHandler.FileContentHandler;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static logic.ParserUtils.convertParams;
import static logic.ParserUtils.eliminateSpecialChars;
import static logic.ParserUtils.getStatements;
import static logic.ParserUtils.indexOfFirstOutsideCurlyBrackets;

public class JavadocFixingHandler {

    private final static String START_PATTERN = "/**";
    private final static String END_PATTERN = "*/";
    private final static String LINE_BEGIN_PATTERN = "[\\n].*?[*][^*]*?";

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
            javadocEnd = fileContent.indexOf(END_PATTERN, javadocStart + 1) + END_PATTERN.length();

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
        if (!describedEntity.isPresent()) {
            return javadoc;
        }
        EntityDetailDescription entityDescription = EntityParser.getEntityDetailDescription(describedEntity);

        if (!entityDescription.isPresent()) {
            return javadoc;
        }

        javadoc = fixParamStatements(javadoc, entityDescription);
        javadoc = fixThrowsStatements(javadoc, entityDescription);

        if (entityDescription instanceof MethodDescription) {
            javadoc = fixReturnStatements(javadoc, (MethodDescription) entityDescription);
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

    @VisibleForTesting
    String fixParamStatements(String javadoc, EntityDetailDescription entityDescription) {
        List<String> javadocParams = getStatements(javadoc, "param");
        List<List<String>> params = convertParams(entityDescription.getParams());
        String beforeParamNameRegex = "[^<]*?@param[\\s\\(]*?\\b";
        String afterParamNameRegex = "\\b[\\s\\)]*?[^<]*";

        for (String javadocParam : javadocParams) {
            List<String> validJavadocParameterName = params.stream()
                    .filter(p -> javadocParam.matches(beforeParamNameRegex + p.get(1) + afterParamNameRegex))
                    .findAny().orElse(null);

            if (validJavadocParameterName == null) {
                javadoc = javadoc.replaceFirst(LINE_BEGIN_PATTERN + eliminateSpecialChars(javadocParam), "");
                continue;
            }

            String[] javadocParamParts = javadocParam.split(" ");

            if (javadocParamParts.length < 3) {
                String replacement = "@param " + validJavadocParameterName.get(1) + " - the " +
                        validJavadocParameterName.get(1) +
                        " (" + replaceVarargs(validJavadocParameterName.get(0)) + ")";

                javadoc = javadoc.replaceFirst(eliminateSpecialChars(javadocParam), replacement);
            }

            if (javadocParamParts[1].matches("[\\(]" + validJavadocParameterName.get(1) + "[\\)]")) {
                javadoc = javadoc.replaceFirst(eliminateSpecialChars(
                        javadocParamParts[1]), validJavadocParameterName.get(1));
            }
        }

        if (params.isEmpty()) {
            return javadoc;
        }

        Collections.reverse(params);
        for (List<String> param : params) {
            boolean paramPresentedInJavadoc = javadocParams.stream()
                    .anyMatch(p -> p.matches(beforeParamNameRegex + param.get(1) + afterParamNameRegex));

            if (!paramPresentedInJavadoc) {
                javadoc = addParamToJavadoc(javadoc, param);
            }
        }
        return javadoc;
    }

    private String addParamToJavadoc(String javadoc, List<String> param) {
        int indexOfFirstStatementMarkOutsideCurlyBrackets = indexOfFirstOutsideCurlyBrackets(javadoc, "@");
        String parameterStatement = "@param " + param.get(1) + " - the "
                + param.get(1) + " (" + replaceVarargs(param.get(0)) + ")\n";

        if (indexOfFirstStatementMarkOutsideCurlyBrackets > 0) {
            return javadoc.substring(0, indexOfFirstStatementMarkOutsideCurlyBrackets) + parameterStatement + "     * "
                    + javadoc.substring(indexOfFirstStatementMarkOutsideCurlyBrackets);
        }

        return javadoc.replaceFirst("[*][/]",
                "* " + parameterStatement + "     */");

    }

    private String replaceVarargs(String paramType) {
        if (paramType.matches(".*?[.]{3}")) {
            return "varargs of type " + paramType.substring(0, paramType.length() - 3);
        }
        return paramType;
    }

    @VisibleForTesting
    String fixThrowsStatements(String javadoc, EntityDetailDescription entityDescription) {
        List<String> javadocThrows = getStatements(javadoc, "throws");

        for (String javadocThrow : javadocThrows) {
            String[] javadocThrowParts = javadocThrow.trim().split("\\s");
            String exceptionName = javadocThrowParts[1];

            boolean thrown = entityDescription.getExceptionsThrown().stream()
                    .anyMatch(ex -> exceptionName.matches("[^<]*\\b" + ex + "\\b[^<]*"));

            if (entityDescription.getExceptionsThrown().isEmpty() || !thrown) {
                javadoc = javadoc.replaceFirst(LINE_BEGIN_PATTERN + eliminateSpecialChars(javadocThrow), "");
                continue;
            }

            if (javadocThrowParts.length < 3) {
                javadoc = javadoc.replaceFirst(eliminateSpecialChars(javadocThrow), javadocThrow + " - exception");
            }
        }

        if (entityDescription.getExceptionsThrown().isEmpty()) {
            return javadoc;
        }

        for (String exception : entityDescription.getExceptionsThrown()) {
            boolean throwsPresentedInJavadoc = javadocThrows.stream()
                    .anyMatch(th -> th.matches("[^<]*\\b" + exception + "\\b[^<]*"));

            if (!throwsPresentedInJavadoc) {
                javadoc = javadoc.replaceFirst("[*][/]", "* @throws " + exception + " - exception\n     */");
            }
        }

        return javadoc;
    }

    String fixReturnStatements(String javadoc, MethodDescription methodDescription) {
        if (methodDescription.getReturnType().equals("void")) {
            javadoc = javadoc.replaceAll(LINE_BEGIN_PATTERN + "[@]return.*", "");
            return javadoc;
        }

        int indexOfReturn = javadoc.indexOf("@return");

        if (indexOfReturn < 0) {
            String returnStatement = "* @return " + methodDescription.getReturnType() + "\n     ";
            int indexOfThrows = javadoc.indexOf("* @throws");
            if (indexOfThrows < 0) {
                javadoc = javadoc.replaceFirst("[*][/]", returnStatement + "*/");
            } else {
                javadoc = javadoc.substring(0, indexOfThrows) + returnStatement + javadoc.substring(indexOfThrows);
            }
            return javadoc;
        }

        boolean noDescriptionForReturn = javadoc.substring(indexOfReturn).matches("@return[^\\w]*?[*][^$]*");
        if (noDescriptionForReturn) {
            int indexOfReturnEnd = indexOfReturn + 7;
            javadoc = javadoc.substring(0, indexOfReturnEnd) + " " + methodDescription.getReturnType() + javadoc.substring(indexOfReturnEnd);
        }

        javadoc = fixDoubleReturns(javadoc, indexOfReturn);

        return javadoc;
    }

    private String fixDoubleReturns(String javadoc, int indexOfReturn) {
        int indexOfNextReturnStatement = indexOfReturn + 7;
        while (true) {
            indexOfNextReturnStatement = javadoc.indexOf("@return", indexOfNextReturnStatement + 1);

            if (indexOfNextReturnStatement < 0) {
                break;
            }

            javadoc = javadoc.substring(0, indexOfNextReturnStatement - 1) + javadoc.substring(indexOfNextReturnStatement + 7);
        }

        return javadoc;
    }

    @VisibleForTesting
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

    @VisibleForTesting
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

    @VisibleForTesting
    String fixAmpersands(String javadoc) {
        return javadoc.replaceAll("(&+|[ ]+&+[ ]+)(?!(?:apos|quot|[gl]t|amp);|#)", " and "); // "&" -> "and"
    }

    @VisibleForTesting
    String fixBadUseOfAngleBrackets(String javadoc) {
        javadoc = javadoc.replaceAll("[ ]+>[ ]+", " greater than ") // ">" -> "greater than"
                .replaceAll("[ ]+<[ ]+", " less than ") // "<" -> "less than"
                .replaceAll("[ ]+>=[ ]+", " equal or greater than ") // ">=" -> "equal or greater than"
                .replaceAll("[ ]+<=[ ]+", " equal or less than ") // "<=" -> "equal or less than"
                .replaceAll("[-]+>", "---"); // "->" -> "---"

        return javadoc;
    }

    @VisibleForTesting
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

    @VisibleForTesting
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
