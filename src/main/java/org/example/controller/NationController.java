package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.example.model.Country;
import org.example.model.Nation;
import org.example.service.NationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
@RequestMapping("/api")
@Tag(name = "Nations", description = "API for managing nation information,"
        + "including viewing, adding, updating, and deleting nations")
@CrossOrigin
public class NationController {

    private final NationService nationService;

    @GetMapping("/countries/{countryId}/nations")
    @Operation(summary = "Get nations by country ID",
            description = "Retrieve a list of nations for a specific country")
    @ApiResponses({@ApiResponse(responseCode = "200",
            description = "List of nations retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Nation.class))),
                   @ApiResponse(responseCode = "204",
                           description = "No nations found for the country"),
                   @ApiResponse(responseCode = "404", description = "Country not found")
    })
    public ResponseEntity<Set<Nation>> getNationsByCountryId(
            @PathVariable @Parameter(description = "ID of the country to retrieve nations for",
                    example = "1") Long countryId) {
        Set<Nation> nations = nationService.getNationsByCountryId(countryId);
        return nations.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(nations);
    }

    @GetMapping("/nations")
    @Operation(summary = "Get all nations", description = "Retrieve a list of all nations")
    @ApiResponses({@ApiResponse(responseCode = "200",
            description = "List of nations retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Nation.class))),
                   @ApiResponse(responseCode = "204", description = "No nations found")
    })
    public ResponseEntity<List<Nation>> getNations() {
        List<Nation> nations = nationService.getNations();
        return nations.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(nations);
    }

    @GetMapping("/nations/{nationId}/countries")
    @Operation(summary = "Get countries by nation ID",
            description = "Retrieve a list of countries associated with a specific nation")
    @ApiResponses({@ApiResponse(responseCode = "200",
            description = "List of countries retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Country.class))),
                   @ApiResponse(responseCode = "204",
                           description = "No countries found for the nation"),
                   @ApiResponse(responseCode = "404", description = "Nation not found")
    })
    public ResponseEntity<Set<Country>> getCountriesByNationId(
            @PathVariable @Parameter(description = "ID of the nation to retrieve countries for",
                    example = "1") Long nationId) {
        Set<Country> countries = nationService.getCountriesByNationId(nationId);
        return countries.isEmpty() ? ResponseEntity.noContent().build() :
                ResponseEntity.ok(countries);
    }

    @PostMapping("/countries/{countryId}/nations")
    @Operation(summary = "Add a single nation to a country",
            description = "Add a new nation to a specific country")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "Nation created successfully",
                    content = @Content(schema = @Schema(implementation = Nation.class))),
                   @ApiResponse(responseCode = "400", description = "Invalid nation data"),
                   @ApiResponse(responseCode = "404", description = "Country not found")
    })
    public ResponseEntity<Nation> addNewNationByCountryId(
            @PathVariable @Parameter(description = "ID of the country to add the nation to",
                    example = "1") Long countryId,
            @RequestBody @Parameter(description = "Nation object to add",
                    required = true) Nation nation) {
        return ResponseEntity.status(HttpStatus.CREATED).body(nationService.addNewNationByCountryId(
                countryId, nation));
    }

    @PostMapping("/countries/{countryId}/nations/bulk")
    @Operation(summary = "Add multiple nations to a country",
            description = "Add a list of new nations to a specific country")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "Nations created successfully",
                    content = @Content(schema = @Schema(implementation = Nation.class))),
                   @ApiResponse(responseCode = "400", description = "Invalid nations data"),
                   @ApiResponse(responseCode = "404", description = "Country not found")
    })
    public ResponseEntity<List<Nation>> addNewNationsByCountryId(
            @PathVariable @Parameter(description = "ID of the country to add the nations to",
                    example = "1") Long countryId,
            @RequestBody @Parameter(description = "List of nation objects to add",
                    required = true) List<Nation> nations) {
        return ResponseEntity.status(HttpStatus.CREATED).body(nationService
                .addNewNationsByCountryId(countryId, nations));
    }

    @PutMapping("/nations/{id}")
    @Operation(summary = "Update a nation", description = "Update details of a nation by its ID")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Nation updated successfully",
                    content = @Content(schema = @Schema(implementation = Nation.class))),
                   @ApiResponse(responseCode = "400", description = "Invalid update parameters"),
                   @ApiResponse(responseCode = "404", description = "Nation not found")
    })
    public ResponseEntity<Nation> updateNation(
            @PathVariable("id") @Parameter(description = "ID of the nation to update",
                    example = "1") Long nationId,
            @RequestParam(required = false) @Parameter(description = "Name of the nation",
                    example = "British") String name,
            @RequestParam(required = false) @Parameter(description = "Language of the nation",
                    example = "English") String language,
            @RequestParam(required = false) @Parameter(description = "Religion of the nation",
                    example = "Christianity") String religion) {
        return ResponseEntity.ok(nationService.updateNation(nationId, name, language, religion));
    }

    @DeleteMapping("/nations/{nationId}")
    @Operation(summary = "Delete a nation", description = "Delete a nation by its ID")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Nation deleted successfully"),
                   @ApiResponse(responseCode = "404", description = "Nation not found")
    })
    public ResponseEntity<Void> deleteNation(
            @PathVariable @Parameter(description = "ID of the nation to delete",
                    example = "1") Long nationId) {
        nationService.deleteNation(nationId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/countries/{countryId}/nations/{nationId}")
    @Operation(summary = "Delete a nation from a country",
            description = "Delete a specific nation from a specific country")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Nation deleted successfully"),
                   @ApiResponse(responseCode = "404", description = "Country or nation not found")
    })
    public ResponseEntity<Void> deleteNationFromCountry(
            @PathVariable @Parameter(description = "ID of the country",
                    example = "1") Long countryId,
            @PathVariable @Parameter(description = "ID of the nation to delete",
                    example = "1") Long nationId) {
        nationService.deleteNationFromCountry(countryId, nationId);
        return ResponseEntity.noContent().build();
    }
}