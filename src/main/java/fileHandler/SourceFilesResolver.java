package fileHandler;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SourceFilesResolver {

    public static List<File> getFilesFromDirectory(String directoryPath) {
        File root = new File(directoryPath);

        if (!root.exists()) {
            throw new IllegalArgumentException("Incorrect directory path provided");
        }

        if (!root.isDirectory()) {
            throw new IllegalArgumentException(directoryPath + " - is not a directory");
        }

        List<File> sourceFiles = new ArrayList<>(Arrays.asList(root.listFiles()));

        int iterationsCount = sourceFiles.size();
        for (int i = 0; i < iterationsCount; i++) {
            File file = sourceFiles.get(i);

            File[] innerFiles = file.listFiles();

            if (innerFiles != null) {
                sourceFiles.addAll(Arrays.asList(innerFiles));
                iterationsCount += innerFiles.length;
            }
        }

        return sourceFiles;
    }
}
