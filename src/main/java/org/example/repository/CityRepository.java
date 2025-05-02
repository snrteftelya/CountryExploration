package org.example.repository;

import java.util.List;
import org.example.model.City;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CityRepository extends JpaRepository<City, Long> {
    List<City> findByCountryId(Long countryId);

    @Query("SELECT c FROM City c WHERE "
            + "(:name IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND "
            + "(:countryId IS NULL OR c.country.id = :countryId)")
    List<City> searchCities(@Param("name") String name, @Param("countryId") Long countryId);
}
