package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.AllArgsConstructor;
import org.example.model.Country;
import org.example.service.CountryService;
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
@RequestMapping("api/country")
@Tag(name = "Countries", description = "API for managing country information,"
       + " including viewing, adding, updating, and deleting countries")
@CrossOrigin
public class CountryController {

    private final CountryService countryService;

    @GetMapping
    @Operation(summary = "Get all countries", description = "Retrieve a list of all countries")
    @ApiResponses({@ApiResponse(responseCode = "200",
            description = "List of countries retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Country.class))),
                   @ApiResponse(responseCode = "204", description = "No countries found")
    })
    public ResponseEntity<List<Country>> getCountries() {
        List<Country> countries = countryService.getCountries();
        return countries.isEmpty()
                ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(countries);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get country by ID", description = "Retrieve a country by its unique ID")
    @ApiResponses({@ApiResponse(responseCode = "200",
            description = "Country retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Country.class))),
                   @ApiResponse(responseCode = "404", description = "Country not found")
    })
    public ResponseEntity<Country> getCountryById(
            @PathVariable("id") @Parameter(
                    description = "ID of the country to retrieve", example = "1") Long countryId) {
        return ResponseEntity.ok(countryService.getCountryById(countryId));
    }

    @PostMapping
    @Operation(summary = "Add a new country",
            description = "Create a new country with the provided details")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "Country created successfully",
                    content = @Content(schema = @Schema(implementation = Country.class))),
                   @ApiResponse(responseCode = "400", description = "Invalid country data")
    })
    public ResponseEntity<Country> addNewCountry(
            @RequestBody @Parameter(description = "Country object to add",
                    required = true) Country country) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(countryService.addNewCountry(country));
    }

    @PostMapping("/bulk")
    @Operation(summary = "Add multiple countries", description = "Create a list of new countries")
    @ApiResponses({@ApiResponse(responseCode = "201",
            description = "Countries created successfully",
                    content = @Content(schema = @Schema(implementation = Country.class))),
                   @ApiResponse(responseCode = "400", description = "Invalid countries data")
    })
    public ResponseEntity<List<Country>> addNewCountries(
            @RequestBody @Parameter(description = "List of country objects to add",
                    required = true) List<Country> countries) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(countryService.addNewCountries(countries));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a country", description = "Update details of a country by its ID")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Country updated successfully",
                    content = @Content(schema = @Schema(implementation = Country.class))),
                   @ApiResponse(responseCode = "404", description = "Country not found"),
                   @ApiResponse(responseCode = "400", description = "Invalid update parameters")
    })
    public ResponseEntity<Country> updateCountry(
            @PathVariable("id") @Parameter(description = "ID of the country to update",
                    example = "1") Long countryId,
            @RequestParam(required = false) @Parameter(description = "Name of the country",
                    example = "Germany") String name,
            @RequestParam(required = false) @Parameter(description = "Capital of the country",
                    example = "Berlin") String capital,
            @RequestParam(required = false) @Parameter(description = "Population of the country",
                    example = "83240525") Double population,
            @RequestParam(required = false) @Parameter(description = "Area in square kilometers",
                    example = "357582") Double areaSquareKm,
            @RequestParam(required = false) @Parameter(description = "GDP in billions USD",
                    example = "4456.0") Double gdp) {
        return ResponseEntity.ok(countryService.updateCountry(countryId, name, capital, population,
                areaSquareKm, gdp));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a country", description = "Delete a country by its ID")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Country deleted successfully"),
                   @ApiResponse(responseCode = "404", description = "Country not found")
    })
    public ResponseEntity<Void> deleteCountry(
            @PathVariable("id") @Parameter(
                    description = "ID of the country to delete", example = "1") Long countryId) {
        countryService.deleteCountry(countryId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    @Operation(summary = "Delete all countries",
            description = "Delete all countries and their associated data")
    @ApiResponses({@ApiResponse(responseCode = "204",
            description = "All countries deleted successfully")
    })
    public ResponseEntity<Void> deleteCountries() {
        countryService.deleteCountries();
        return ResponseEntity.noContent().build();
    }
}
