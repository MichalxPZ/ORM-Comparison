package put.poznan.pl.michalxpz.hibernateapp.model;


import java.math.BigDecimal;
import java.util.Set;

public record ProductDto(
        Integer id,
        String name,
        String description,
        BigDecimal price,
        int stock,
        Integer categoryId,
        Set<Integer> tagIds
) {}
