package org.example.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.example.model.Country;

import java.util.Set;

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

    public static CountryDto fromEntity(Country country) {
        CountryDto Dto = new CountryDto();
        Dto.setId(country.getId());
        Dto.setName(country.getName());
        Dto.setCapital(country.getCapital());
        Dto.setPopulation(country.getPopulation());
        Dto.setAreaSquareKm(country.getAreaSquareKm());
        Dto.setGdp(country.getGdp());
        return Dto;
    }
}