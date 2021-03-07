package service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SortFileImplVerSaveRAM implements SortFile {

    private int countLinesAvailableRAM;
    private final String TEMP_FILES_DIR = "temp";
    private final String TEMP_SPLIT_FILES_DIR = TEMP_FILES_DIR + "/temp";


    @Override
    public void sort(String pathSourceFile, String pathResultFile, int countLinesAvailableRAM) throws IOException {
        this.countLinesAvailableRAM = countLinesAvailableRAM;
        // создаём временную дерикторию
        File tempDir = initTempDir();
        //делим исходный файл на части и сортируем содержимого каждого файла по возрастанию
        splitFileAndSortLines(pathSourceFile);
        // сортируем файлы-фрагменты и записываем, по окончанию, в итоговый файл
        sorting(pathResultFile);
        // удаляем временную дерикторию
        deleteFolder(tempDir);
    }

    private File initTempDir() throws IOException {
        // создаём File временной дериктории
        File tempDir = new File(TEMP_FILES_DIR);
        //удаляем,если временная директория уже существует(возможно не пустая)
        if (tempDir.exists()) {
            deleteFolder(tempDir);
        }
        // создаём временную дерикторию
        Path path = Paths.get(TEMP_FILES_DIR);
        Files.createDirectory(path);
        return tempDir;
    }

    private void splitFileAndSortLines(String pathSourceFile) {
        List<String> result = new ArrayList<>(countLinesAvailableRAM);
        String line;
        int i = 1;
        int countPart = 1;
        String pathDir = TEMP_SPLIT_FILES_DIR;

        try (BufferedReader br = new BufferedReader(new FileReader(pathSourceFile))) {

            Path pathDirectory = Paths.get(pathDir);
            Files.createDirectory(pathDirectory);
            String pathTempFile;
            // читаем исходный файл и записываем частями в количестве countLinesAvailableRAM строк во временные файлы
            while ((line = br.readLine()) != null) {
                result.add(line);
                if (i < countLinesAvailableRAM) {
                    i++;
                } else {
                    i = 1;
                    pathTempFile = pathDir + "/" + countPart + ".txt";
                    countPart++;
                    result.sort(String::compareTo);
                    Files.write(Paths.get(pathTempFile), result, StandardOpenOption.CREATE_NEW);
                    result.clear();
                }
            }

            if (!result.isEmpty()) {
                pathTempFile = pathDir + "/" + countPart + ".txt";
                result.sort(String::compareTo);
                Files.write(Paths.get(pathTempFile), result, StandardOpenOption.CREATE_NEW);
                result.clear();
            }
        } catch (IOException e) {
            System.err.println("Не удалось разделить файл на части");
            e.printStackTrace();
        }
    }

    private void sorting(String pathResultFile) throws IOException {
        File splitFilesDir = new File(TEMP_SPLIT_FILES_DIR);
        long countDir = 1;
        long countFile = 1;
        String pathDir = TEMP_SPLIT_FILES_DIR;
        String pathFile;

        while (splitFilesDir.listFiles().length > 1) {

            if (splitFilesDir.listFiles().length % 2 != 0) { //если в папке 2 файла, то записываем в итоговый файл
                File[] arrayFile = splitFilesDir.listFiles();
                pathFile = pathDir + "/" + "merge.txt";
                merge(arrayFile[0], arrayFile[1], pathFile);
                arrayFile[0].delete();
                arrayFile[1].delete();
            }

            // создаём новую папку для хранения объединенных файлов
            pathDir = TEMP_SPLIT_FILES_DIR + countDir;
            Path path = Paths.get(pathDir);
            Files.createDirectory(path);

            // попарно объединяем файлы
            File[] arrayFile = splitFilesDir.listFiles();
            for (int i = 0; i < arrayFile.length; i += 2) {

                if (arrayFile.length == 2) { //если в папке 2 файла, то записываем в итоговый файл
                    pathFile = pathResultFile;
                } else pathFile = pathDir + "/" + countFile + ".txt";

                merge(arrayFile[i], arrayFile[i + 1], pathFile);
                countFile++;
            }
            countDir++;
            countFile = 1;
            deleteFolder(splitFilesDir);
            splitFilesDir = new File(pathDir);
        }
    }

    private static void merge(File fileSource1, File fileSource2, String pathFileResult) throws IOException {
        try (RandomAccessFile raf1 = new RandomAccessFile(fileSource1, "r");
             RandomAccessFile raf2 = new RandomAccessFile(fileSource2, "r")) {

            Path pathResult = Paths.get(pathFileResult);
            Files.deleteIfExists(pathResult);
            Files.createFile(pathResult);

            long pointer1 = raf1.getFilePointer();
            long pointer2 = raf2.getFilePointer();

            String line1 = raf1.readLine();
            String line2 = raf2.readLine();
            boolean flag1 = true;
            boolean flag2 = true;
            // в цикле читаем по 1 строке из fileSource1 и fileSource2 и записываем в FileResult
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
            // если остались не считанные строки, дописываем их в итоговый файл
            while ((line1 = raf1.readLine()) != null) {
                Files.writeString(pathResult, (line1 + "\n"), StandardOpenOption.APPEND);
            }
            while ((line2 = raf2.readLine()) != null) {
                Files.writeString(pathResult, (line2 + "\n"), StandardOpenOption.APPEND);
            }
        }
    }

    private boolean deleteFolder(File folder) {
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
