package org.example.controller;

import java.util.List;
import java.util.Set;
import org.example.model.City;
import org.example.service.CityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class CityController {

    private final CityService cityService;

    @Autowired
    public CityController(CityService cityService) {
        this.cityService = cityService;
    }

    @GetMapping(path = "cities")
    public List<City> getCities() {
        return cityService.getCities();
    }

    @GetMapping(path = "countries/{countryId}/cities")
    public Set<City> getCitiesByCountryId(@PathVariable(value = "countryId") Long countryId) {
        return cityService.getCitiesByCountryId(countryId);
    }

    @PostMapping(path = "countries/{countryId}/cities")
    public void addNewCityByCountryId(@PathVariable(value = "countryId")
                                          Long countryId, @RequestBody City city) {
        cityService.addNewCityByCountryId(countryId, city);
    }

    @DeleteMapping(path = "countries/{countryId}/cities")
    public void deleteCitiesByCountryId(@PathVariable(value = "countryId") Long countryId) {
        cityService.deleteCitiesByCountryId(countryId);
    }

    @DeleteMapping(path = "countries/{countryId}/cities/{cityId}")
    public void deleteCityByIdFromCountryByCountryId(@PathVariable(value = "countryId")
                                                         Long countryId,
                                                     @PathVariable(value = "cityId") Long cityId) {
        cityService.deleteCityByIdFromCountryByCountryId(countryId, cityId);
    }

    @PutMapping(path = "cities/{id}")
    public void updateCity(
            @PathVariable("id") Long cityId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Double population,
            @RequestParam(required = false) Double areaSquareKm) {
        cityService.updateCity(cityId, name, population, areaSquareKm);
    }
}