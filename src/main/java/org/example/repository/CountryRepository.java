package org.example.repository;

import java.util.List;
import java.util.Optional;
import org.example.model.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface CountryRepository extends JpaRepository<Country, Long> {

    @Query(value = "SELECT * FROM Country WHERE name = ?1", nativeQuery = true)
    Optional<Country> findCountryByName(String name);

    @Query("SELECT DISTINCT c FROM Country c LEFT JOIN FETCH c.cities city"
            + " WHERE :cityId IN (SELECT ct.id "
            + "FROM Country c2 JOIN c2.cities ct WHERE c2 = c)")
    Optional<Country> findCountryWithCitiesByCityId(
            @Param("cityId") Long cityId);

    @Query("SELECT DISTINCT c FROM Country c "
            + "LEFT JOIN FETCH c.nations LEFT JOIN FETCH c.cities")
    List<Country> findAllWithCitiesAndNations();

    @Query("SELECT DISTINCT c FROM Country c LEFT JOIN FETCH c.cities")
    List<Country> findAllWithCities();

    @Query("SELECT DISTINCT c FROM Country c LEFT JOIN FETCH "
            + "c.nations LEFT JOIN FETCH c.cities WHERE c.id = :id")
    Optional<Country> findCountryWithCitiesAndNationsById(@Param("id") Long id);

    @Query("SELECT DISTINCT c FROM Country c "
            + "LEFT JOIN FETCH c.nations WHERE c.id = :id")
    Optional<Country> findCountryWithNationsById(@Param("id") Long id);

    @Query("SELECT c FROM Country c LEFT JOIN FETCH c.cities WHERE c.id = :id")
    Optional<Country> findCountryWithCitiesById(@Param("id") Long id);

    @Query("SELECT DISTINCT c FROM Country c LEFT JOIN FETCH c.nations n "
            + "WHERE EXISTS (SELECT 1 FROM Nation n2 "
            + "WHERE n2.id = :nationId AND n2 MEMBER OF c.nations)")
    List<Country> findCountriesWithNationsByNationByNationId(
            @Param("nationId") Long nationId);

    @Query("SELECT DISTINCT c FROM Country c JOIN c.cities city WHERE LOWER(city.name)"
            + " LIKE LOWER(CONCAT('%', :cityName, '%'))")
    List<Country> findCountriesByCityName(@Param("cityName") String cityName);
}
