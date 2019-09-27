package logic;

import entity.DescribedEntity;
import entity.MethodDescription;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static logic.ParserUtils.completeGenerics;
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

        List<String> beforeParams = completeGenerics(beforeParamsMatcher.group().trim().split(" "));
        List<String> params = completeGenerics(paramsMatcher.group().trim().split(", "));

        if (afterParamsMatcher.find()) {
            String[] afterParams = afterParamsMatcher.group().replaceAll("\\)|[,]", " ").trim().split("\\s");

            List<String> afterParamsFiltered = Arrays.stream(afterParams).map(String::trim).filter(s -> !s.equals(""))
                    .collect(Collectors.toList());

            if (!afterParamsFiltered.isEmpty() && afterParamsFiltered.get(0).contains("throws")) {
                methodDescription.setExceptionsThrown(afterParamsFiltered.subList(1, afterParamsFiltered.size()));
            } else {
                methodDescription.setExceptionsThrown(Collections.emptyList());
            }
        }

        methodDescription.setReturnType(beforeParams.get(beforeParams.size() - 2));

        if (params.size() > 0) {
            params.set(0, params.get(0).replaceAll("\\(", ""));
            params.set(params.size() - 1, params.get(params.size() - 1).replaceAll("\\)", ""));
        }

        params = params.stream().map(ParserUtils::skipJavaAnnotations)
                .filter(p -> !p.equals("")).collect(Collectors.toList());

        methodDescription.setParams(params);

        return methodDescription;
    }

    static DescribedEntity getDescribedEntity(int javadocEndIndex, String fileContent) {
        DescribedEntity describedEntity = new DescribedEntity();
        String methodRegex = "[^.]*?[A-Za-z0-9_<>\\[\\]]+?\\s+?[a-z][A-Za-z0-9_<>]*?[(][^.]*?[)][^.]*";
        String fieldRegex = "[^.]*?[A-Za-z0-9_<>]+?\\s+?\\w+?";

        int indexOfNextCurlyBracket = fileContent.indexOf("{", javadocEndIndex + 1);
        int indexOfNextSemicolon = fileContent.indexOf(";", javadocEndIndex + 1);

        if (indexOfNextCurlyBracket < 0 && indexOfNextSemicolon < 0) {
            describedEntity.setPresent(false);
            return describedEntity;
        }

        describedEntity.setPresent(true);

        String data =
                (indexOfNextCurlyBracket > indexOfNextSemicolon || indexOfNextCurlyBracket < 0) && indexOfNextSemicolon > 0
                        ? fileContent.substring(javadocEndIndex, indexOfNextSemicolon)
                        : fileContent.substring(javadocEndIndex, indexOfNextCurlyBracket);

        describedEntity.setData(skipNewLines(skipJavaAnnotations(data)));

        if (describedEntity.getData().matches(methodRegex)) {
            describedEntity.setType(DescribedEntity.Type.METHOD);
        } else if (describedEntity.getData().matches("(?i)" + methodRegex)) {
            describedEntity.setType(DescribedEntity.Type.CONSTRUCTOR);
        } else if (describedEntity.getData().matches(fieldRegex)) {
            describedEntity.setType(DescribedEntity.Type.FIELD);
        } else if (describedEntity.getData().contains(" class ")) {
            describedEntity.setType(DescribedEntity.Type.CLASS);
        } else if (describedEntity.getData().contains(" interface ")) {
            describedEntity.setType(DescribedEntity.Type.INTERFACE);
        } else {
            describedEntity.setType(DescribedEntity.Type.ANOTHER);
        }

        return describedEntity;
    }
}