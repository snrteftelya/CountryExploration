package org.example.service;

import jakarta.transaction.Transactional;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.example.cache.CacheService;
import org.example.model.City;
import org.example.model.Country;
import org.example.repository.CityRepository;
import org.example.repository.CountryRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CityService {

    private final CityRepository cityRepository;

    private final CountryRepository countryRepository;

    private final CacheService cacheService;

    private static final String ALL_CITIES_BY_COUNTRY_ID = "allCitiesByCountryId_";
    private static final String ALL_CITIES = "allCities";

    private void updateCache(Country country) {

        if (cacheService.containsKey(ALL_CITIES_BY_COUNTRY_ID + country.getId())) {
            cacheService.put(ALL_CITIES_BY_COUNTRY_ID + country.getId(), country.getCities());
        }
        if (cacheService.containsKey("allCountries")) {
            cacheService.remove("allCountries");
        }

        if (cacheService.containsKey("countryId_" + country.getId())) {
            cacheService.put("countryId_" + country.getId(), country);
        }
    }

    public List<City> getCities() {
        if (cacheService.containsKey(ALL_CITIES)) {
            return (List<City>) cacheService.get(ALL_CITIES);
        } else {
            List<City> cities = cityRepository.findAll();
            cacheService.put(ALL_CITIES, cities);
            return cities;
        }
    }

    public Set<City> getCitiesByCountryId(Long countryId) {

        if (cacheService.containsKey(ALL_CITIES_BY_COUNTRY_ID + countryId)) {
            return (Set<City>) cacheService.get(ALL_CITIES_BY_COUNTRY_ID + countryId);
        } else {
            Country country = countryRepository.findCountryWithCitiesById(countryId)
                    .orElseThrow(() -> new IllegalStateException(
                            "country with id " + countryId + " does not exist,"
                                    + " that's why you can't view cities from its"));
            Set<City> cities = country.getCities();
            cacheService.put(ALL_CITIES_BY_COUNTRY_ID + countryId, cities);
            return cities;
        }
    }

    @Transactional
    public void addNewCityByCountryId(Long countryId, City cityRequest) {

        Country country = countryRepository.findCountryWithCitiesById(countryId)
                .orElseThrow(() -> new IllegalStateException(
                        "country, which id " + countryId
                                + " does not exist, that's why you can't add new city"));

        if (country.getCities().stream().noneMatch(city -> city.getName()
                .equals(cityRequest.getName()))) {
            country.getCities().add(cityRequest);
            cityRepository.save(cityRequest);
            countryRepository.save(country);
        } else {
            throw new IllegalStateException("city with name " + cityRequest.getName()
                    + " already exists in the country " + country.getName() + ".");
        }

        if (cacheService.containsKey(ALL_CITIES)) {
            List<City> allCities = (List<City>) cacheService.get(ALL_CITIES);
            allCities.add(cityRequest);
            cacheService.put(ALL_CITIES, allCities);
        }

        updateCache(country);
    }

    @Transactional
    public void deleteCitiesByCountryId(Long countryId) {
        Country country = countryRepository.findCountryWithCitiesById(countryId)
                .orElseThrow(() -> new IllegalStateException(
                        "country, which id " + countryId
                                + " does not exist, that's why you can't delete cities from its"));

        final Set<City> citiesBeforeChanges = new HashSet<>(country.getCities());
        Set<City> cities = country.getCities();

        for (City city : cities) {
            cityRepository.deleteById(city.getId());
        }

        country.getCities().clear();
        countryRepository.save(country);

        if (cacheService.containsKey(ALL_CITIES)) {
            List<City> allCities = (List<City>) cacheService.get(ALL_CITIES);
            for (City city : citiesBeforeChanges) {
                allCities.remove(city);
            }
            cacheService.put(ALL_CITIES, allCities);
        }

        updateCache(country);
    }

    @Transactional
    public void deleteCityByIdFromCountryByCountryId(Long countryId, Long cityId) {
        Country country = countryRepository.findCountryWithCitiesById(countryId)
                .orElseThrow(() -> new IllegalStateException(
                        "country with id " + countryId
                                + " does not exist, that's why you can't delete city from its"));

        City city = cityRepository.findById(cityId)
                .orElseThrow(() -> new IllegalStateException(
                        "city with id " + countryId
                                + " does not exist, that's why you can't delete its"));

        cityRepository.deleteById(city.getId());

        country.getCities().remove(city);
        countryRepository.save(country);

        if (cacheService.containsKey(ALL_CITIES)) {
            List<City> allCities = (List<City>) cacheService.get(ALL_CITIES);
            allCities.remove(city);
            cacheService.put(ALL_CITIES, allCities);
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
                        "country with city , which id "
                                + cityId + " can not be updated, because it does not exist"));

        City cityBeforeChanges = new City();
        BeanUtils.copyProperties(city, cityBeforeChanges);

        Set<City> cities = country.getCities();

        if (name != null && !name.isEmpty() && !Objects.equals(city.getName(), name)) {
            for (City cityTemp : cities) {
                if (Objects.equals(cityTemp.getName(), name)) {
                    throw new IllegalStateException(
                            "In this country city with this name exists");
                }
            }
            city.setName(name);
        }

        if (population != null && population > 0) {
            city.setPopulation(population);
        }

        if (areaSquareKm != null && areaSquareKm > 0) {
            city.setAreaSquareKm(areaSquareKm);
        }

        if (cacheService.containsKey(ALL_CITIES)) {
            List<City> allCities = (List<City>) cacheService.get(ALL_CITIES);
            allCities.remove(cityBeforeChanges);
            allCities.add(city);
            cacheService.put(ALL_CITIES, allCities);
        }

        updateCache(country);
    }
}