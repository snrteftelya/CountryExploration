package org.example.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.AllArgsConstructor;
import org.example.dto.CountryDto;
import org.example.service.CountryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;



@RestController
@AllArgsConstructor
@Tag(name = "Search", description = "Find cities")
public class SearchController {

    private final CountryService countryService;

    @GetMapping("/search")
    public ResponseEntity<List<CountryDto>> searchCountriesByCity(
            @RequestParam(required = false) String cityName
    ) {
        List<CountryDto> list = countryService.searchCountriesByCityName(cityName);
        return list.isEmpty()
                ? ResponseEntity.status(HttpStatus.NOT_FOUND).body(null)
                : ResponseEntity.ok(list);
    }
}