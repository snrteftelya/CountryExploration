package org.example.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Set;
import lombok.Data;
import org.example.model.Country;

@Data
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class CountryDto {
    private Long id;
    private String name;
    private String capital;
    private Double population;
    private Double areaSquareKm;
    private Double gdp;
    private Set<Long> cityIds;

    @SuppressWarnings("checkstyle:LocalVariableName")
    public static CountryDto fromEntity(Country country) {
        CountryDto dto = new CountryDto();
        dto.setId(country.getId());
        dto.setName(country.getName());
        dto.setCapital(country.getCapital());
        dto.setPopulation(country.getPopulation());
        dto.setAreaSquareKm(country.getAreaSquareKm());
        dto.setGdp(country.getGdp());
        return dto;
    }
}