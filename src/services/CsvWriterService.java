package services;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class CsvWriterService {
    private static CsvWriterService instance = null;

    private CsvWriterService() {
    }

    public static CsvWriterService getInstance() {
        if (instance == null) {
            instance = new CsvWriterService();
        }
        return instance;
    }

    public void wipe(String path) throws IOException {
        new FileWriter(path, false).close();
    }

    public void write(String path, List<String> data, boolean append) throws IOException {
        FileWriter fileWriter = new FileWriter(path, append);

        String toWrite = String.join(",", data);
        fileWriter.write(toWrite + "\n");

        fileWriter.flush();
        fileWriter.close();
    }
}