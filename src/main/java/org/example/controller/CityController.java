package org.example.controller;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.example.model.City;
import org.example.model.Country;
import org.example.repository.CountryRepository;
import org.example.service.CityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
public class CityController {

    private final CityService cityService;
    private final CountryRepository countryRepository; // Добавляем репозиторий стран

    @Autowired
    public CityController(CityService cityService, CountryRepository countryRepository) {
        this.cityService = cityService;
        this.countryRepository = countryRepository;
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
    public ResponseEntity<String> addNewCityByCountryId(
            @PathVariable Long countryId,
            @RequestBody City cityRequest) {
        Country country = countryRepository.findById(countryId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Country not found with ID: " + countryId
                ));
        cityRequest.setCountry(country);
        cityService.addNewCityByCountryId(countryId, cityRequest);
        return ResponseEntity.ok("City added successfully to country ID: " + countryId);
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

    @GetMapping("countries/{countryId}/cities/analytics")
    public Map<String, Object> getCityAnalyticsByCountry(
            @PathVariable Long countryId,
            @RequestParam(required = false, defaultValue = "100000") int smallCityThreshold,
            @RequestParam(required = false, defaultValue = "1000000") int largeCityThreshold
    ) {
        Set<City> cities = cityService.getCitiesByCountryId(countryId);


        if (cities.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "No cities found for country ID: " + countryId
            );
        }

        // Основные метрики
        double totalPopulation = cities.stream()
                .mapToDouble(City::getPopulation)
                .sum();

        double avgArea = cities.stream()
                .mapToDouble(City::getAreaSquareKm)
                .average()
                .orElse(0);

        City largestCity = cities.stream()
                .max(Comparator.comparingDouble(City::getPopulation))
                .orElseThrow();

        // Распределение по размерам
        Map<String, Long> citySizeDistribution = cities.stream()
                .collect(Collectors.groupingBy(
                        city -> {
                            if (city.getPopulation() < smallCityThreshold) {
                                return "small";
                            } else if (city.getPopulation() < largeCityThreshold) {
                                return "medium";
                            } else {
                                return "large";
                            }
                        },
                        Collectors.counting()
                ));

        // Формируем ответ
        return Map.of(
                "totalCities", cities.size(),
                "totalPopulation", totalPopulation,
                "averageAreaSquareKm", avgArea,
                "largestCity", Map.of(
                        "name", largestCity.getName(),
                        "population", largestCity.getPopulation(),
                        "areaSquareKm", largestCity.getAreaSquareKm()
                ),
                "citySizeDistribution", citySizeDistribution
        );
    }
}
