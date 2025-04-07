package org.example.service;

import jakarta.transaction.Transactional;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.example.cache.CacheService;
import org.example.model.Country;
import org.example.repository.CountryRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class CountryService {

    private final CountryRepository countryRepository;

    private final CacheService cacheService;

    private static final String ALL_COUNTRIES = "allCountries";
    private static final String COUNTRY_ID = "countryId_";


    public List<Country> getCountries() {
        if (cacheService.containsKey(ALL_COUNTRIES)) {
            return (List<Country>) cacheService.get(ALL_COUNTRIES);
        } else {
            List<Country> countries = countryRepository.findAllWithCitiesAndNations();
            cacheService.put(ALL_COUNTRIES, countries);
            return countries;
        }
    }

    public Country getCountryById(Long countryId) {
        if (cacheService.containsKey(COUNTRY_ID + countryId)) {
            return (Country) cacheService.get(COUNTRY_ID + countryId);
        } else {
            Country country = countryRepository.findCountryWithCitiesAndNationsById(countryId)
                    .orElseThrow(() -> new IllegalStateException(
                            "country with id " + countryId + "does not exist"));
            cacheService.put(COUNTRY_ID + countryId, country);
            return country;
        }
    }

    public void addNewCountry(Country country) {
        Optional<Country> countryOptional = countryRepository
                .findCountryByName(country.getName());
        if (countryOptional.isPresent()) {
            throw new IllegalStateException("country exists");
        }
        if (country.getNations() == null) {
            country.setNations(new HashSet<>());
        }
        if (country.getCities() == null) {
            country.setCities(new HashSet<>());
        }
        countryRepository.save(country);
        if (cacheService.containsKey(ALL_COUNTRIES)) {
            List<Country> countries = (List<Country>) cacheService.get(ALL_COUNTRIES);
            countries.add(country);
            cacheService.put(ALL_COUNTRIES, countries);
        }
        cacheService.put(COUNTRY_ID + country.getId(), country);
    }

    @Transactional
    public void deleteCountry(Long countryId) {
        Country country = countryRepository.findCountryWithCitiesById(countryId)
                .orElseThrow(() -> new IllegalStateException(
                        "country, which id " + countryId + " does not exist"));
        cacheService.remove(COUNTRY_ID + country.getId());
        if (cacheService.containsKey(ALL_COUNTRIES)) {
            List<Country> countries = (List<Country>) cacheService.get(ALL_COUNTRIES);
            countries.remove(country);
            cacheService.put(ALL_COUNTRIES, countries);
        }
        country.getCities().clear();
        countryRepository.deleteById(countryId);
    }

    public void deleteCountries() {
        List<Country> countries = countryRepository.findAllWithCities();
        for (Country country : countries) {
            country.getCities().clear();
        }
        countryRepository.deleteAll();
        cacheService.remove(ALL_COUNTRIES);
    }

    @Transactional
    public void updateCountry(Long countryId,
                              String name,
                              String capital,
                              Double population,
                              Double areaSquareKm,
                              Double gdp) {
        Country countryChanged = countryRepository.findById(countryId)
                .orElseThrow(() -> new IllegalStateException(
                        "country with id " + countryId
                                + "can not be updated, because it does not exist"));


        Country countryBeforeChanges = new Country();
        BeanUtils.copyProperties(countryChanged, countryBeforeChanges);

        if (name != null && !name.isEmpty() && !Objects.equals(countryChanged.getName(), name)) {
            Optional<Country> countryOptional = countryRepository.findCountryByName(name);
            if (countryOptional.isPresent()) {
                throw new IllegalStateException("country with this name exists");
            }
            countryChanged.setName(name);
        }

        if (capital != null && !capital.isEmpty()
                && !Objects.equals(countryChanged.getCapital(), capital)) {
            countryChanged.setCapital(capital);
        }

        if (population != null && population > 0) {
            countryChanged.setPopulation(population);
        }

        if (areaSquareKm != null && areaSquareKm > 0) {
            countryChanged.setAreaSquareKm(areaSquareKm);
        }

        if (gdp != null && gdp > 0) {
            countryChanged.setGdp(gdp);
        }

        if (cacheService.containsKey(ALL_COUNTRIES)) {
            List<Country> countries = (List<Country>) cacheService.get(ALL_COUNTRIES);
            countries.remove(countryBeforeChanges);
            countries.add(countryChanged);
            cacheService.put(ALL_COUNTRIES, countries);
        }
        cacheService.put(COUNTRY_ID + countryChanged.getId(), countryChanged);
    }
}
