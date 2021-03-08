import service.*;

import java.io.IOException;

public class TestStart {
    private final static int COUNT_LINES_SOURCE = 1_000_000_000;
    private final static int COUNT_LINES_AVAILABLE_RAM = 10_000_000;
    private final static int MAX_LENGTH_STRING = 20;
    private final static String PATH_SOURCE_FILE = "source.txt";
    private final static String PATH_RESULT_FILE = "result.txt";
    private final static String SOURCE_CHAR = "0123456789"
            + "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
            + "abcdefghijklmnopqrstuvxyz";

    public static void main(String[] args) throws IOException {
        generateFileSource();
        testEffectiveRAM();
//        testSaveRAM();
    }

    private static void generateFileSource() throws IOException {
        long l1 = System.currentTimeMillis();
        CreateFile cF = new CreateFileImpl();
        cF.createFile(PATH_SOURCE_FILE, SOURCE_CHAR, COUNT_LINES_SOURCE,
                COUNT_LINES_AVAILABLE_RAM, MAX_LENGTH_STRING);
        long l2 = System.currentTimeMillis();
        System.out.println("Время создания файла: " + (l2 - l1) + " мсек");
    }

    private static void testEffectiveRAM() {
        long l1 = System.currentTimeMillis();
        SortFile sortFileEffectiveRAM = new SortFileImplVerEffectiveRAM();
        try {
            sortFileEffectiveRAM.sort(PATH_SOURCE_FILE, PATH_RESULT_FILE, COUNT_LINES_AVAILABLE_RAM);
        } catch (IOException e) {
            System.err.println("Не удалось осортировать файл с помощью метода sort класса SortFileImplVerEffectiveRAM ");
            e.printStackTrace();
        }
        long l2 = System.currentTimeMillis();
        System.out.println("Время сортировки с эффективным использованием доступной RAM:  " + (l2 - l1) + " мсек");
    }

    private static void testSaveRAM() {
        long l1 = System.currentTimeMillis();
        SortFile sortFileSaveRAM = new SortFileImplVerSaveRAM();
        try {
            sortFileSaveRAM.sort(PATH_SOURCE_FILE, PATH_RESULT_FILE, COUNT_LINES_AVAILABLE_RAM);
        } catch (IOException e) {
            System.err.println("Не удалось осортировать файл с помощью метода sort класса SortFileImplVerSaveRAM ");
            e.printStackTrace();
        }
        long l2 = System.currentTimeMillis();
        System.out.println("Время сортировки с минимальным использованием доступной RAM:  " + (l2 - l1) + " мсек");
    }
}
