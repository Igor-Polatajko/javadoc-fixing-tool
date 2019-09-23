package logic;

import entity.DescribedEntity;
import entity.MethodDescription;

import java.util.Arrays;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static logic.ParserUtils.skipJavaAnnotations;
import static logic.ParserUtils.skipNewLines;

public class EntityParser {
    static MethodDescription getMethodDescription(DescribedEntity describedEntity) {
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
            } else {
                methodDescription.setExceptionsThrown(Collections.emptyList());
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

    static DescribedEntity getDescribedEntity(int javadocEndIndex, String fileContent) {
        DescribedEntity describedEntity = new DescribedEntity();
        String methodRegex = "[^.]*?[A-Za-z0-9_<>]+?\\s+?\\w+?[(][^.]*?[)][^.]*";
        String fieldRegex = "[^.]*?[A-Za-z0-9_<>]+?\\s+?\\w+?";

        int indexOfNextCurlyBracket = fileContent.indexOf("{", javadocEndIndex + 1);
        int indexOfNextSemicolon = fileContent.indexOf(";", javadocEndIndex + 1);

        if (indexOfNextCurlyBracket < 0 && indexOfNextSemicolon < 0) {
            describedEntity.setPresent(false);
            return describedEntity;
        }

        describedEntity.setPresent(true);

        if ((indexOfNextCurlyBracket > indexOfNextSemicolon || indexOfNextCurlyBracket < 0) && indexOfNextSemicolon > 0) {
            describedEntity.setData(skipNewLines
                    (skipJavaAnnotations(fileContent.substring(javadocEndIndex, indexOfNextSemicolon))));

            if (describedEntity.getData().matches(methodRegex)) {
                describedEntity.setType(DescribedEntity.Type.METHOD);
            } else if (describedEntity.getData().matches(fieldRegex)) {
                describedEntity.setType(DescribedEntity.Type.FIELD);
            } else {
                describedEntity.setType(DescribedEntity.Type.ANOTHER);
            }

            return describedEntity;
        }

        describedEntity.setData(
                skipNewLines(
                        skipJavaAnnotations(fileContent.substring(javadocEndIndex, indexOfNextCurlyBracket))));

        if (describedEntity.getData().contains(" class ")) {
            describedEntity.setType(DescribedEntity.Type.CLASS);
        } else if (describedEntity.getData().contains(" interface ")) {
            describedEntity.setType(DescribedEntity.Type.INTERFACE);
        } else if (describedEntity.getData().matches(methodRegex)) {
            describedEntity.setType(DescribedEntity.Type.METHOD);
        } else {
            describedEntity.setType(DescribedEntity.Type.ANOTHER);
        }

        return describedEntity;
    }

}
