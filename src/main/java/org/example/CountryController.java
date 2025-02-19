package org.example;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/countries")
public class CountryController {
    private final List<Country> countries = new ArrayList<>();

    public CountryController() {
        countries.add(new Country(1L, "USA", "Washington", 331_000_000));
        countries.add(new Country(2L, "Canada", "Ottawa", 38_000_000));
        countries.add(new Country(3L, "Germany", "Berlin", 83_000_000));
    }

    @GetMapping
    public ResponseEntity<List<Country>> getCountries(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String capital) {

        List<Country> filtered = countries.stream()
                .filter(c -> (name == null || c.getName().equalsIgnoreCase(name)) &&
                        (capital == null || c.getCapital().equalsIgnoreCase(capital)))
                .collect(Collectors.toList());

        return ResponseEntity.ok(filtered);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Country> getCountryById(@PathVariable Long id) {
        Optional<Country> country = countries.stream()
                .filter(c -> c.getId().equals(id))
                .findFirst();

        return country.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
