package service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class SortFileImplVerEffectiveRAM implements SortFile {
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

    private void splitFileAndSortLines(String pathSourceFile) throws IOException {
        List<String> result = new ArrayList<>(countLinesAvailableRAM);
        String bufLine;
        int countLine = 1;
        int countPart = 1;
        try (BufferedReader br = new BufferedReader(new FileReader(pathSourceFile))) {
            // создаём временную дерикторию
            Path pathTempSplitFilesDir = Paths.get(TEMP_SPLIT_FILES_DIR);
            Files.createDirectory(pathTempSplitFilesDir);
            String pathTempFile;
            // читаем исходный файл и делим на части размером countLinesAvailableRAM строк
            while ((bufLine = br.readLine()) != null) {
                result.add(bufLine);
                if (countLine < countLinesAvailableRAM) {
                    countLine++;
                } else { // после достижения размера countLinesAvailableRAM записываем фрагмент в файл
                    countLine = 1;
                    pathTempFile = TEMP_SPLIT_FILES_DIR + "/" + countPart + ".txt";
                    countPart++;
                    result.sort(String::compareTo);
                    Files.write(Paths.get(pathTempFile), result, StandardOpenOption.CREATE_NEW);
                    result.clear();
                }
            }
            if (!result.isEmpty()) {
                pathTempFile = TEMP_SPLIT_FILES_DIR + "/" + countPart + ".txt";
                result.sort(String::compareTo);
                Files.write(Paths.get(pathTempFile), result, StandardOpenOption.CREATE_NEW);
                result.clear();
            }
        }
    }

    private void sorting(String pathResultFile) throws IOException {
        File splitFilesDir = new File(TEMP_SPLIT_FILES_DIR);
        long countDir = 1;
        long countFile = 1;
        String pathDir = TEMP_SPLIT_FILES_DIR;
        String pathFile;

            while (splitFilesDir.listFiles().length > 1) {

                if (splitFilesDir.listFiles().length % 2 != 0) { // если нечетное количество файлов, то объединяем первые 2
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

                    if (arrayFile.length == 2) {
                        pathFile = pathResultFile; //если в папке 2 файла, то записываем в итоговый файл
                    } else pathFile = pathDir + "/" + countFile + ".txt";

                    merge(arrayFile[i], arrayFile[i + 1], pathFile);
                    countFile++;
                }
                countDir++;
                countFile = 1;

                deleteFolder(splitFilesDir); // удаляем отработанную папку
                splitFilesDir = new File(pathDir); // присваеваем путь к созданной папке для следующего цикла объединения
            }
    }

    private void merge(File fileSource1, File fileSource2, String pathFileResult) throws IOException {
        try (BufferedReader br1 = new BufferedReader(new FileReader(fileSource1));
             BufferedReader br2 = new BufferedReader(new FileReader(fileSource2))) {

            // создаём  файл с результатом слияния
            Path pathResult = Paths.get(pathFileResult);
            Files.deleteIfExists(pathResult);
            Files.createFile(pathResult);

            // создаём 2 списка, из которых будем брать строки для сравнения,
            // и один результирующий список
            LinkedList<String> listCompare1 = new LinkedList<>();
            LinkedList<String> listCompare2 = new LinkedList<>();
            LinkedList<String> listResult = new LinkedList<>();
            // строки чтения из файла источника и строк записи в итоговый файл
            String lineRead1 = null;
            String lineRead2 = null;
            String lineWrite1 = null;
            String lineWrite2 = null;
            // флаги указывающие нужно ли считывать следующую строку
            boolean updateLine1;
            boolean updateLine2;
            // флаг указывающий последнюю итерацию
            boolean isLastIteration = false;

            while (!isLastIteration) {
                // наполняем 2 списка до размера ограниченого значением countLinesAvailableRAM
                while (listCompare1.size() < countLinesAvailableRAM / 4 && (lineRead1 = br1.readLine()) != null) {
                    listCompare1.add(lineRead1);
                }
                while (listCompare2.size() < countLinesAvailableRAM / 4 && (lineRead2 = br2.readLine()) != null) {
                    listCompare2.add(lineRead2);
                }

                isLastIteration = (lineRead1 == null && listCompare1.isEmpty())
                        || (lineRead2 == null && listCompare2.isEmpty());

                updateLine1 = true;
                updateLine2 = true;

                while (!isLastIteration) { // цикл сравнения

                    if (updateLine1) {
                        if (listCompare1.isEmpty()) { // выходим из цикла, если список сравнения пустой
                            listCompare2.addFirst(lineWrite2); //возвращаем строку в список,т.к. выходим из цикла сравнения
                            break;
                        }
                        lineWrite1 = listCompare1.removeFirst(); // берём строку для сравнения из первого списка
                        updateLine1 = false;
                    }
                    if (updateLine2) {
                        if (listCompare2.isEmpty()) {
                            listCompare1.addFirst(lineWrite1);
                            break;
                        } else
                            lineWrite2 = listCompare2.removeFirst(); // берём строку для сравнения из второго списка
                        updateLine2 = false;
                    }

                    //  сравниваем и строку с меньшим значением, записываем в результирующий список.
                    //  Указываем, что её нужно будет обновить
                    if (lineWrite1.compareTo(lineWrite2) <= 0) {
                        listResult.add(lineWrite1);
                        updateLine1 = true;

                    } else {
                        listResult.add(lineWrite2);
                        updateLine2 = true;
                    }
                }
                // вышли из цикла сравнения, записываем результирующий список в итоговый файл
                Files.write(pathResult, listResult, StandardOpenOption.APPEND);
                listResult.clear();
                // если это последняя итерация, записываем всё, что не пустое в итоговый файл
                if (isLastIteration) {
                    if (!listCompare1.isEmpty()) {
                        Files.write(pathResult, listCompare1, StandardOpenOption.APPEND);
                        listCompare1.clear();
                    }
                    if (!listCompare2.isEmpty()) {
                        Files.write(pathResult, listCompare2, StandardOpenOption.APPEND);
                        listCompare2.clear();
                    }
                    if (lineRead1 != null) {
                        while ((lineRead1 = br1.readLine()) != null) {
                            listCompare1.add(lineRead1);
                        }
                        Files.write(pathResult, listCompare1, StandardOpenOption.APPEND);
                        listCompare1.clear();
                    } else {
                        while ((lineRead2 = br2.readLine()) != null) {
                            listCompare2.add(lineRead2);
                        }
                        Files.write(pathResult, listCompare2, StandardOpenOption.APPEND);
                        listCompare2.clear();
                    }
                }
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
            //если это директория
            if (f.isDirectory()) {
                //очищаем и удаляем папку
                deleteFolder(f);
            } else {
                //удаляем файл
                f.delete();
            }
        }
        //удаляем пустую папку
        return Objects.requireNonNull(folder.listFiles()).length == 0 && folder.delete();
    }
}
