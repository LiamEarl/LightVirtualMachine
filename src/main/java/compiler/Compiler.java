package compiler;
import java.io.*;
import java.util.Scanner;

// UNFINISHED

public class Compiler {

    public static final String filePath = "src/main/resources/code.txt";

    public static void main(String[] args) throws IOException {
        String byteCode = compile(filePath);
        saveToNewCompiledFile(byteCode, filePath);
    }

    private static String compile(String filePath) {
        StringBuilder byteCode = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                byteCode.append(line).append("\n");
            }
        } catch (IOException e) {
            System.err.println("Error reading the file: " + e.getMessage());
        }
        return byteCode.toString();
    }

    private static void saveToNewCompiledFile(String byteCode, String originalFilePath) throws IOException {
        String[] splitPath = originalFilePath.split("/");
        String originalFileDetails = splitPath[splitPath.length - 1];
        String fileExtension = originalFileDetails.split("\\.")[1];
        String originalFileName = originalFileDetails.split("\\.")[0];

        StringBuilder compiledCodePath = new StringBuilder();
        for(int i = 0; i < splitPath.length - 1; i++) {
            compiledCodePath.append(splitPath[i]).append("/");
        }
        compiledCodePath.append(originalFileName).append("_c.").append(fileExtension);

        FileWriter fileWriter = new FileWriter(compiledCodePath.toString());
        BufferedWriter writer = new BufferedWriter(fileWriter);

        Scanner scanner = new Scanner(byteCode);
        while(scanner.hasNextLine()) {
            String line = scanner.nextLine();
            writer.write(line + "\n");
            System.out.println(line);
        }
        writer.flush();
        fileWriter.close();
    }
}
