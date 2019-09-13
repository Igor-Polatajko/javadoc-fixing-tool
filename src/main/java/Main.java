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
        System.out.println("------------------------------------");

        fixFiles(sourceFiles);
    }

    private static void fixFiles(List<File> sourceFiles) {
        for (int i = 0; i < sourceFiles.size(); i++) {
            JavadocFixingHandler.fix(sourceFiles.get(i));
            drawProgressBar(i, sourceFiles.size());
        }
    }

    private static void drawProgressBar(int fileNumber, int filesCount) {
        int progressPercent = (int) Math.round((((double) fileNumber / filesCount) * 100));
        int progressBarLength = 40;
        boolean bracketNotShown = true;

        System.out.print("\r" + progressPercent + "%   [");

        for (int i = 0; i < progressBarLength; i++) {
            if (i < ((double) progressBarLength / 100) * progressPercent) {
                System.out.print("=");
                continue;
            }

            if (bracketNotShown) {
                System.out.print(">");
                bracketNotShown = false;
                continue;
            }

            System.out.print(" ");
        }

        System.out.print("]  ");
    }

}
