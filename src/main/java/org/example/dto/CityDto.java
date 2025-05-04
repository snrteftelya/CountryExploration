package org.example.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.example.model.City;

@Data
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class CityDto {
    private Long id;
    private String name;
    private Double population;
    private Double areaSquareKm;

    @JsonIgnoreProperties({"cities", "nations"})
    private CountryDto country;

    @SuppressWarnings("checkstyle:LocalVariableName")
    public static CityDto fromEntity(City city) {
        CityDto dto = new CityDto();
        dto.setId(city.getId());
        dto.setName(city.getName());
        dto.setPopulation(city.getPopulation());
        dto.setAreaSquareKm(city.getAreaSquareKm());
        if (city.getCountry() != null) {
            dto.setCountry(CountryDto.fromEntity(city.getCountry()));
        }
        return dto;
    }
}