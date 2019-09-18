package logic;

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
}
