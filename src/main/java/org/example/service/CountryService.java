package org.example.service;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.example.model.Country;
import org.example.repository.CountryRepository;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class CountryService {

    private final CountryRepository countryRepository;

    public List<Country> getCountries() {
        return countryRepository.findAll();
    }

    public Country getCountryById(Long countryId) {
        return countryRepository.findById(countryId)
                .orElseThrow(() -> new IllegalStateException(
                        "country with id " + countryId + "does not exist"));
    }

    public void addNewCountry(Country country) {
        Optional<Country> countryOptional = countryRepository
                .findCountryByName(country.getName());
        if (countryOptional.isPresent()) {
            throw new IllegalStateException("country exists");
        }
        countryRepository.save(country);
    }

    public void deleteCountry(Long countryId) {
        boolean exists = countryRepository.existsById(countryId);
        if (!exists) {
            throw new IllegalStateException(
                    "country, which id " + countryId
                            + " can not be deleted, because id does not exist");
        }
        countryRepository.deleteById(countryId);
    }

    public void deleteCountries() {
        countryRepository.deleteAll();
    }

    @Transactional
    public void updateCountry(Long countryId,
                              String name,
                              String capital,
                              Double population,
                              Double areaSquareKm,
                              Double gdp) {
        Country country = countryRepository.findById(countryId)
                .orElseThrow(() -> new IllegalStateException(
                        "country with id " + countryId
                                + "can not be updated, because it does not exist"));

        if (name != null && !name.isEmpty() && !Objects.equals(country.getName(), name)) {
            Optional<Country> countryOptional = countryRepository.findCountryByName(name);
            if (countryOptional.isPresent()) {
                throw new IllegalStateException("country with this name exists");
            }
            country.setName(name);
        }

        if (capital != null && !capital.isEmpty()
                && !Objects.equals(country.getCapital(), capital)) {
            country.setCapital(capital);
        }

        if (population != null && population > 0) {
            country.setPopulation(population);
        }

        if (areaSquareKm != null && areaSquareKm > 0) {
            country.setAreaSquareKm(areaSquareKm);
        }

        if (gdp != null && gdp > 0) {
            country.setGdp(gdp);
        }
    }
}
