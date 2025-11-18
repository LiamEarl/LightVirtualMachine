package org.lightvm.compiler;
import java.io.*;
import java.util.*;

public class Compiler {

    public static final String filePath = "src/main/resources/code.txt";

    public static final Map<String, Integer> instructionFlags = new HashMap<>();

    public static int instructionPointer = 0;

    public static void main(String[] args) throws Exception {
        String byteCode = compile(filePath);
        saveToNewCompiledFile(byteCode, filePath);
    }

    private static String compile(String filePath) throws Exception {
        List<String> byteCode = new ArrayList<>();
        List<Integer> pointerAtEachLine = new ArrayList<>();
        List<String> originalLines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                int temp = instructionPointer;
                String byteCodeOf = getByteCodeOf(line);

                if(byteCodeOf.isBlank()) continue;

                byteCode.add(byteCodeOf);
                pointerAtEachLine.add(temp);
                if(!Objects.equals(line.split(" ")[0], "flag")) originalLines.add(line);
            }
        } catch (Exception e) {
            System.err.println("Error reading the file: " + e.getMessage());
        }

        StringBuilder completeCode = new StringBuilder();

        offsetInstructionFlags(byteCode, pointerAtEachLine);
        for(int i = 0; i < byteCode.size(); i++) {

            //completeCode.append(originalLines.get(i).split(" ")[0]).append(" Pointer: ").append(pointerAtEachLine.get(i)).append("\n");
            if(byteCode.get(i).startsWith("branch")) { // it's a branch
                byteCode.set(i, parseBranch(byteCode.get(i).split(" ")));
            }
            completeCode.append(byteCode.get(i));
        }

        return completeCode.toString();
    }


    private static String getByteCodeOf(String line) throws Exception {
        if(line == null || line.isBlank()) return "";
        String byteCode = "";
        String[] separatedLine = line.split(" ");
        //TODO find a better way to go about this
        if(separatedLine[0].equals("save")) {
            byteCode = parseSave(separatedLine);
        } else if(separatedLine[0].equals("load")) {
            byteCode = parseLoad(separatedLine);
        } else if(separatedLine[0].equals("add")) {
            byteCode = parseMathOp("0011", separatedLine);
        } else if(separatedLine[0].equals("subtract")) {
            byteCode = parseMathOp("0100", separatedLine);
        } else if(separatedLine[0].equals("multiply")) {
            byteCode = parseMathOp("0101", separatedLine);
        } else if(separatedLine[0].equals("divide")) {
            byteCode = parseMathOp("0110", separatedLine);
        } else if(separatedLine[0].equals("and")) {

        } else if(separatedLine[0].equals("or")) {

        } else if(separatedLine[0].equals("not")) {

        } else if(separatedLine[0].equals("branch")) {
            // don't process branch bytecode since the flags are not populated yet
            byteCode = line;
        }else if(separatedLine[0].equals("halt")) {
            byteCode = "11110000\n";
            instructionPointer += 1;
        }else if(separatedLine[0].equals("flag")) {
            instructionFlags.put(separatedLine[1], instructionPointer);
        }
        return byteCode;
    }

    private static String parseSave(String[] separatedLine) throws Exception {
        StringBuilder saveCommand = new StringBuilder();
        switch (separatedLine[1]) {
            case "r" -> {
                if (separatedLine[2].equals("m")) {
                    if (separatedLine[3].equals("b")) { // save r m b 1 12500
                        saveCommand.append("00010111\n"); // opcode
                        saveCommand.append(intToBinaryString(Integer.parseInt(separatedLine[4]), 1));
                        saveCommand.append(intToBinaryString(Integer.parseInt(separatedLine[5]), 2));
                    } else { // save r m 1 500
                        saveCommand.append("00010000\n"); // opcode
                        saveCommand.append(intToBinaryString(Integer.parseInt(separatedLine[3]), 1));
                        saveCommand.append(intToBinaryString(Integer.parseInt(separatedLine[4]), 2));
                    }
                    instructionPointer += 4;
                }
            }
            case "l" -> {
                if (separatedLine[2].equals("r")) {
                    if (!separatedLine[3].equals("b")) {// save l r 1 59392
                        saveCommand.append("00010001\n"); // opcode
                        saveCommand.append(intToBinaryString(Integer.parseInt(separatedLine[3]), 1));
                        saveCommand.append(intToBinaryString(Integer.parseInt(separatedLine[4]), 4));
                        instructionPointer += 6;
                    } else {// save l r b 1 56
                        saveCommand.append("00011000\n"); // opcode
                        saveCommand.append(intToBinaryString(Integer.parseInt(separatedLine[4]), 1));
                        saveCommand.append(intToBinaryString(Integer.parseInt(separatedLine[5]), 1));
                        instructionPointer += 3;
                    }
                } else if (separatedLine[2].equals("m")) {
                    if (separatedLine[3].equals("b")) {
                        if (separatedLine[4].equals("r")) { // save l m b r 2 234003
                            saveCommand.append("00011010\n");
                            saveCommand.append(intToBinaryString(Integer.parseInt(separatedLine[5]), 1));
                            saveCommand.append(intToBinaryString(Integer.parseInt(separatedLine[6]), 1));
                            instructionPointer += 3;
                        } else {
                            saveCommand.append("00011001\n");
                            saveCommand.append(intToBinaryString(Integer.parseInt(separatedLine[4]), 2));
                            saveCommand.append(intToBinaryString(Integer.parseInt(separatedLine[5]), 1));
                            instructionPointer += 4;
                        }
                    } else { // save l m 235 512000
                        saveCommand.append("00010010\n");
                        saveCommand.append(intToBinaryString(Integer.parseInt(separatedLine[3]), 2));
                        saveCommand.append(intToBinaryString(Integer.parseInt(separatedLine[4]), 4));
                        instructionPointer += 7;
                    }
                }
            }
            case "m" -> {
                switch (separatedLine[2]) {
                    case "v" -> {
                        if (separatedLine[3].equals("r")) { // save m v r 1(mem) 2(vmem) 3(numbytes)
                            saveCommand.append("00011011\n");
                            saveCommand.append(intToBinaryString(Integer.parseInt(separatedLine[4]), 1));
                            saveCommand.append(intToBinaryString(Integer.parseInt(separatedLine[5]), 1));
                            saveCommand.append(intToBinaryString(Integer.parseInt(separatedLine[6]), 1));
                            instructionPointer += 4;
                        } else {
                            saveCommand.append("00010011\n");
                            saveCommand.append(intToBinaryString(Integer.parseInt(separatedLine[3]), 2));
                            saveCommand.append(intToBinaryString(Integer.parseInt(separatedLine[4]), 2));
                            saveCommand.append(intToBinaryString(Integer.parseInt(separatedLine[4]), 2));
                            instructionPointer += 7;
                        }
                    }
                    case "p" -> {
                        if (separatedLine[3].equals("i")) {// save m p i 50 10
                            saveCommand.append("00010100\n");
                            saveCommand.append(intToBinaryString(Integer.parseInt(separatedLine[4]), 2));
                            saveCommand.append(intToBinaryString(Integer.parseInt(separatedLine[5]), 2));
                            instructionPointer += 5;
                        } else if (separatedLine[3].equals("c")) {// save m p c 50 10
                            saveCommand.append("00010101\n");
                            saveCommand.append(intToBinaryString(Integer.parseInt(separatedLine[4]), 2));
                            saveCommand.append(intToBinaryString(Integer.parseInt(separatedLine[5]), 2));
                            instructionPointer += 5;
                        }
                    }
                    case "d" -> {
                        saveCommand.append("00010110\n");
                        saveCommand.append(intToBinaryString(Integer.parseInt(separatedLine[3]), 2));
                        saveCommand.append(intToBinaryString(Integer.parseInt(separatedLine[4]), 2));
                        saveCommand.append(intToBinaryString(Integer.parseInt(separatedLine[5]), 2));
                        instructionPointer += 7;
                    }
                }
            }
        }
        if(saveCommand.toString().isBlank()) throw new Exception("Something went wrong parsing save at address: " + instructionPointer);
        return saveCommand.toString();
    }

    private static String parseLoad(String[] separatedLine) {
        StringBuilder loadCommand = new StringBuilder();

        if(separatedLine[1].equals("m") && separatedLine[2].equals("r")) { // load m r 5 1234
            if(separatedLine[3].equals("b")) {
                loadCommand.append("00100011\n");
                loadCommand.append(intToBinaryString(Integer.parseInt(separatedLine[4]), 1));
                loadCommand.append(intToBinaryString(Integer.parseInt(separatedLine[5]), 2));
                instructionPointer += 4;
            }else {
                loadCommand.append("00100000\n");
                loadCommand.append(intToBinaryString(Integer.parseInt(separatedLine[3]), 1));
                loadCommand.append(intToBinaryString(Integer.parseInt(separatedLine[4]), 2));
                instructionPointer += 4;
            }
        }else if(separatedLine[1].equals("d") && separatedLine[2].equals("m")) { // load d m (mem)1245 (disk) 142 (numBytes) 124
            loadCommand.append("00100001\n");
            loadCommand.append(intToBinaryString(Integer.parseInt(separatedLine[3]), 2));
            loadCommand.append(intToBinaryString(Integer.parseInt(separatedLine[4]), 2));
            loadCommand.append(intToBinaryString(Integer.parseInt(separatedLine[4]), 2));
            instructionPointer += 7;
        }else if(separatedLine[1].equals("k") && separatedLine[2].equals("m")) { // load k m 5201
            loadCommand.append("00100010\n");
            loadCommand.append(intToBinaryString(Integer.parseInt(separatedLine[3]), 2));
            instructionPointer += 3;
        }
        return loadCommand.toString();
    }

    private static void offsetInstructionFlags(List<String> byteCode, List<Integer> pointerAtEachLine) {
        Map<String, Integer> offsetsBranchType = Map.of(
            "u", 3,
            "c", 5,
            "r", 1
        );

        for(int i = 0; i < byteCode.size(); i++) {
            if(!byteCode.get(i).startsWith("branch")) continue;
            String[] splitL = byteCode.get(i).split(" ");

            for (Map.Entry<String, Integer> entry : instructionFlags.entrySet()) {
                String flagName = entry.getKey();
                Integer pointerAtFlag = entry.getValue();
                if(pointerAtEachLine.get(i) > pointerAtFlag) {
                    System.out.println(splitL[0] + " " + splitL[1] + " pointer branch: " + pointerAtEachLine.get(i) + " pointer flag " + pointerAtFlag + " no offset.");
                    continue;
                }
                System.out.println(splitL[0] + " " + splitL[1] + " pointer branch: " + pointerAtEachLine.get(i) + " pointer flag " + pointerAtFlag + ". Offsetting flag " + flagName + " by " + offsetsBranchType.get(splitL[1]) + " bytes.");
                instructionFlags.replace(flagName, pointerAtFlag + offsetsBranchType.get(splitL[1]));
            }

            for(int j = i+1; j < pointerAtEachLine.size(); j++) {
                pointerAtEachLine.set(j, pointerAtEachLine.get(j) + offsetsBranchType.get(splitL[1]));
            }
        }
    }

    private static String parseBranch(String[] separatedLine) throws Exception {
        StringBuilder branchCommand = new StringBuilder();

        if(separatedLine[1].equals("u")) { // branch u flagName
            String firstBit = "0";
            if(separatedLine.length > 3) {
                firstBit = separatedLine[3].equals("^") ? "1" : "0";
            }
            branchCommand.append("1010").append(firstBit).append("000\n");

            System.out.println("Flag " + separatedLine[2] + " Address :" + instructionFlags.get(separatedLine[2]));
            branchCommand.append(intToBinaryString(instructionFlags.get(separatedLine[2]), 2));
        } else if(separatedLine[1].equals("c")) {
            String firstBit = "0";
            if(separatedLine.length > 6) {
                firstBit = separatedLine[6].equals("^") ? "1" : "0";
            }
            switch (separatedLine[2]) {
                case "e" ->  // branch c e 5 2 flagName
                        branchCommand.append("1010").append(firstBit).append("001\n");
                case "g" ->  // branch c g 5 2 flagName
                        branchCommand.append("1010").append(firstBit).append("010\n");
                case "l" ->  // branch c l 5 2 flagName
                        branchCommand.append("1010").append(firstBit).append("011\n");
                default -> throw new Exception("Error at branch");
            }
            branchCommand.append(intToBinaryString(Integer.parseInt(separatedLine[3]), 1));
            branchCommand.append(intToBinaryString(Integer.parseInt(separatedLine[4]), 1));
            System.out.println("Flag " + separatedLine[5] + " Address :" + instructionFlags.get(separatedLine[5]));
            branchCommand.append(intToBinaryString(instructionFlags.get(separatedLine[5]), 2));
        } else if(separatedLine[1].equals("r")) { // branch r
            branchCommand.append("10100100\n");
        }else {
            throw new Exception("Error at branch");
        }

        return branchCommand.toString();
    }

    private static String parseMathOp(String opcode, String[] separatedLine) {
        StringBuilder addCommand = new StringBuilder();
        addCommand.append(opcode);
        addCommand.append(getMathOpBytes(separatedLine));
        return addCommand.toString();
    }

    private static String getMathOpBytes(String[] separatedLine) {
        Map<String, String> numberTypeToCode = Map.of(
                "r", "00",
                "mr", "01",
                "ma", "10",
                "l", "11"
        );
        Map<String, Integer> numberTypeNumBytes = Map.of(
                "r", 1,
                "mr", 2,
                "ma", 2,
                "l", 4
        );
        StringBuilder mathOp = new StringBuilder();

        mathOp.append(numberTypeToCode.get(separatedLine[1]));
        mathOp.append(numberTypeToCode.get(separatedLine[2])).append("\n");

        int numBytes1st = numberTypeNumBytes.get(separatedLine[1]);
        int numBytes2nd = numberTypeNumBytes.get(separatedLine[2]);

        mathOp.append(intToBinaryString( // first num
                Integer.parseInt(separatedLine[3]),
                numBytes1st
        ));
        mathOp.append(intToBinaryString( //second num
                Integer.parseInt(separatedLine[4]),
                numBytes2nd
        ));
        // register to save the value to
        mathOp.append(intToBinaryString(Integer.parseInt(separatedLine[5]),1));
        instructionPointer += 2 + numBytes1st + numBytes2nd;
        return mathOp.toString();
    }

    private static String intToBinaryString(int toConvert, int numBytes) {
        StringBuilder original = new StringBuilder(Integer.toBinaryString(toConvert));
        while(original.length() < numBytes * 8) {
            original.insert(0, "0");
        }
        for(int i = 8; i <= original.length(); i+= 8) {
            original.insert(i, "\n");
            i+= 1;
        }
        return original.toString();
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
        }
        writer.flush();
        fileWriter.close();
    }

}
