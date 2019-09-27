package logic;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParserUtils {

    static boolean insideBlock(String javadoc, String blockType, int index) {
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

    static boolean insideTag(String javadoc, String tag, int index) {
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

    static boolean afterWordInOneLine(String javadoc, String word, int index) {
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

    static String eliminateSpecialChars(String regex) {
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

    static boolean noGenerics(String generics) {
        return generics.matches(".*</.+?>.*");
    }

    static String skipJavaAnnotations(String data) {
        data = data.replaceAll("[@][A-Za-z0-9].*?[(].*?[)]\\s", "");
        data = data.replaceAll("[@][A-Za-z0-9].*?\\s", "");
        return data;
    }

    static String skipNewLines(String data) {
        return data.replaceAll("[\\n]", "");
    }

    static List<String> getStatements(String javadoc, String statement) {
        List<String> statementsList = new ArrayList<>();
        Pattern pattern = Pattern.compile("[@]" + statement + "\\s[A-Za-z0-9].*");
        Matcher matcher = pattern.matcher(javadoc);

        while (matcher.find()) {
            statementsList.add(matcher.group());
        }

        return statementsList;
    }

    static List<String> completeGenerics(String[] input) {
        List<String> result = new ArrayList<>();

        for (int i = 0; i < input.length; i++) {
            int wordBracketsBalance = 0;
            for (int j = 0; j < input[i].length(); j++) {
                if (input[i].charAt(j) == '<') {
                    wordBracketsBalance++;
                }
                if (input[i].charAt(j) == '>') {
                    wordBracketsBalance--;
                }
            }

            if (wordBracketsBalance > 0 && i < input.length - 1) {
                input[i + 1] = input[i] +
                        (input[i].charAt(input[i].length() - 1) == ',' ? " " : ", ") + input[i + 1];
                continue;
            }

            result.add(input[i]);
        }

        return result;
    }

}
