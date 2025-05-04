package org.example.repository;

import java.util.List;
import java.util.Optional;
import org.example.model.City;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CityRepository extends JpaRepository<City, Long> {
    @Query("SELECT c FROM City c LEFT JOIN FETCH c.country WHERE c.country.id = :countryId")
    List<City> findByCountryId(@Param("countryId") Long countryId);

    @Query("SELECT c FROM City c LEFT JOIN FETCH c.country WHERE c.id = :id")
    Optional<City> findByIdWithCountry(@Param("id") Long id);
}
