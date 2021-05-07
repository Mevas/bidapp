package services;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class AuditService {
    private static AuditService instance = null;
    private final CsvWriterService csvWriterService;

    private AuditService() {
        this.csvWriterService = CsvWriterService.getInstance();
    }

    public static AuditService getInstance() {
        if (instance == null) {
            instance = new AuditService();
        }
        return instance;
    }

    public void log(String eventName) {
        List<String> toWrite = new ArrayList<>();
        toWrite.add(eventName);
        toWrite.add(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss").format(LocalDateTime.now()));

        try {
            csvWriterService.write("data\\audit.csv", toWrite, true);
        } catch (IOException e) {
            System.out.println("Failed to write to csv.");
        }
    }
}