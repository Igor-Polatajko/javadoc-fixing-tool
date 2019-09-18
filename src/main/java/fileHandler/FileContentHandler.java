package fileHandler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
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
            fileContent.append(sc.nextLine()).append("\n");
        }

        return fileContent.toString();
    }

    public static void rewriteFile(File file, String newContent) {
        try {
            FileWriter writer = new FileWriter(file, false);
            writer.write(newContent);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
