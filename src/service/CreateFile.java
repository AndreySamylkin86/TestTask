package service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class CreateFile {

    public void createFile(String pathResultFile, String sourceChar, long countString, int countLinesAvailableRAM, int maxLengthString) throws IOException {
        createFileStringRandomChar(pathResultFile, sourceChar, countString, countLinesAvailableRAM, maxLengthString);
    }

    private void createFileStringRandomChar(String pathResultFile, String sourceChar, long countString, int countLinesAvailableRAM, int maxLengthString) throws IOException {
        List<String> stringList = new ArrayList<>(countLinesAvailableRAM);
        int lengthString, indexChar;
            for (long i = 0; i < countString; ) {
                for (int j = 0; j < countLinesAvailableRAM && i < countString; j++, i++) {
                    lengthString = (int) (maxLengthString * Math.random()) + 1;

                    StringBuilder sb = new StringBuilder(lengthString);
                    for (int c = 0; c < lengthString; c++) {
                        indexChar = (int) (sourceChar.length() * Math.random());
                        sb.append(sourceChar.charAt(indexChar));
                    }
                    stringList.add(sb.toString());
                }
                writeFile(pathResultFile, stringList);
            }
    }

    private void writeFile(String pathFile, List<String> stringList) throws IOException {
        Path path = Paths.get(pathFile);

        Files.deleteIfExists(path);
        Files.createFile(path);
        Files.write(path, stringList, StandardOpenOption.WRITE);

    }
}