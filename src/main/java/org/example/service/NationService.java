package org.example.service;

import org.example.model.Country;
import org.example.model.Nation;
import org.example.repository.CountryRepository;
import org.example.repository.NationRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@AllArgsConstructor
@Service
public class NationService {

    private final NationRepository nationRepository;

    private final CountryRepository countryRepository;

    public List<Nation> getNationsByCountryId(Long countryId) {
        Country country = countryRepository.findById(countryId)
                .orElseThrow(() -> new IllegalStateException(
                        "country with id " + countryId + " does not exist, that's why you can't view nations from its"));
        return country.getNations();
    }

    public List<Nation> getNations() {
        return nationRepository.findAll();
    }

    public List<Country> getCountriesByNationId(Long nationId) {
        Nation nation = nationRepository.findById(nationId)
                .orElseThrow(() -> new IllegalStateException(
                        "nation, which id " + nationId + " does not exist, that's why you can't view countries from its"));
        return nation.getCountries();
    }

    public void addNewNationByCountryId(Long countryId, Nation nationRequest) {

        Country country = countryRepository.findById(countryId)
                .orElseThrow(() -> new IllegalStateException(
                        "country, which id " + countryId + " does not exist, that's why you can't add nation to its"));

        Nation nation = nationRepository.findNationsByName(nationRequest.getName());

        if (country.getNations().stream().noneMatch(nationFunc -> nationFunc.getName().equals(nationRequest.getName()))) {
            if (nation != null) {
                nationRepository.save(nation);
                country.getNations().add(nation);
                countryRepository.save(country);
            } else {
                nationRepository.save(nationRequest);
                country.getNations().add(nationRequest);
                countryRepository.save(country);
            }
        } else {
            throw new IllegalStateException("nation with name " + nationRequest.getName() + " already exists in the country " + country.getName() + ".");
        }

    }

    @Transactional
    public void updateNation(Long nationId,
                             String name,
                             String language,
                             String religion) {
        Nation nation = nationRepository.findById(nationId)
                .orElseThrow(() -> new IllegalStateException(
                        "nation with id " + nationId + " does not exist, that's why you can't update this"));

        if (name != null && !name.isEmpty() && !Objects.equals(nation.getName(), name)) {
            Optional<Nation> nationOptional = Optional.ofNullable(nationRepository.findNationsByName(name));
            if (nationOptional.isPresent()) {
                throw new IllegalStateException("nation with this name exists");
            }
            nation.setName(name);
        }

        if (language != null && !language.isEmpty() && !Objects.equals(nation.getLanguage(), language)) {
            nation.setLanguage(language);
        }

        if (religion != null && !religion.isEmpty() && !Objects.equals(nation.getReligion(), religion)) {
            nation.setReligion(religion);
        }
    }

    public void deleteNation(Long nationId) {

        Nation nation = nationRepository.findById(nationId)
                .orElseThrow(() -> new IllegalStateException(
                        "nation, which id " + nationId + " does not exist, that's why you can't delete its"));

        List<Country> countries = nation.getCountries();

        for (Country country : countries) {
            country.getNations().remove(nation);
            countryRepository.save(country);
        }

        nationRepository.delete(nation);
    }

    public void deleteNationFromCountry(Long countryId, Long nationId) {

        Country country = countryRepository.findById(countryId)
                .orElseThrow(() -> new IllegalStateException(
                        "country with id " + countryId + " doesn't exist, that's why you can't delete its"));

        Nation nation = nationRepository.findById(nationId)
                .orElseThrow(() -> new IllegalStateException(
                        "nation with id " + nationId + " does not exist, that's why you can't delete its"));

        country.getNations().remove(nation);
        countryRepository.save(country);
    }
}
