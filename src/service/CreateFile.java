package service;

import java.io.IOException;

public interface CreateFile {
    void createFile(String pathResultFile, String sourceChar, int countString, int countLinesAvailableRAM,
                    int maxLengthString) throws IOException;
}
