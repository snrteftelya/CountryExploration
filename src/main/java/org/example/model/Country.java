package org.example.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.Set;
import lombok.Data;

@Data
@Entity
@Table(name = "country")
public class Country {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "capital")
    private String capital;

    @Column(name = "population")
    private Double population;

    @Column(name = "area")
    private Double areaSquareKm;

    @Column(name = "gdp")
    private Double gdp;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "country_id")
    private Set<City> cities;


    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "country_nations",
            joinColumns = {@JoinColumn(name = "country_id")},
            inverseJoinColumns = {@JoinColumn(name = "nation_id")})
    private Set<Nation> nations;
}