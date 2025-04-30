package org.example.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.example.dto.CountryDto;
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

    public static CityDto fromEntity(City city) {
        CityDto Dto = new CityDto();
        Dto.setId(city.getId());
        Dto.setName(city.getName());
        Dto.setPopulation(city.getPopulation());
        Dto.setAreaSquareKm(city.getAreaSquareKm());
        Dto.setCountry(CountryDto.fromEntity(city.getCountry()));
        return Dto;
    }
}