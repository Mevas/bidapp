package services;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class CsvReaderService {
    private static CsvReaderService instance = null;

    private CsvReaderService() {
    }

    public static CsvReaderService getInstance() {
        if (instance == null) {
            instance = new CsvReaderService();
        }
        return instance;
    }

    public List<List<String>> read(String path) throws FileNotFoundException {
        Scanner scanner = new Scanner(new File(path));
        List<List<String>> data = new ArrayList<>();

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            List<String> items = Arrays.asList(line.split(","));
            data.add(items);
        }

        scanner.close();
        // Remove the csv header
        return data.subList(1, data.size() - 1);
    }
}