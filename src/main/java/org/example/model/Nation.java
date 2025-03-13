package org.example.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity
@Table(name = "nation")
public class Nation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "language")
    private String language;

    @Column(name = "religion")
    private String religion;

    @ManyToMany(mappedBy = "nations")
    @JsonIgnore
    private List<Country> countries;
}
