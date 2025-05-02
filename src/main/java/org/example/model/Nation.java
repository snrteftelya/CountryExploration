package org.example.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.util.List;
import lombok.Data;

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
