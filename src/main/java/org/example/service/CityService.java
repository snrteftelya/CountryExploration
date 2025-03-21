package org.example.service;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.example.model.City;
import org.example.model.Country;
import org.example.repository.CityRepository;
import org.example.repository.CountryRepository;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CityService {

    private final CityRepository cityRepository;

    private final CountryRepository countryRepository;

    public List<City> getCities() {
        return cityRepository.findAll();
    }

    public List<City> getCitiesByCountryId(Long countryId) {
        Country country = countryRepository.findById(countryId)
                .orElseThrow(() -> new IllegalStateException(
                        "country with id " + countryId
                                + " does not exist, that's why you can't view cities from its"));
        return country.getCities();
    }

    public void addNewCityByCountryId(Long countryId, City cityRequest) {

        Country country = countryRepository.findById(countryId)
                .orElseThrow(() -> new IllegalStateException(
                        "country, which id " + countryId
                                + " does not exist, that's why you can't add new city"));

        if (country.getCities().stream().noneMatch(
                city -> city.getName().equals(cityRequest.getName()))) {
            country.getCities().add(cityRequest);
            cityRepository.save(cityRequest);
            countryRepository.save(country);
        } else {
            throw new IllegalStateException("city with name "
                    + cityRequest.getName()
                    + " already exists in the country " + country.getName() + ".");
        }
    }

    public void deleteCitiesByCountryId(Long countryId) {
        Country country = countryRepository.findById(countryId)
                .orElseThrow(() -> new IllegalStateException(
                        "country with id " + countryId
                                + " does not exist, that's why you can't delete cities from its"));

        country.getCities().clear();
        countryRepository.save(country);
    }

    public void deleteCityByCityId(Long cityId) {
        if (!cityRepository.existsById(cityId)) {
            throw new IllegalStateException("city with id " + cityId + " does not exists in the database.");
        }
        cityRepository.deleteById(cityId);
    }

    @Transactional
    public void updateCity(Long cityId,
                           String name,
                           Double population,
                           Double areaSquareKm) {

        City city = cityRepository.findById(cityId)
                .orElseThrow(() -> new IllegalStateException(
                        "city with id " + cityId + " does not exist"));

        if (name != null && !name.isEmpty() && !Objects.equals(city.getName(), name)) {
            Optional<City> cityOptional = cityRepository.findCityByName(name);
            if (cityOptional.isPresent()) {
                throw new IllegalStateException("city with this name exists");
            }
            city.setName(name);
        }

        if (population != null && population > 0) {
            city.setPopulation(population);
        }

        if (areaSquareKm != null && areaSquareKm > 0) {
            city.setAreaSquareKm(areaSquareKm);
        }
    }
}
