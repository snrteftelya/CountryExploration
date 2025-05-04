package org.example.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import org.example.exception.LogsException;
import org.example.exception.ObjectNotFoundException;
import org.example.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class LogProcessingService {

    private static final Logger logger = LoggerFactory.getLogger(LogProcessingService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter
            .ofPattern("yyyy-MM-dd");
    private static final Path LOG_PATH = Paths.get("logs/application.log");
    private static final Path LOGS_DIR = Paths.get("logs");

    private final Map<String, LogTaskStatus> taskStatusMap = new ConcurrentHashMap<>();

    public static class LogTaskStatus {
        private final String status;
        private final String filePath;
        private final String errorMessage;

        public LogTaskStatus(String status, String filePath, String errorMessage) {
            this.status = status;
            this.filePath = filePath;
            this.errorMessage = errorMessage;
        }

        public String getStatus() {
            return status;
        }

        public String getFilePath() {
            return filePath;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }

    public String startLogFileCreation(String date) {
        String taskId = UUID.randomUUID().toString();
        taskStatusMap.put(taskId, new LogTaskStatus("PENDING", null, null));
        logger.info("Starting log file creation for date: {}, taskId: {}", date, taskId);

        CompletableFuture.runAsync(() -> {
            try {
                String filteredLogs = getFilteredLogs(date);
                Path logFile = LOGS_DIR.resolve(date + "_" + taskId + ".log");
                logger.debug("Writing logs to file: {}", logFile);
                Files.createDirectories(LOGS_DIR);
                Files.write(logFile, filteredLogs.getBytes(StandardCharsets.UTF_8),
                        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                logger.info("Log file created successfully: {}", logFile);
                taskStatusMap.put(taskId, new LogTaskStatus("COMPLETED", logFile.toString(), null));
            } catch (Exception e) {
                logger.error("Failed to create log file for date: {}, taskId: {}", date, taskId, e);
                taskStatusMap.put(taskId, new LogTaskStatus("FAILED", null, e.getMessage()));
            }
        });

        return taskId;
    }

    public LogTaskStatus getTaskStatus(String taskId) {
        LogTaskStatus status = taskStatusMap.get(taskId);
        if (status == null) {
            throw new ObjectNotFoundException("Task ID not found: " + taskId);
        }
        return status;
    }

    public Path getLogFilePath(String taskId) {
        LogTaskStatus status = getTaskStatus(taskId);
        if (!"COMPLETED".equals(status.getStatus()) || status.getFilePath() == null) {
            throw new ObjectNotFoundException("Log file not available for task ID: " + taskId);
        }
        return Paths.get(status.getFilePath());
    }

    private String getFilteredLogs(String date) throws IOException {
        try {
            LocalDate logDate = LocalDate.parse(date, DATE_FORMATTER);
            if (logDate.isAfter(LocalDate.now())) {
                throw new ValidationException("Date cannot be in the future");
            }
        } catch (java.time.format.DateTimeParseException e) {
            throw new ValidationException("Invalid date format. Please use yyyy-MM-dd");
        }

        if (!Files.exists(LOG_PATH)) {
            throw new ObjectNotFoundException("Log file not found");
        }

        try (java.util.stream.Stream<String> lines = Files.lines(LOG_PATH)) {
            String filtered = lines
                    .filter(line -> line.startsWith(date))
                    .collect(java.util.stream.Collectors.joining("\n"));

            if (filtered.isEmpty()) {
                throw new ObjectNotFoundException("No log entries found for date: " + date);
            }

            return filtered;
        } catch (IOException e) {
            throw new LogsException("Error reading log file: " + e.getMessage());
        }
    }
}