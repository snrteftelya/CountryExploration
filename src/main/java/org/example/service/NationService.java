package org.example.service;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.example.cache.CacheService;
import org.example.model.Country;
import org.example.model.Nation;
import org.example.repository.CountryRepository;
import org.example.repository.NationRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@AllArgsConstructor
@Service
public class NationService {

    private final NationRepository nationRepository;

    private final CountryRepository countryRepository;

    private final CacheService cacheService;

    private static final String ALL_NATIONS_BY_COUNTRY_ID = "allNationsByCountryId_";
    private static final String ALL_COUNTRIES_BY_NATION_ID = "allCountriesByNationId_";
    private static final String ALL_NATIONS = "allNations";

    private void cleanCache(Long nationId, Nation nation) {

        List<Country> countries = countryRepository.findCountriesWithNationsByNationByNationId(nationId);

        for (Country country : countries) {
            if (cacheService.containsKey(ALL_NATIONS_BY_COUNTRY_ID + country.getId())) {
                cacheService.remove(ALL_NATIONS_BY_COUNTRY_ID + country.getId());
            }
            if (cacheService.containsKey("countryId_" + country.getId())) {
                cacheService.remove("countryId_" + country.getId());
            }
        }

        if (cacheService.containsKey(ALL_NATIONS)) {
            cacheService.remove(ALL_NATIONS);
        }

        if (cacheService.containsKey(ALL_COUNTRIES_BY_NATION_ID + nation.getId())) {
            cacheService.remove(ALL_COUNTRIES_BY_NATION_ID + nation.getId());
        }

        if (cacheService.containsKey("allCountries")) {
            cacheService.remove("allCountries");
        }
    }

    public Set<Nation> getNationsByCountryId(Long countryId) {

        if (cacheService.containsKey(ALL_NATIONS_BY_COUNTRY_ID + countryId)) {
            return (Set<Nation>) cacheService.get(ALL_NATIONS_BY_COUNTRY_ID + countryId);
        } else {
            Country country = countryRepository.findCountryWithNationsById(countryId)
                    .orElseThrow(() -> new IllegalStateException(
                            "country, which id " + countryId + " does not exist, that's why you can't view nations from its"));
            Set<Nation> nations = country.getNations();
            cacheService.put(ALL_NATIONS_BY_COUNTRY_ID + countryId, nations);
            return nations;
        }
    }

    public List<Nation> getNations() {
        if (cacheService.containsKey(ALL_NATIONS)) {
            return (List<Nation>) cacheService.get(ALL_NATIONS);
        } else {
            List<Nation> nations = nationRepository.findAll();
            cacheService.put(ALL_NATIONS, nations);
            return nations;
        }
    }

    public Set<Country> getCountriesByNationId(Long nationId) {

        if (cacheService.containsKey(ALL_COUNTRIES_BY_NATION_ID + nationId)) {
            return (Set<Country>) cacheService.get(ALL_COUNTRIES_BY_NATION_ID + nationId);
        } else {
            Nation nation = nationRepository.findByIdWithCountriesWithCities(nationId)
                    .orElseThrow(() -> new IllegalStateException(
                            "nation, which id " + nationId + " does not exist, that's why you can't view countries from its"));
            Set<Country> countries = new HashSet<>(nation.getCountries());
            cacheService.put(ALL_COUNTRIES_BY_NATION_ID + nationId, countries);
            return countries;
        }
    }

    public void addNewNationByCountryId(Long countryId, Nation nationRequest) {

        Country country = countryRepository.findCountryWithNationsById(countryId)
                .orElseThrow(() -> new IllegalStateException(
                        "country, which id " + countryId + " does not exist, that's why you can't add nation to its"));

        Nation nation = nationRepository.findNationByName(nationRequest.getName());

        if (country.getNations().stream().noneMatch(nationFunc -> nationFunc.getName().equals(nationRequest.getName()))) {
            if (nation != null) {
                country.getNations().add(nation);
                countryRepository.save(country);
            } else {
                nationRepository.save(nationRequest);
                country.getNations().add(nationRequest);
                countryRepository.save(country);
                nation = nationRepository.findNationByName(nationRequest.getName());
            }
        } else {
            throw new IllegalStateException("nation with name " + nationRequest.getName() + " already exists in the country " + country.getName() + ".");
        }

        cleanCache(nation.getId(), nation);
    }

    @Transactional
    public void updateNation(Long nationId,
                             String name,
                             String language,
                             String religion) {
        Nation nation = nationRepository.findById(nationId)
                .orElseThrow(() -> new IllegalStateException(
                        "nation with id " + nationId + " does not exist, that's why you can't update this"));

        cleanCache(nationId, nation);

        if (name != null && !name.isEmpty() && !Objects.equals(nation.getName(), name)) {
            Optional<Nation> nationOptional = Optional.ofNullable(nationRepository.findNationByName(name));
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

    @Transactional
    public void deleteNation(Long nationId) {

        Nation nation = nationRepository.findByIdWithCountries(nationId)
                .orElseThrow(() -> new IllegalStateException(
                        "nation, which id " + nationId + " does not exist, that's why you can't delete its"));

        cleanCache(nationId, nation);

        List<Country> countries = countryRepository.findCountriesWithNationsByNationByNationId(nationId);

        for (Country country : countries) {
            country.getNations().remove(nation);
            countryRepository.save(country);
        }

        nationRepository.delete(nation);
    }

    @Transactional
    public void deleteNationFromCountry(Long countryId, Long nationId) {

        Country country = countryRepository.findCountryWithNationsById(countryId)
                .orElseThrow(() -> new IllegalStateException(
                        "country with id " + countryId + " doesn't exist, that's why you can't delete its"));

        Nation nation = nationRepository.findById(nationId)
                .orElseThrow(() -> new IllegalStateException(
                        "nation with id " + nationId + " does not exist, that's why you can't delete its"));

        cleanCache(nationId, nation);

        country.getNations().remove(nation);
        countryRepository.save(country);
    }
}