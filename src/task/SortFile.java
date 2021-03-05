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

    private final static int COUNT_LINES_PART = 1000;
    private final static String PATH_SOURCE_FILE = "source.txt";
    private final static String PATH_RESULT_FILE = "result.txt";
    private final static String TEMP_FILES_DIR = "temp";
    private final static String TEMP_SPLIT_FILES_DIR = TEMP_FILES_DIR + "/temp";


    public static void main(String[] args) throws IOException {
        long l1 = System.currentTimeMillis();


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
        // сортируем строки записываем в результирующий файл
        sort(new File(TEMP_SPLIT_FILES_DIR));

        deleteFolder(dir);

        long l2 = System.currentTimeMillis();
        System.out.println(l2 - l1);
    }

    private static int splitFileAndSortLines(String fileName) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        List<String> result = new ArrayList<>(COUNT_LINES_PART);
        String line;
        int i = 1;
        int countPart = 1;
        String pathDir =TEMP_SPLIT_FILES_DIR;
        Path pathDirectory = Paths.get(pathDir);
        Files.createDirectory(pathDirectory);
        String pathFile = pathDir + "/" + countPart + ".txt";
        // читаем исходный файл и записываем частями в количестве COUNT_LINES_PART строк во временные файлы
        while ((line = br.readLine()) != null) {
            result.add(line);
            if (i < COUNT_LINES_PART) {
                i++;
            } else {
                i = 1;
                pathFile = pathDir + "/" + countPart + ".txt";
                countPart++;
                result.sort(String::compareTo);
                writeFile(pathFile, result);
                result.clear();
            }
        }

        result.sort(String::compareTo);
        Path path = Paths.get(pathFile);
        Files.write(path, result, StandardOpenOption.APPEND);
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

    private static void sort(File dir) throws IOException {

        long countDir = 1;
        long countFile = 1;
        String pathDir = TEMP_SPLIT_FILES_DIR;

        String pathFile;


        while (dir.listFiles().length > 1) {

            if (dir.listFiles().length % 2 != 0) {
                File[] arrayFile = dir.listFiles();
                pathFile = pathDir + "/" + "merge.txt";
                merge(arrayFile[0], arrayFile[1], pathFile);
                arrayFile[0].delete();
                arrayFile[1].delete();
            }


            File[] arrayFile = dir.listFiles();

            pathDir = TEMP_SPLIT_FILES_DIR + countDir;
            Path path = Paths.get(pathDir);
            Files.createDirectory(path);
            for (int i = 0; i < arrayFile.length; i += 2) {
                if (arrayFile.length != 2) {
                    pathFile = pathDir + "/" + countFile + ".txt";
                } else pathFile = PATH_RESULT_FILE;

                merge(arrayFile[i], arrayFile[i + 1], pathFile);
                countFile++;
            }
            countDir++;
            countFile = 1;
            deleteFolder(dir);
            dir = new File(pathDir);
        }


    }

    private static void merge(File file1, File file2, String pathFile) throws IOException {
        Path pathResult = Paths.get(pathFile);
        Files.deleteIfExists(pathResult);
        Files.createFile(pathResult);
        RandomAccessFile raf1 = new RandomAccessFile(file1, "r");
        RandomAccessFile raf2 = new RandomAccessFile(file2, "r");

        long pointer1 = raf1.getFilePointer();
        long pointer2 = raf2.getFilePointer();

        String line1 = raf1.readLine();
        String line2 = raf2.readLine();
        boolean flag1 = true;
        boolean flag2 = true;

        while (flag1 && flag2) {
            if (pointer1 == raf1.getFilePointer()) {
                if ((line1 = raf1.readLine()) == null) {
                    Files.writeString(pathResult, (line2 + "\n"), StandardOpenOption.APPEND);
                    flag1 = false;
                    continue;
                }
            }
            if (pointer2 == raf2.getFilePointer()) {
                if ((line2 = raf2.readLine()) == null) {
                    Files.writeString(pathResult, (line1 + "\n"), StandardOpenOption.APPEND);
                    flag2 = false;
                    continue;
                }
            }


            if (line1.compareTo(line2) <= 0) {
                Files.writeString(pathResult, (line1 + "\n"), StandardOpenOption.APPEND);
                pointer1 = raf1.getFilePointer();
            } else {
                Files.writeString(pathResult, (line2 + "\n"), StandardOpenOption.APPEND);
                pointer2 = raf2.getFilePointer();
            }
        }
        while ((line1 = raf1.readLine()) != null) {
            Files.writeString(pathResult, (line1 + "\n"), StandardOpenOption.APPEND);
        }
        while ((line2 = raf2.readLine()) != null) {
            Files.writeString(pathResult, (line2 + "\n"), StandardOpenOption.APPEND);
        }
        raf1.close();
        raf2.close();
    }
}
