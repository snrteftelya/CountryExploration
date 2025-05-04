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
import org.example.dto.CityDto;
import org.example.exception.ObjectNotFoundException;
import org.example.model.City;
import org.example.service.CityService;
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
@Tag(name = "Cities", description = "API for managing "
        + "city information, including viewing, adding, updating, and deleting cities")
@CrossOrigin
public class CityController {

    private final CityService cityService;

    @GetMapping("/cities")
    @Operation(summary = "Get all cities", description = "Retrieve a list of all cities")
    @ApiResponses({@ApiResponse(responseCode = "200",
            description = "List of cities retrieved successfully",
                    content = @Content(schema = @Schema(implementation = CityDto.class))),
                   @ApiResponse(responseCode = "204", description = "No cities found")
    })
    public ResponseEntity<List<CityDto>> getCities() {
        List<City> cities = cityService.getCities();
        List<CityDto> cityDtos = cities.stream().map(CityDto::fromEntity).toList();
        return cityDtos.isEmpty() ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(cityDtos);
    }

    @GetMapping("/countries/{countryId}/cities")
    @Operation(summary = "Get cities by country ID",
            description = "Retrieve a list of cities for a specific country")
    @ApiResponses({@ApiResponse(responseCode = "200",
            description = "List of cities retrieved successfully",
                    content = @Content(schema = @Schema(implementation = CityDto.class))),
                   @ApiResponse(responseCode = "204",
                           description = "No cities found for the country"),
                   @ApiResponse(responseCode = "404", description = "Country not found")
    })
    public ResponseEntity<Set<CityDto>> getCitiesByCountryId(
            @PathVariable @Parameter(description = "ID of the country",
                    example = "1") Long countryId) {
        Set<CityDto> cities = cityService.getCitiesByCountryId(countryId);
        return cities.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(cities);
    }

    @PostMapping("/countries/{countryId}/cities")
    @Operation(summary = "Add cities to a country",
            description = "Add one or more cities to a specific country")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "Cities created successfully",
                    content = @Content(schema = @Schema(implementation = City.class))),
                   @ApiResponse(responseCode = "400", description = "Invalid city data"),
                   @ApiResponse(responseCode = "404", description = "Country not found")
    })
    public ResponseEntity<List<City>> addCitiesByCountryId(
            @PathVariable @Parameter(description = "ID of the country to add the cities to",
                    example = "1") Long countryId,
            @RequestBody @Parameter(
                    description = "Single city object or list of city objects to add",
                    required = true) List<City> cities) {
        List<City> addedCities = cityService.addNewCitiesByCountryId(countryId, cities);
        return ResponseEntity.status(HttpStatus.CREATED).body(addedCities);
    }

    @PutMapping("/cities/{id}")
    @Operation(summary = "Update a city", description = "Update details of a city by its ID")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "City updated successfully",
                    content = @Content(schema = @Schema(implementation = City.class))),
                   @ApiResponse(responseCode = "400", description = "Invalid update parameters"),
                   @ApiResponse(responseCode = "404", description = "City or country not found")
    })
    public ResponseEntity<City> updateCity(
            @PathVariable("id") @Parameter(description = "ID of the city to update",
                    example = "1") Long cityId,
            @RequestParam(required = false) @Parameter(
                    description = "Name of the city", example = "Minsk") String name,
            @RequestParam(required = false) @Parameter(
                    description = "Population of the city", example = "2000000") Double population,
            @RequestParam(required = false) @Parameter(
                    description = "Area in square kilometers",
                    example = "409.5") Double areaSquareKm) {
        if (name != null && !isValidName(name)) {
            throw new IllegalArgumentException("Invalid city name");
        }
        if (population != null && (population < 0 || Double.isNaN(population)
                || Double.isInfinite(population))) {
            throw new IllegalArgumentException("Invalid population value");
        }
        if (areaSquareKm != null && (areaSquareKm < 0 || Double.isNaN(areaSquareKm)
                || Double.isInfinite(areaSquareKm))) {
            throw new IllegalArgumentException("Invalid area value");
        }
        return ResponseEntity.ok(cityService.updateCity(cityId, name, population, areaSquareKm));
    }

    private boolean isValidName(String name) {
        return name.matches("^[a-zA-Z0-9\\s\\-,.]{1,100}$");
    }

    @DeleteMapping("/countries/{countryId}/cities")
    @Operation(summary = "Delete all cities in a country",
            description = "Delete all cities associated with a specific country")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Cities deleted successfully"),
                   @ApiResponse(responseCode = "404", description = "Country not found")
    })
    public ResponseEntity<Void> deleteCitiesByCountryId(
            @PathVariable @Parameter(description = "ID of the country to delete cities from",
                    example = "1") Long countryId) {
        cityService.deleteCitiesByCountryId(countryId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/cities/{cityId}")
    @Operation(summary = "Delete a city", description = "Delete a city by its ID")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "City deleted successfully"),
                   @ApiResponse(responseCode = "404", description = "City not found")
    })
    public ResponseEntity<Void> deleteCity(
            @PathVariable @Parameter(description = "ID of the city to delete",
                    example = "1") Long cityId) {
        try {
            cityService.deleteCityById(cityId);
            return ResponseEntity.noContent().build();
        } catch (ObjectNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/countries/{countryId}/cities/{cityId}")
    @Operation(summary = "Delete a city from a country",
            description = "Delete a specific city from a specific country")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "City deleted successfully"),
                   @ApiResponse(responseCode = "404", description = "City or country not found")
    })
    public ResponseEntity<Void> deleteCityByIdFromCountryByCountryId(
            @PathVariable @Parameter(description = "ID of the country",
                    example = "1") Long countryId,
            @PathVariable @Parameter(description = "ID of the city to delete",
                    example = "1") Long cityId) {
        cityService.deleteCityByIdFromCountryByCountryId(countryId, cityId);
        return ResponseEntity.noContent().build();
    }
}
