package org.example.service;

import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.example.cache.SearchCache;
import org.example.dto.CityDto;
import org.example.exception.ObjectExistedException;
import org.example.exception.ObjectNotFoundException;
import org.example.model.City;
import org.example.model.Country;
import org.example.repository.CityRepository;
import org.example.repository.CountryRepository;
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

    private static final String CITIES_BY_COUNTRY_PREFIX = "cities_country_";
    private static final String CITY_PREFIX = "city_";
    private static final String COUNTRY_PREFIX = "country_";
    private static final String ALL_CITIES_BY_COUNTRY_ID = "allCitiesByCountryId_";
    private static final String ALL_CITIES = "allCities";
    private static final String ALL_COUNTRIES_BY_NATION_ID = "allCountriesByNationId_";
    private static final String COUNTRY_ID = "countryId_";

    private void updateCache(final Country country, final String operation) {
        Long countryId = country.getId();
        searchCache.remove(ALL_CITIES);
        searchCache.remove(ALL_CITIES_BY_COUNTRY_ID + countryId);
        searchCache.remove(COUNTRY_ID + countryId);
        searchCache.remove(CITIES_BY_COUNTRY_PREFIX + countryId);
        if (country.getNations() != null) {
            country.getNations().forEach(nation -> searchCache.remove(
                    ALL_COUNTRIES_BY_NATION_ID + nation.getId()));
        }
        logger.info("🔄 Обновлён кэш для страны с ID: {}. Операция: {}", countryId, operation);
    }

    private boolean isValidName(String name) {
        return name.matches("^[a-zA-Z0-9\\s\\-,.]{1,100}$");
    }

    @Transactional
    public List<City> getCities() {
        if (searchCache.containsKey(ALL_CITIES)) {
            Object cachedValue = searchCache.get(ALL_CITIES);
            List<City> cities = safeCastToListOfCities(cachedValue);
            if (!cities.isEmpty()) {
                logger.info("Getting cities from cache");
                return cities.stream()
                        .filter(c -> c.getCountry() == null || countryRepository.existsById(
                                c.getCountry().getId())).toList();
            }
            logger.warn("Invalid or empty cache entry for key: {}", ALL_CITIES);
            searchCache.remove(ALL_CITIES);
        }

        List<City> cities = cityRepository.findAll();
        cities = cities.stream()
                .filter(c -> c.getCountry() == null || countryRepository.existsById(
                        c.getCountry().getId())).toList();

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
    public Set<CityDto> getCitiesByCountryId(Long countryId) {
        if (countryId == null) {
            throw new IllegalArgumentException("Country ID cannot be null");
        }
        String cacheKey = CITIES_BY_COUNTRY_PREFIX + countryId;

        if (searchCache.containsKey(cacheKey)) {
            Object cached = searchCache.get(cacheKey);
            if (cached instanceof Set<?> set && (set.isEmpty() || set.iterator().next()
                    instanceof CityDto)) {
                logger.info("Getting cities with countryId_{} from cache", countryId);
                if (logger.isInfoEnabled()) {
                    logger.info("{}", cached);
                }
                return (Set<CityDto>) cached;
            }
            if (cached instanceof List<?> list && (list.isEmpty() || list.get(0)
                    instanceof CityDto)) {
                logger.info("Converting cached list to set for countryId_{}", countryId);
                Set<CityDto> converted = new HashSet<>((List<CityDto>) cached);
                searchCache.put(cacheKey, converted);
                return converted;
            }
            logger.warn("Invalid cache entry for key: {}. Removing cache.", cacheKey);
            searchCache.remove(cacheKey);
        }

        if (!countryRepository.existsById(countryId)) {
            return Collections.emptySet();
        }
        Set<CityDto> result = cityRepository.findByCountryId(countryId).stream()
                .map(CityDto::fromEntity)
                .collect(Collectors.toSet());
        searchCache.put(cacheKey, result);
        logger.info("Cities with countryId_{} loaded from database and cached", countryId);
        if (logger.isInfoEnabled()) {
            logger.info("{}", result);
        }
        return result;
    }

    public void evictCitiesByCountryCache(Long countryId) {
        String cacheKey = CITIES_BY_COUNTRY_PREFIX + countryId;
        searchCache.remove(cacheKey);
        logger.info("Evicted cities cache for country {}", countryId);
    }

    @Transactional
    public City addNewCityByCountryId(final Long countryId, final City cityRequest) {
        if (countryId == null) {
            throw new IllegalArgumentException("Country ID cannot be null");
        }
        if (cityRequest == null) {
            throw new IllegalArgumentException("City request cannot be null");
        }
        Country country = countryRepository
                .findCountryWithCitiesById(countryId)
                .orElseThrow(() -> new ObjectNotFoundException("country, which id "
                        + countryId + " does not exist, you can't add new city"));

        if (cityRequest.getName() == null || cityRequest.getName().isEmpty()) {
            throw new IllegalArgumentException("City name cannot be null or empty");
        }
        if (country.getCities().stream().anyMatch(c -> c.getName().equalsIgnoreCase(
                cityRequest.getName()))) {
            throw new ObjectExistedException("City with name " + cityRequest.getName()
                    + " already exists");
        }

        cityRequest.setCountry(country);
        updateCache(country, "ADD");
        City savedCity = cityRepository.save(cityRequest);
        logger.info("➕ Added city with ID: {} to country with ID: {}", savedCity.getId(),
                countryId);
        return savedCity;
    }

    @Transactional
    public List<City> addNewCitiesByCountryId(final Long countryId,
                                              final List<City> citiesRequest) {
        if (countryId == null) {
            throw new IllegalArgumentException("Country ID cannot be null");
        }
        if (citiesRequest == null) {
            throw new IllegalArgumentException("Cities request cannot be null");
        }
        List<City> addedCities = new ArrayList<>();
        citiesRequest.forEach(city -> addedCities.add(addNewCityByCountryId(countryId, city)));
        return addedCities;
    }

    @Transactional
    public City updateCity(final Long cityId, final String name, final Double population,
                           final Double areaSquareKm) {
        if (cityId == null) {
            throw new IllegalArgumentException("ID города не может быть null");
        }
        if (name != null && !name.isEmpty() && !isValidName(name)) {
            throw new IllegalArgumentException("Недопустимое название города");
        }
        City city = cityRepository.findById(cityId)
                .orElseThrow(() -> new ObjectNotFoundException("Город не найден"));
        Country country = countryRepository.findCountryWithCitiesByCityId(cityId)
                .orElseThrow(() -> new ObjectNotFoundException("Страна не найдена для города с ID: "
                        + cityId));
        if (name != null && !name.isEmpty() && !name.equalsIgnoreCase(city.getName())) {
            boolean nameExists = country.getCities().stream()
                    .anyMatch(c -> c.getName().equalsIgnoreCase(name) && !c.getId().equals(cityId));
            if (nameExists) {
                throw new ObjectExistedException("Название города уже существует в этой стране");
            }
            city.setName(name);
        }


        Optional.ofNullable(population).filter(p -> p >= 0 && !Double.isNaN(p)
                        && !Double.isInfinite(p))
                .ifPresent(city::setPopulation);
        Optional.ofNullable(areaSquareKm).filter(a -> a > 0 && !Double.isNaN(a)
                        && !Double.isInfinite(a))
                .ifPresent(city::setAreaSquareKm);


        updateCache(country, "UPDATE");


        cityRepository.save(city);
        logger.info("✏️ Обновлён город с ID: {}. Операция: UPDATE", cityId);
        return city;
    }

    @Transactional
    public void deleteCitiesByCountryId(final Long countryId) {
        if (countryId == null) {
            throw new IllegalArgumentException("Country ID cannot be null");
        }
        Country country = countryRepository.findCountryWithCitiesById(countryId)
                .orElseThrow(() -> new ObjectNotFoundException(NOT_FOUND_MESSAGE));

        Set<City> citiesToDelete = new HashSet<>(country.getCities());
        logger.info("🗑️ Deleting {} cities from country with ID: {}",
                citiesToDelete.size(), countryId);

        updateCache(country, "DELETE");
        cityRepository.deleteAll(citiesToDelete);
        country.getCities().clear();
        countryRepository.save(country);
    }

    @Transactional
    public void deleteCityById(Long cityId) {
        if (cityId == null) {
            throw new IllegalArgumentException("City ID cannot be null");
        }
        City city = cityRepository.findByIdWithCountry(cityId)
                .orElseThrow(() -> new ObjectNotFoundException("City with id "
                        + cityId + " not found"));

        Long countryId = city.getCountry() != null ? city.getCountry().getId() : null;

        if (countryId != null) {
            city.getCountry().getCities().remove(city);
        }

        cityRepository.delete(city);
        invalidateCityCaches(cityId, countryId);

        logger.info("🗑️ Deleted city with ID: {}", cityId);
    }

    private void invalidateCityCaches(Long cityId, Long countryId) {
        searchCache.remove(ALL_CITIES);
        searchCache.remove(CITY_PREFIX + cityId);
        if (countryId != null) {
            searchCache.remove(CITIES_BY_COUNTRY_PREFIX + countryId);
            searchCache.remove(COUNTRY_PREFIX + countryId);
        }
    }

    @Transactional
    public void deleteCityByIdFromCountryByCountryId(final Long countryId, final Long cityId) {
        if (countryId == null || cityId == null) {
            throw new IllegalArgumentException("Country ID and City ID cannot be null");
        }
        Country country = countryRepository.findCountryWithCitiesById(countryId)
                .orElseThrow(() -> new ObjectNotFoundException(NOT_FOUND_MESSAGE));

        City city = cityRepository.findById(cityId)
                .orElseThrow(() -> new ObjectNotFoundException("City not found"));

        if (!country.getCities().contains(city)) {
            throw new ObjectNotFoundException("City does not belong to the specified country");
        }

        logger.info("🗑️ Deleting city with ID: {} from country with ID: {}", cityId, countryId);

        updateCache(country, "DELETE");
        cityRepository.deleteById(cityId);
        country.getCities().remove(city);
        countryRepository.save(country);

        logger.info("✅ City with ID: {} deleted from country with ID: {}", cityId, countryId);
    }
}