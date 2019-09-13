import java.io.File;
import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Directory location: ");
        String directoryLocation = scanner.nextLine();

        List<File> files = SourceFilesResolver.getFilesFromDirectory(directoryLocation);
        List<File> sourceFiles = Utils.filterFilesByExtension(files, "java");

        System.out.println("Files found: " + sourceFiles.size());
        System.out.println("------------------------------------");
        sourceFiles.forEach(System.out::println);
    }
}
