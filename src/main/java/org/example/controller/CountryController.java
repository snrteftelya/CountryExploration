package org.example.controller;

import org.example.model.Country;
import org.example.service.CountryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping(path = "api/country")
@Tag(name = "Countries", description = "You can view, add, update"
        + " and delete information about countries")
@CrossOrigin
public class CountryController {

    private final CountryService countryService;

    @GetMapping
    @Operation(method = "GET",
            summary = "Get countries",
            description = "Get information about all countries")
    public ResponseEntity<List<Country>> getCountries() {
        List<Country> countries = countryService.getCountries();
        if (countries.isEmpty()) {
            return new ResponseEntity<>(countries, HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(countries, HttpStatus.OK);
    }

    @GetMapping(path = "{id}")
    @Operation(method = "GET",
            summary = "Get country",
            description = "Get information about country by its id")
    public ResponseEntity<Country> getCountryById(
            @PathVariable("id")
            @Parameter(description = "Id of the country,"
                    + " which information you want to see")
            final Long countryId) {
        return new ResponseEntity<>(countryService
                .getCountryById(countryId), HttpStatus.OK);
    }

    @PostMapping
    @Operation(method = "POST",
            summary = "Add country",
            description = "Add new country at existed countries")
    public ResponseEntity<Country> addNewCountry(
            @RequestBody final Country country) {
        return new ResponseEntity<>(countryService
                .addNewCountry(country), HttpStatus.CREATED);
    }

    @PostMapping("/bulk")
    @Operation(method = "POST",
            summary = "Add countries",
            description = "Add new list of countries at existed countries")
    public ResponseEntity<List<Country>> addNewCountries(
            @RequestBody final List<Country> countries) {
        return new ResponseEntity<>(countryService
                .addNewCountries(countries), HttpStatus.CREATED);
    }

    @PutMapping(path = "{id}")
    @Operation(method = "PUT",
            summary = "Update country",
            description = "Update information about country by its id")
    public ResponseEntity<Country> updateCountry(
            @PathVariable("id") final Long countryId,
            @RequestParam(required = false)
            @Parameter(description = "Name of country")
            final String name,
            @RequestParam(required = false)
            @Parameter(description = "Name of capital of country")
            final String capital,
            @RequestParam(required = false)
            @Parameter(description = "Quantity of population of country")
            final Double population,
            @RequestParam(required = false)
            @Parameter(description = "Area of country in square km")
            final Double areaSquareKm,
            @RequestParam(required = false)
            @Parameter(description = "Quantity of gdp of country")
            final Double gdp) {
        return new ResponseEntity<>(countryService
                .updateCountry(countryId, name, capital,
                        population, areaSquareKm, gdp),
                HttpStatus.OK);
    }

    @DeleteMapping(path = "{id}")
    @Operation(method = "DELETE",
            summary = "Delete country by id",
            description = "Delete country from existing countries")
    public ResponseEntity<HttpStatus> deleteCountry(
            @PathVariable("id")
            @Parameter(description = "Id of the country, that's need to delete")
            final Long countryId) {
        countryService.deleteCountry(countryId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping
    @Operation(method = "DELETE",
            summary = "Delete all countries",
            description = "Delete all countries and their cities")
    public ResponseEntity<HttpStatus> deleteCountries() {
        countryService.deleteCountries();
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
