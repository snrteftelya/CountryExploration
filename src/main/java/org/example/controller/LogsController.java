package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Pattern;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.example.exception.LogsException;
import org.example.exception.ObjectNotFoundException;
import org.example.exception.ValidationException;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@Tag(name = "Logs", description = "Application logs management endpoints")
@RestController
@RequestMapping("/api/logs")
public class LogsController {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final Path LOG_PATH = Paths.get("logs/application.log");
    private static final Path LOGS_DIR = Paths.get("logs");

    @SuppressWarnings({"checkstyle:Indentation", "checkstyle:VariableDeclarationUsageDistance"})
    @Operation(
            summary = "View or download logs by date",
            description = "Returns log entries for a specific date."
            + " If the Accept header is 'application/octet-stream', a file will be downloaded."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logs found and returned"),
            @ApiResponse(responseCode = "400",
                    description = "Invalid date format or date is in the future"),
            @ApiResponse(responseCode = "404",
                    description = "No logs found for the specified date or log file not found"),
            @ApiResponse(responseCode = "500",
                    description = "Internal server error while processing log files")
    })
    @GetMapping
    public ResponseEntity<Object> downloadOrViewLogs(
            @Parameter(description = "Date in yyyy-MM-dd format",
                    example = "2025-04-24", required = true)
            @RequestParam(name = "date")
            @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}",
                    message = "Date must be in yyyy-MM-dd format") String date,
            @RequestHeader(value = HttpHeaders.ACCEPT,
                    required = false, defaultValue = MediaType.TEXT_PLAIN_VALUE) String acceptHeader
    ) throws IOException {

        String filteredLogs = getFilteredLogs(date);

        // Создаём файл с логами для конкретной даты
        Path logFile = LOGS_DIR.resolve(date + ".log");
        Files.write(logFile, filteredLogs.getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        if (acceptHeader.contains(MediaType.APPLICATION_OCTET_STREAM_VALUE)) {
            // Клиент хочет скачать файл
            InputStream inputStream = Files.newInputStream(logFile,
                    StandardOpenOption.DELETE_ON_CLOSE);
            InputStreamResource resource = new InputStreamResource(inputStream);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + date + ".log");
            headers.add(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
            headers.add(HttpHeaders.PRAGMA, "no-cache");
            headers.add(HttpHeaders.EXPIRES, "0");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(Files.size(logFile))
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        }

        // Клиент хочет просто посмотреть текст
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .body(filteredLogs);
    }

    private String getFilteredLogs(String date) throws IOException {
        try {
            LocalDate logDate = LocalDate.parse(date, DATE_FORMATTER);
            if (logDate.isAfter(LocalDate.now())) {
                throw new ValidationException("Date cannot be in the future");
            }
        } catch (DateTimeParseException e) {
            throw new ValidationException("Invalid date format. Please use yyyy-MM-dd");
        }

        if (!Files.exists(LOG_PATH)) {
            throw new ObjectNotFoundException("Log file not found");
        }

        try (Stream<String> lines = Files.lines(LOG_PATH)) {
            // Фильтруем строки, начинающиеся с искомой даты
            String filtered = lines
                    .filter(line -> line.startsWith(date))
                    .collect(Collectors.joining("\n"));

            if (filtered.isEmpty()) {
                throw new ObjectNotFoundException("No log entries found for date: " + date);
            }

            return filtered;
        } catch (IOException e) {
            throw new LogsException("Error reading log file: " + e.getMessage());
        }
    }
}
