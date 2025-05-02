package org.example.repository;

import java.util.Optional;
import org.example.model.Nation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NationRepository extends JpaRepository<Nation, Long> {

    Nation findNationByName(String name);

    @Query("SELECT n FROM Nation n LEFT JOIN FETCH n.countries c "
            + "LEFT JOIN FETCH c.cities "
            + "LEFT JOIN FETCH c.nations WHERE n.id = :id")
    Optional<Nation> findByIdWithCountriesWithCities(@Param("id") Long id);

    @Query("SELECT n FROM Nation n "
            + "LEFT JOIN FETCH n.countries WHERE n.id = :id")
    Optional<Nation> findByIdWithCountries(@Param("id") Long id);
}
