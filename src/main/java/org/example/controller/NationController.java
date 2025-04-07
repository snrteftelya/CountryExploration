package org.example.controller;

import org.example.model.Country;
import org.example.model.Nation;
import org.example.service.NationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api")
public class NationController {

    private final NationService nationService;

    @Autowired
    public NationController(NationService nationService) {
        this.nationService = nationService;
    }

    @GetMapping("countries/{countryId}/nations")
    public Set<Nation> getNationsByCountryId(@PathVariable(value = "countryId") Long countryId) {
        return nationService.getNationsByCountryId(countryId);
    }

    @GetMapping("/nations")
    public List<Nation> getNations() {
        return nationService.getNations();
    }

    @GetMapping("nations/{nationId}/countries")
    public Set<Country> getCountriesByNationId(@PathVariable(value = "nationId") Long nationId) {
        return nationService.getCountriesByNationId(nationId);
    }

    @PostMapping("countries/{countryId}/nations")
    public void registerNewNationByCountryId(@PathVariable(value = "countryId") Long countryId, @RequestBody Nation nation) {
        nationService.addNewNationByCountryId(countryId, nation);
    }

    @PutMapping("/nations/{id}")
    public void updateNation(
            @PathVariable("id") Long cityId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) String religion) {
        nationService.updateNation(cityId, name, language, religion);
    }

    @DeleteMapping("/nations/{id}")
    public void deleteNation(@PathVariable(value = "id") Long nationId) {
        nationService.deleteNation(nationId);
    }

    @DeleteMapping("/countries/{countryId}/nations/{nationId}")
    public void deleteNationFromCountry(@PathVariable(value = "countryId") Long countryId,
                                        @PathVariable(value = "nationId") Long nationId) {
        nationService.deleteNationFromCountry(countryId, nationId);
    }
}