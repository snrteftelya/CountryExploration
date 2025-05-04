package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Pattern;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.example.exception.LogsException;
import org.example.exception.ObjectNotFoundException;
import org.example.service.LogProcessingService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@Tag(name = "Logs", description = "Application logs management endpoints")
@RestController
@RequestMapping("/api/logs")
public class LogsController {

    private final LogProcessingService logProcessingService;

    public LogsController(LogProcessingService logProcessingService) {
        this.logProcessingService = logProcessingService;
    }

    @PostMapping
    @Operation(
            summary = "Start asynchronous log file creation",
            description = "Initiates the creation of a log file" 
                    + " for a specific date and returns a task ID."
    )
    @ApiResponses(value = {@ApiResponse(responseCode = "202",
            description = "Log file creation started, task ID returned"),
                           @ApiResponse(responseCode = "400",
                                   description = "Invalid date format or date is in the future")
    })
    public ResponseEntity<String> startLogFileCreation(
            @Parameter(description = "Date in yyyy-MM-dd format",
                    example = "2025-04-24", required = true)
            @RequestParam(name = "date")
            @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}",
                    message = "Date must be in yyyy-MM-dd format") String date) {
        String taskId = logProcessingService.startLogFileCreation(date);
        return ResponseEntity.accepted().body(taskId);
    }

    @GetMapping("/status/{taskId}")
    @Operation(
            summary = "Check log file creation status",
            description = "Returns the status of the log file creation task by task ID."
    )
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Status returned"),
                           @ApiResponse(responseCode = "404", description = "Task ID not found")
    })
    public ResponseEntity<LogProcessingService.LogTaskStatus> getLogFileStatus(
            @Parameter(description = "Task ID of the log file creation",
                    example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable String taskId) {
        return ResponseEntity.ok(logProcessingService.getTaskStatus(taskId));
    }

    @GetMapping("/file/{taskId}")
    @Operation(
            summary = "Download log file by task ID",
            description = "Downloads the log file for a completed task by task ID."
    )
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Log file downloaded"), 
                           @ApiResponse(responseCode = "404",
                                   description = "Task ID not found or file not available")
    })
    public ResponseEntity<InputStreamResource> downloadLogFile(
            @Parameter(description = "Task ID of the log file creation",
                    example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable String taskId) throws IOException {
        Path logFile = logProcessingService.getLogFilePath(taskId);
        if (!Files.exists(logFile)) {
            throw new ObjectNotFoundException("Log file not found for task ID: " + taskId);
        }
        InputStream inputStream = Files.newInputStream(logFile);
        InputStreamResource resource = new InputStreamResource(inputStream);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=log_" + taskId + ".log");
        headers.add(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
        headers.add(HttpHeaders.PRAGMA, "no-cache");
        headers.add(HttpHeaders.EXPIRES, "0");

        ResponseEntity<InputStreamResource> response = ResponseEntity.ok()
                .headers(headers)
                .contentLength(Files.size(logFile))
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);

        Files.deleteIfExists(logFile);
        return response;
    }

    @GetMapping
    @Operation(
            summary = "View or download logs by date",
            description = "Returns log entries for a specific date. If "
                    + "the Accept header is 'application/octet-stream', a file will be downloaded."
    )
    @ApiResponses(value = {@ApiResponse(responseCode = "200",
            description = "Logs found and returned"),
                           @ApiResponse(responseCode = "400",
                                   description = "Invalid date format or date is in the future"),
                           @ApiResponse(responseCode = "404",
                                   description = "No logs found for the specified date or"
                                          + " log file not found"),
                           @ApiResponse(responseCode = "500",
                                   description = "Internal server error while processing log files")
    })
    public ResponseEntity<Object> downloadOrViewLogs(
            @Parameter(description = "Date in yyyy-MM-dd format",
                    example = "2025-04-24", required = true)
            @RequestParam(name = "date")
            @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "Date must be in yyyy-MM-dd format")
            String date,
            @RequestHeader(value = HttpHeaders.ACCEPT, required = false,
                    defaultValue = MediaType.TEXT_PLAIN_VALUE) String acceptHeader
    ) throws IOException {
        String taskId = logProcessingService.startLogFileCreation(date);
        LogProcessingService.LogTaskStatus status = logProcessingService.getTaskStatus(taskId);

        while (!"COMPLETED".equals(status.getStatus()) && !"FAILED".equals(status.getStatus())) {
            try {
                Thread.sleep(100);
                status = logProcessingService.getTaskStatus(taskId);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new LogsException("Interrupted while waiting for log file creation");
            }
        }

        if ("FAILED".equals(status.getStatus())) {
            throw new LogsException("Log file creation failed: " + status.getErrorMessage());
        }

        Path logFile = logProcessingService.getLogFilePath(taskId);
        String filteredLogs = Files.readString(logFile);

        if (acceptHeader.contains(MediaType.APPLICATION_OCTET_STREAM_VALUE)) {
            InputStream inputStream = Files.newInputStream(logFile);
            InputStreamResource resource = new InputStreamResource(inputStream);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + date + ".log");
            headers.add(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
            headers.add(HttpHeaders.PRAGMA, "no-cache");
            headers.add(HttpHeaders.EXPIRES, "0");

            ResponseEntity<Object> response = ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(Files.size(logFile))
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);

            Files.deleteIfExists(logFile);
            return response;
        }

        Files.deleteIfExists(logFile);
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .body(filteredLogs);
    }
}
