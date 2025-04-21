package org.example.service;

import jakarta.transaction.Transactional;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.example.cache.CacheService;
import org.example.model.City;
import org.example.model.Country;
import org.example.repository.CityRepository;
import org.example.repository.CountryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CityService {

    private final CityRepository cityRepository;
    private final CountryRepository countryRepository;
    private final CacheService cacheService;

    @Value("${cache.cities.ttl:60}")
    private Long citiesCacheTtl; // Changed to Long wrapper

    private static final String ALL_CITIES_BY_COUNTRY_ID = "allCitiesByCountryId_";
    private static final String ALL_CITIES = "allCities";
    private static final Logger logger = LoggerFactory.getLogger(CacheService.class);

    private void updateCache(Country country) {
        if (cacheService.containsKey(ALL_CITIES_BY_COUNTRY_ID + country.getId())) {
            cacheService.put(ALL_CITIES_BY_COUNTRY_ID + country.getId(),
                    country.getCities(), citiesCacheTtl);
        }
        if (cacheService.containsKey("allCountries")) {
            cacheService.remove("allCountries");
        }

        if (cacheService.containsKey("countryId_" + country.getId())) {
            cacheService.put("countryId_" + country.getId(), country, citiesCacheTtl);
        }
    }

    public List<City> getCities() {
        if (cacheService.containsKey(ALL_CITIES)) {
            @SuppressWarnings("unchecked")
            List<City> cities = (List<City>) cacheService.get(ALL_CITIES);
            logger.debug("Retrieved from cache allCities: {}", cities);
            return cities;
        } else {
            List<City> cities = cityRepository.findAll();
            logger.debug("Putting into cache allCities: {}", cities);
            cacheService.put(ALL_CITIES, cities, citiesCacheTtl);
            return cities;
        }
    }

    public Set<City> getCitiesByCountryId(Long countryId) {
        if (cacheService.containsKey(ALL_CITIES_BY_COUNTRY_ID + countryId)) {
            @SuppressWarnings("unchecked")
            Set<City> cities = (Set<City>) cacheService.get(ALL_CITIES_BY_COUNTRY_ID + countryId);
            logger.debug("Retrieved from cache allCitiesById: {}", cities);
            return cities;
        } else {
            Country country = countryRepository.findCountryWithCitiesById(countryId)
                    .orElseThrow(() -> new IllegalStateException(
                            "country with id " + countryId + " does not exist,"
                                    + " that's why you can't view cities from it"));
            Set<City> cities = country.getCities();
            logger.debug("Putting into cache allCitiesById: {}", cities);
            cacheService.put(ALL_CITIES_BY_COUNTRY_ID + countryId, cities, citiesCacheTtl);
            return cities;
        }
    }

    @Transactional
    public void addNewCityByCountryId(Long countryId, City cityRequest) {
        Country country = countryRepository.findCountryWithCitiesById(countryId)
                .orElseThrow(() -> new IllegalStateException(
                        "country, which id " + countryId
                                + " does not exist, that's why you can't add new city"));

        if (country.getCities().stream().anyMatch(city -> city.getName()
                .equals(cityRequest.getName()))) {
            throw new IllegalStateException("city with name " + cityRequest.getName()
                    + " already exists in the country " + country.getName() + ".");
        }

        country.getCities().add(cityRequest);
        cityRepository.save(cityRequest);
        countryRepository.save(country);

        if (cacheService.containsKey(ALL_CITIES)) {
            List<City> allCities = (List<City>) cacheService.get(ALL_CITIES);
            allCities.add(cityRequest);
            cacheService.put(ALL_CITIES, allCities, citiesCacheTtl);
        }

        updateCache(country);
    }


    @Transactional
    public void deleteCitiesByCountryId(Long countryId) {
        Country country = countryRepository.findCountryWithCitiesById(countryId)
                .orElseThrow(() -> new IllegalStateException(
                        "country, which id " + countryId
                                + " does not exist, that's why you can't delete cities from it"));

        final Set<City> citiesBeforeChanges = new HashSet<>(country.getCities());

        cityRepository.deleteAllByCountryId(countryId); // Updated method call
        country.getCities().clear();
        countryRepository.save(country);

        if (cacheService.containsKey(ALL_CITIES)) {
            List<City> allCities = (List<City>) cacheService.get(ALL_CITIES);
            allCities.removeAll(citiesBeforeChanges);
            cacheService.put(ALL_CITIES, allCities, citiesCacheTtl);
        }

        updateCache(country);
    }


    @Transactional
    public void deleteCityByIdFromCountryByCountryId(Long countryId, Long cityId) {
        Country country = countryRepository.findCountryWithCitiesById(countryId)
                .orElseThrow(() -> new IllegalStateException(
                        "country with id " + countryId
                                + " does not exist, that's why you can't delete city from it"));

        City city = cityRepository.findById(cityId)
                .orElseThrow(() -> new IllegalStateException(
                        "city with id " + cityId // Fixed variable name from countryId to cityId
                                + " does not exist, that's why you can't delete it"));

        cityRepository.deleteById(city.getId());
        country.getCities().remove(city);
        countryRepository.save(country);

        if (cacheService.containsKey(ALL_CITIES)) {
            List<City> allCities = (List<City>) cacheService.get(ALL_CITIES);
            allCities.remove(city);
            cacheService.put(ALL_CITIES, allCities, citiesCacheTtl);
        }

        updateCache(country);
    }

    @Transactional
    public void updateCity(Long cityId,
                           String name,
                           Double population,
                           Double areaSquareKm) {
        City city = cityRepository.findById(cityId)
                .orElseThrow(() -> new IllegalStateException(
                        "city with id " + cityId + " does not exist"));

        Country country = countryRepository.findCountryWithCitiesByCityId(cityId)
                .orElseThrow(() -> new IllegalStateException(
                        "country with city, which id "
                                + cityId + " cannot be updated, because it does not exist"));

        City cityBeforeChanges = new City();
        BeanUtils.copyProperties(city, cityBeforeChanges);

        if (name != null && !name.isEmpty() && !Objects.equals(city.getName(), name)) {
            boolean nameExists = country.getCities().stream()
                    .anyMatch(c -> Objects.equals(c.getName(), name) && !c.getId().equals(cityId));
            if (nameExists) {
                throw new IllegalStateException(
                        "In this country city with this name already exists");
            }
            city.setName(name);
        }

        if (population != null && population > 0) {
            city.setPopulation(population);
        }

        if (areaSquareKm != null && areaSquareKm > 0) {
            city.setAreaSquareKm(areaSquareKm);
        }

        cityRepository.save(city); // Don't forget to save the changes

        if (cacheService.containsKey(ALL_CITIES)) {
            List<City> allCities = (List<City>) cacheService.get(ALL_CITIES);
            allCities.remove(cityBeforeChanges);
            allCities.add(city);
            cacheService.put(ALL_CITIES, allCities, citiesCacheTtl);
        }

        updateCache(country);
    }
}
