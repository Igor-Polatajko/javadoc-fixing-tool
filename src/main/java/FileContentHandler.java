import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class FileContentHandler {

    public static String getFileContent(File file) {
        Scanner sc = null;
        try {
            sc = new Scanner(file);
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("File not found");
        }

        StringBuilder fileContent = new StringBuilder();
        while (sc.hasNextLine()) {
            fileContent.append(sc.nextLine()).append("\n\r");
        }

        return fileContent.toString();
    }
}
