package task;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SortFile {

    private final static int COUNT_LINES_PART = 1_0;
    private final static String PATH_SOURCE_FILE = "source.txt";
    private final static String PATH_RESULT_FILE = "result.txt";
    private final static String TEMP_FILES_DIR = "temp";


    public static void main(String[] args) throws IOException {
        long l1 = System.currentTimeMillis();
        // создаём файл с результатом сортировки
        Path pathResultFile = Paths.get(PATH_RESULT_FILE);
        Files.deleteIfExists(pathResultFile);
        Files.createFile(pathResultFile);


        // создаём временную дерикторию
        File dir = new File(TEMP_FILES_DIR);
        //если временная директория уже существует(не пустая)
        if (dir.exists()) {
            //то предварительно удаляем
            deleteFolder(dir);
        }
        Path path = Paths.get(TEMP_FILES_DIR);
        Files.createDirectory(path);
        //делим исходный файл на части и сортируем содержимого каждого файла по возрастанию
        splitFileAndSortLines(PATH_SOURCE_FILE);
        // сортируем строки из каждого файла и записываем в результирующий файл
        resultSortAndWriteFile(dir);

        long l2 = System.currentTimeMillis();
        System.out.println(l2 - l1);
    }

    private static int splitFileAndSortLines(String fileName) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        List<String> result = new ArrayList<>(COUNT_LINES_PART);
        String line;
        int i = 1;
        int countPart = 1;
        String pathFile;
        // читаем исходный файл и записываем частями в количестве COUNT_LINES_PART строк во временные файлы
        while ((line = br.readLine()) != null) {
            result.add(line);
            if (i < COUNT_LINES_PART) {
                i++;
            } else {
                i = 1;
                pathFile = TEMP_FILES_DIR + "/" + countPart + ".txt";
                countPart++;
                result.sort(String::compareTo);
                writeFile(pathFile, result);
                result.clear();
            }
        }
        pathFile = TEMP_FILES_DIR + "/" + countPart + ".txt";
        result.sort(String::compareTo);
        writeFile(pathFile, result);
        result.clear();
        br.close();
        return countPart;
    }

    public static void writeFile(String pathFile, List<String> stringList) throws IOException {
        Path path = Paths.get(pathFile);

        Files.deleteIfExists(path);
        Files.createFile(path);

        Files.write(path, stringList, StandardOpenOption.WRITE);
    }

    public static void writeFileResult(String pathFile, List<String> stringList) throws IOException {
        Path path = Paths.get(pathFile);
        Files.write(path, stringList, StandardOpenOption.APPEND);
    }

    private static void resultSortAndWriteFile(File folder) throws IOException {
        List<BufferedReader> readerList = new ArrayList<>();
        List<String> tempList = new ArrayList<>();
        // создаём лист BufferedReader
        for (File f : Objects.requireNonNull(folder.listFiles())) {
            readerList.add(new BufferedReader(new FileReader(f)));
        }
        String line;
        // считываем одну строку из каждого файла, добавляем в tempList,
        // сортируем его и записываем в итоговый файл
        for (int i = 0; i < COUNT_LINES_PART + 1; i++) {

            for (int j = 0; j < readerList.size(); j++) {
                BufferedReader br = readerList.get(j);
                if ((line = br.readLine()) != null) {
                    tempList.add(line);
                }
            }
            tempList.sort(String::compareTo);
            writeFileResult(PATH_RESULT_FILE, tempList);
            tempList.clear();
        }
        // закрываем BufferedReader
        for (int j = 0; j < readerList.size(); j++) {
            BufferedReader br = readerList.get(j);
            br.close();
        }
    }

    private static boolean deleteFolder(File folder) {
        //если папка недоступна, выходим с false
        if (folder.listFiles() == null) {
            System.out.println("Папка не доступна");
            return false;
        }
        //в цикле листаем временную папку и удаляем все файлы-фрагменты
        for (File f : Objects.requireNonNull(folder.listFiles())) {
            //если это директория (на всякий случай)
            if (f.isDirectory()) {
                //очищаем и удаляем папку
                deleteFolder(f);
            } else {
                //удаляем файл
                f.delete();
            }
        }
        //теперь можем удалить пустую папку
        return Objects.requireNonNull(folder.listFiles()).length == 0 && folder.delete();
    }
}
