package org.example.service;

import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.example.cache.SearchCache;
import org.example.dto.CountryDto;
import org.example.exception.ObjectExistedException;
import org.example.exception.ObjectNotFoundException;
import org.example.model.City;
import org.example.model.Country;
import org.example.repository.CityRepository;
import org.example.repository.CountryRepository;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CountryService {
    private static final Logger logger = LoggerFactory.getLogger(CountryService.class);

    private CityRepository cityRepository;

    private final CountryRepository countryRepository;
    private final SearchCache searchCache;

    private static final String ALL_COUNTRIES = "all_countries";
    private static final String COUNTRY_PREFIX = "country_";
    private static final String CITIES_BY_COUNTRY_PREFIX = "cities_country_";
    private static final String COUNTRIES_BY_NATION_PREFIX = "countries_nation_";

    // Получение всех стран
    @Transactional
    public List<Country> getCountries() {
        logger.debug("Attempting to get all countries");

        if (searchCache.containsKey(ALL_COUNTRIES)) {
            List<Country> cached = (List<Country>) searchCache.get(ALL_COUNTRIES);
            logger.info("✅ Retrieved {} countries from cache", cached.size());
            return cached;
        }

        List<Country> countries = countryRepository.findAllWithCitiesAndNations();
        initializeLazyCollections(countries);

        searchCache.put(ALL_COUNTRIES, countries);
        logger.info("🔄 Fetched {} countries from DB and cached", countries.size());
        return countries;
    }

    // Получение страны по ID
    @Transactional
    public Country getCountryById(Long countryId) {
        String cacheKey = COUNTRY_PREFIX + countryId;
        logger.debug("Looking for country in cache: {}", cacheKey);

        if (searchCache.containsKey(cacheKey)) {
            logger.info("✅ Country found in cache: {}", cacheKey);
            return (Country) searchCache.get(cacheKey);
        }

        Country country = countryRepository.findCountryWithCitiesAndNationsById(countryId)
                .orElseThrow(() -> {
                    logger.error("🚫 Country not found with ID: {}", countryId);
                    return new ObjectNotFoundException("Country not found");
                });

        initializeLazyCollections(country);
        searchCache.put(cacheKey, country);
        logger.info("🔄 Country loaded from DB and cached: {}", cacheKey);
        return country;
    }

    // Добавление страны
    @Transactional
    public Country addNewCountry(Country country) {
        logger.debug("Attempting to add new country: {}", country.getName());

        countryRepository.findCountryByName(country.getName())
                .ifPresent(c -> {
                    logger.error("🚫 Country already exists: {}", country.getName());
                    throw new ObjectExistedException("Country exists");
                });

        country.setNations(new HashSet<>());
        country.setCities(new HashSet<>());
        Country savedCountry = countryRepository.save(country);

        searchCache.remove(ALL_COUNTRIES);
        searchCache.put(COUNTRY_PREFIX + savedCountry.getId(), savedCountry);
        logger.info("✨ Created country: {} (ID: {})", savedCountry.getName(), savedCountry.getId());
        return savedCountry;
    }

    // Обновление страны
    @Transactional
    public Country updateCountry(Long countryId, String name, String capital,
                                 Double population, Double areaSquareKm, Double gdp) {
        logger.debug("Updating country ID: {}", countryId);

        Country country = getCountryById(countryId); // Используем кэшированный метод
        Country originalCountry = new Country();
        BeanUtils.copyProperties(country, originalCountry);

        if (name != null && !name.equals(country.getName())) {
            countryRepository.findCountryByName(name)
                    .ifPresent(c -> {
                        logger.error("🚫 Country name conflict: {}", name);
                        throw new ObjectExistedException("Name exists");
                    });
            country.setName(name);
        }

        // Обновление остальных полей
        Optional.ofNullable(capital).ifPresent(country::setCapital);
        Optional.ofNullable(population).ifPresent(country::setPopulation);
        Optional.ofNullable(areaSquareKm).ifPresent(country::setAreaSquareKm);
        Optional.ofNullable(gdp).ifPresent(country::setGdp);

        Country updatedCountry = countryRepository.save(country);
        updateCache(originalCountry, updatedCountry);
        logger.info("🔄 Updated country ID: {}", countryId);
        return updatedCountry;
    }

    // Удаление страны
    @Transactional
    public void deleteCountry(Long id) {
        logger.warn("Attempting to delete country ID: {}", id);

        Country country = countryRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Country not found with ID: " + id));

        // Delete all cities associated with this country
        cityRepository.deleteAll();
        // Flush to ensure the cities are deleted in the database
        cityRepository.flush();

        // Clear the cities collection in the Hibernate session
        if (country.getCities() != null) {
            country.getCities().clear();
        }

        // Clear associations with Nations (if needed)
        if (country.getNations() != null) {
            country.getNations().forEach(nation -> nation.getCountries().remove(country));
            country.getNations().clear();
        }

        // Delete the country
        countryRepository.delete(country);
        // Invalidate cache
        invalidateDependentCaches(country);
        logger.info("🗑️ Deleted country ID: {}", id);
    }

    // Вспомогательные методы
    private void initializeLazyCollections(Country country) {
        if (country.getCities() != null) {
            Hibernate.initialize(country.getCities());
        }
        if (country.getNations() != null) {
            Hibernate.initialize(country.getNations());
            country.getNations().forEach(nation ->
                    Hibernate.initialize(nation.getCountries())
            );
        }
    }

    private void initializeLazyCollections(List<Country> countries) {
        countries.forEach(this::initializeLazyCollections);
    }

    private void updateCache(Country oldCountry, Country newCountry) {
        // Инвалидация старых ключей
        searchCache.remove(COUNTRY_PREFIX + oldCountry.getId());
        searchCache.remove(ALL_COUNTRIES);

        // Обновление связанных кэшей
        newCountry.getNations().forEach(nation ->
                searchCache.remove(COUNTRIES_BY_NATION_PREFIX + nation.getId())
        );

        // Добавление обновленных данных
        searchCache.put(COUNTRY_PREFIX + newCountry.getId(), newCountry);
    }

    private void invalidateDependentCaches(Country country) {
        searchCache.remove(COUNTRY_PREFIX + country.getId());
        searchCache.remove(ALL_COUNTRIES);
        searchCache.remove(CITIES_BY_COUNTRY_PREFIX + country.getId());

        country.getNations().forEach(nation ->
                searchCache.remove(COUNTRIES_BY_NATION_PREFIX + nation.getId())
        );
    }

    @Transactional
    public List<Country> addNewCountries(List<Country> countries) {
        logger.debug("Attempting to add {} countries", countries.size());

        List<Country> savedCountries = new ArrayList<>();
        for (Country country : countries) {
            // Используем существующий метод добавления одной страны
            savedCountries.add(this.addNewCountry(country));
        }

        logger.info("✨ Added {} countries", savedCountries.size());
        return savedCountries;
    }

    @Transactional
    public void deleteCountries() {
        logger.warn("Attempting to delete all countries");

        // Очищаем все связанные кэши
        searchCache.clear();
        logger.debug("♻️ Cleared all cache entries");

        // Удаляем данные из БД
        countryRepository.deleteAll();
        logger.info("🗑️ Deleted all countries");

        // Дополнительная очистка зависимых сущностей (если нужно)
        countryRepository.findAllWithCities().forEach(country ->
                country.getCities().clear()
        );
    }

    public List<CountryDto> searchCountriesByCityName(String cityName) {
        List<Country> countries = countryRepository.findCountriesByCityName(cityName);
        return convertToCountryDtoList(countries);
    }

    private List<CountryDto> convertToCountryDtoList(List<Country> countries) {
        return countries.stream()
                .map(this::convertToCountryDto)
                .collect(Collectors.toList());
    }

    private CountryDto convertToCountryDto(Country country) {
        CountryDto dto = new CountryDto();
        dto.setId(country.getId());
        dto.setName(country.getName());
        dto.setCapital(country.getCapital());
        dto.setPopulation(country.getPopulation());
        dto.setAreaSquareKm(country.getAreaSquareKm());
        dto.setGdp(country.getGdp());
        dto.setCityIds(country.getCities().stream()
                .map(City::getId)
                .collect(Collectors.toSet()));
        return dto;
    }
}