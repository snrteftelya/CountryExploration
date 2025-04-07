package org.example.controller;

import org.example.model.Country;
import org.example.service.CountryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "api/country")
public class CountryController {

    private final CountryService countryService;

    @Autowired
    public CountryController(CountryService countryService) {
        this.countryService = countryService;
    }

    @GetMapping
    public List<Country> getCountries() {
        return countryService.getCountries();
    }

    @GetMapping(path = "{id}")
    public Country getCountryById(@PathVariable("id") Long countryId) {
        return countryService.getCountryById(countryId);
    }

    @PostMapping
    public void registerNewCountry(@RequestBody Country country) {
        countryService.addNewCountry(country);
    }

    @DeleteMapping(path = "{id}")
    public void deleteCountry(@PathVariable("id") Long countryId) {
        countryService.deleteCountry(countryId);
    }

    @DeleteMapping
    public void deleteCountries() {
        countryService.deleteCountries();
    }

    @PutMapping(path = "{id}")
    public void updateCountry(
            @PathVariable("id") Long countryId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String capital,
            @RequestParam(required = false) Double population,
            @RequestParam(required = false) Double areaSquareKm,
            @RequestParam(required = false) Double gdp) {
        countryService.updateCountry(countryId, name, capital, population, areaSquareKm, gdp);
    }
}