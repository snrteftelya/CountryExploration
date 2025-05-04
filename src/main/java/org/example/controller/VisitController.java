package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.service.VisitService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/visits")
@Tag(name = "Visit", description = "API for tracking website visits")
public class VisitController {
    private final VisitService visitService;

    public VisitController(VisitService visitService) {
        this.visitService = visitService;
    }

    @Operation(summary = "Count visit",
            description = "Increments the visit count for the given URL.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200",
            description = "Visit count incremented")
    })
    @GetMapping("/count")
    public ResponseEntity<Void> countVisit() {
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Get visit count for a specific URL",
            description = "Retrieves the visit count for the given URL.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Visit count retrieved",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(type = "integer")))
    })
    @GetMapping("/result")
    public ResponseEntity<Long> getVisitCount(
            @Parameter(description = "URL to get visit count for",
                    example = "/some/path") @RequestParam String url
    ) {
        return ResponseEntity.ok(visitService.getVisitCount(url));
    }
}