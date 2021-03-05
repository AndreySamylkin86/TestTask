package task;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class GenerateFile {
    private final static int COUNT_LINES = 200_000;
    private final static int MAX_LENGTH_STRING = 20;
    private final static String PATH_RESULT_FILE = "source.txt";
    private final static String SOURCE_CHAR = "0123456789"
            + "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
            + "abcdefghijklmnopqrstuvxyz";

    public static void main(String[] args) throws IOException {
        long l1 = System.currentTimeMillis();

        List<String> stringList = getListStringRandomChar(SOURCE_CHAR, COUNT_LINES, MAX_LENGTH_STRING);
        writeFile(PATH_RESULT_FILE, stringList);


        long l2 = System.currentTimeMillis();
        System.out.println(l2 - l1);
    }

    public static List getListStringRandomChar(String sourceChar, int countString, int maxLengthString) {
        List<String> stringList = new ArrayList<>(countString);
        int lengthString, index;

        for (int i = 0; i < countString; i++) {
            lengthString = (int) (maxLengthString * Math.random()) + 1;

            StringBuilder sb = new StringBuilder(lengthString);
            for (int j = 0; j < lengthString; j++) {
                index = (int) (sourceChar.length() * Math.random());
                sb.append(sourceChar.charAt(index));
            }
            stringList.add(sb.toString());
        }
        return stringList;
    }

    public static void writeFile(String pathFile, List<String> stringList) throws IOException {
        Path path = Paths.get(pathFile);

        Files.deleteIfExists(path);
        Files.createFile(path);

        Files.write(path, stringList, StandardOpenOption.WRITE);
    }
}


