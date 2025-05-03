package org.example.service;

import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.example.cache.SearchCache;
import org.example.exception.ObjectExistedException;
import org.example.exception.ObjectNotFoundException;
import org.example.model.City;
import org.example.model.Country;
import org.example.repository.CityRepository;
import org.example.repository.CountryRepository;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


@Service
@AllArgsConstructor
public class CityService {

    public static final String NOT_FOUND_MESSAGE = "Country not found";
    private final CityRepository cityRepository;

    private final CountryRepository countryRepository;

    private final SearchCache searchCache;
    private static final Logger logger = LoggerFactory.getLogger(CityService.class);

    private static final String ALL_CITIES_BY_COUNTRY_ID =
            "allCitiesByCountryId_";
    private static final String ALL_CITIES = "allCities";
    private static final String ALL_COUNTRIES_BY_NATION_ID =
            "allCountriesByNationId_";
    private static final String ALL_COUNTRIES = "allCountries";
    private static final String COUNTRY_ID = "countryId_";


    private void updateCache(final Country country, String action) {
        logger.info("🔄 Updating cache for country '{}' (ID: {}). Action: {}", country.getName(),
                country.getId(), action);

        searchCache.remove(ALL_CITIES);
        searchCache.remove(ALL_CITIES_BY_COUNTRY_ID + country.getId());
        searchCache.remove(COUNTRY_ID + country.getId());

        country.getNations().forEach(nation ->
                searchCache.remove(ALL_COUNTRIES_BY_NATION_ID + nation.getId())
        );
    }

    @Transactional()
    public List<City> getCities() {
        if (searchCache.containsKey(ALL_CITIES)) {
            Object cachedValue = searchCache.get(ALL_CITIES);
            List<City> cities = safeCastToListOfCities(cachedValue);
            if (cities != null) {
                logger.info("Getting cities from cache");
                return cities;
            }
            logger.warn("Invalid cache entry for key: {}", ALL_CITIES);
            searchCache.remove(ALL_CITIES);
        }

        List<City> cities = cityRepository.findAll();


        cities.forEach(city -> {
            if (city.getCountry() != null) {
                Hibernate.initialize(city.getCountry().getCities());
            }
        });

        searchCache.put(ALL_CITIES, cities);
        logger.info("Cities loaded from database and cached");
        return cities;
    }

    @SuppressWarnings("unchecked")
    private List<City> safeCastToListOfCities(Object obj) {
        if (obj instanceof List<?> list && (list.isEmpty() || list.get(0) instanceof City)) {
            return (List<City>) list;
        }
        return Collections.emptyList();
    }

    @Transactional
    public Set<City> getCitiesByCountryId(final Long countryId) {
        String cacheKey = ALL_CITIES_BY_COUNTRY_ID + countryId;

        if (searchCache.containsKey(cacheKey)) {
            return (Set<City>) searchCache.get(cacheKey);
        }


        Set<City> cities = new HashSet<>(cityRepository.findByCountryId(countryId));

        searchCache.put(cacheKey, cities);
        return cities;
    }

    @Transactional
    public City addNewCityByCountryId(final Long countryId, final City cityRequest) {
        Country country = countryRepository
                .findCountryWithCitiesById(countryId)
                .orElseThrow(() -> new ObjectNotFoundException(
                        "country, which id " + countryId
                                + " does not exist, you can't add new city"));


        if (country.getCities().stream().anyMatch(c -> c.getName().equals(cityRequest.getName()))) {
            throw new ObjectExistedException("City with name "
                    + cityRequest.getName() + " already exists");
        }


        cityRequest.setCountry(country);


        cityRepository.save(cityRequest);

        searchCache.remove(ALL_CITIES_BY_COUNTRY_ID + countryId);

        searchCache.remove(ALL_CITIES);


        if (searchCache.containsKey(ALL_CITIES)) {
            List<City> allCities = (List<City>) searchCache.get(ALL_CITIES);
            allCities.add(cityRequest);
            searchCache.put(ALL_CITIES, allCities);
        }

        logger.info("➕ Added city '{}' (ID: {}) to country '{}' (ID: {})",
                cityRequest.getName(), cityRequest.getId(), country.getName(), country.getId());

        updateCache(country, "ADD");
        return cityRequest;
    }

    @Transactional
    public List<City> addNewCitiesByCountryId(final Long countryId,
                                              final List<City> citiesRequest) {

        List<City> addedCities = new ArrayList<>();

        citiesRequest.forEach(city -> addedCities
                .add(addNewCityByCountryId(countryId, city)));

        return addedCities;
    }

    @SuppressWarnings("checkstyle:VariableDeclarationUsageDistance")
    @Transactional
    public City updateCity(final Long cityId,
                           final String name,
                           final Double population,
                           final Double areaSquareKm) {

        City city = cityRepository.findById(cityId)
                .orElseThrow(() -> new ObjectNotFoundException("City not found"));

        Country country = countryRepository
                .findCountryWithCitiesByCityId(cityId)
                .orElseThrow(() -> new ObjectNotFoundException(NOT_FOUND_MESSAGE));


        String oldName = city.getName();


        if (name != null && !name.equals(city.getName())) {
            boolean nameExists = country.getCities().stream()
                    .anyMatch(c -> c.getName().equalsIgnoreCase(name));
            if (nameExists) {
                throw new ObjectExistedException("City name already exists");
            }
            city.setName(name);
        }


        Optional.ofNullable(population).filter(p -> p > 0).ifPresent(city::setPopulation);
        Optional.ofNullable(areaSquareKm).filter(a -> a > 0).ifPresent(city::setAreaSquareKm);


        cityRepository.save(city);


        searchCache.remove(ALL_CITIES);
        searchCache.remove(ALL_CITIES_BY_COUNTRY_ID + country.getId());

        if (searchCache.containsKey(ALL_CITIES)) {
            List<City> allCities = (List<City>) searchCache.get(ALL_CITIES);

            allCities.removeIf(c -> c.getId().equals(cityId));
            allCities.add(city);
            searchCache.put(ALL_CITIES, allCities);
        }

        logger.info("✏️ Updated city '{}' (ID: {}). New name: '{}', population: {}, area: {} km²",
                oldName, cityId, name, population, areaSquareKm);

        updateCache(country, "UPDATE");

        return city;
    }

    @Transactional
    public void deleteCitiesByCountryId(final Long countryId) {
        Country country = countryRepository.findCountryWithCitiesById(countryId)
                .orElseThrow(() -> new ObjectNotFoundException(NOT_FOUND_MESSAGE));

        Set<City> citiesToDelete = new HashSet<>(country.getCities());
        logger.info("🗑️ Deleting {} cities from country '{}'",
                citiesToDelete.size(), country.getName());


        cityRepository.deleteAll(citiesToDelete);
        country.getCities().clear();
        countryRepository.save(country);


        searchCache.remove(ALL_CITIES);
        searchCache.remove(ALL_CITIES_BY_COUNTRY_ID + countryId);
        searchCache.remove(COUNTRY_ID + countryId);

        logger.info("✅ All cities deleted from country '{}'. Cache invalidated", country.getName());
    }

    @Transactional
    public void deleteCityByIdFromCountryByCountryId(final Long countryId, final Long cityId) {
        Country country = countryRepository.findCountryWithCitiesById(countryId)
                .orElseThrow(() -> new ObjectNotFoundException(NOT_FOUND_MESSAGE));

        City city = cityRepository.findById(cityId)
                .orElseThrow(() -> new ObjectNotFoundException("City not found"));

        logger.info("🗑️ Deleting city '{}' (ID: {})", city.getName(), cityId);


        cityRepository.deleteById(cityId);
        country.getCities().remove(city);
        countryRepository.save(country);

        searchCache.remove(ALL_CITIES);
        searchCache.remove(ALL_CITIES_BY_COUNTRY_ID + countryId);

        if (searchCache.containsKey(ALL_CITIES)) {
            List<City> allCities = (List<City>) searchCache.get(ALL_CITIES);
            allCities.removeIf(c -> c.getId().equals(cityId));
            searchCache.put(ALL_CITIES, allCities);
        }


        searchCache.remove(ALL_CITIES_BY_COUNTRY_ID + countryId);
        searchCache.remove(COUNTRY_ID + countryId);

        logger.info("✅ City '{}' (ID: {}) deleted and cache invalidated", city.getName(), cityId);
    }
}
