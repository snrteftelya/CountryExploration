package org.example.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

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

    @OneToMany(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "country_id")
    private List<City> cities;


    @ManyToMany
    @JoinTable(name = "country_nations",
            joinColumns = {@JoinColumn(name = "country_id")},
            inverseJoinColumns = {@JoinColumn(name = "nation_id")})
    private List<Nation> nations;


}
