package org.example.repository;

import org.example.model.Nation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NationRepository extends JpaRepository<Nation, Long> {
    Nation findNationsByName(String name);
}
