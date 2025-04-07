package org.example.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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

    @ManyToOne // Establishes a many-to-one relationship
    @JoinColumn(name = "country_id", nullable = false) // Foreign key to Country
    private Country country; // Link to the Country entity
}
