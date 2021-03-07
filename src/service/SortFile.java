package service;

import java.io.IOException;

public interface SortFile {

    void sort(String pathSourceFile, String pathResultFile, int countLinesAvailableRAM) throws IOException;
}
