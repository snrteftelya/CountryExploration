package org.example.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity
@Table(name = "nation")
public class Nation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Hidden
    private Long id;

    @Column(name = "name")
    @Schema(example = "Belarusian")
    private String name;

    @Column(name = "language")
    @Schema(example = "Belarusian")
    private String language;

    @Column(name = "religion")
    @Schema(example = "Christian")
    private String religion;

    @ManyToMany(mappedBy = "nations")
    @JsonIgnore
    private List<Country> countries;
}
