package org.example.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "city")
public class City {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "population")
    private Double population;

    @Column(name = "area")
    private Double areaSquareKm;
}
