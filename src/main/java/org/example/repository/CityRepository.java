package org.example.repository;

import org.example.model.City;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface CityRepository extends JpaRepository<City, Long> {
    @Transactional
    @Modifying
    void deleteAllByCountryId(Long countryId); // Changed to use underscore notation
}
