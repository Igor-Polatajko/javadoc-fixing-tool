package logic;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

public class Utils {

    public static List<File> filterFilesByExtension(List<File> files, String fileExtension) {
        return files.stream()
                .filter(file -> file.getName().matches(".*[.]" + fileExtension))
                .collect(Collectors.toList());
    }
}
