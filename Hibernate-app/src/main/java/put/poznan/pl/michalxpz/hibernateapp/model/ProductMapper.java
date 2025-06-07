package put.poznan.pl.michalxpz.hibernateapp.model;

import java.util.Set;
import java.util.stream.Collectors;

public class ProductMapper {

    public static ProductDto toDto(Product product) {
        return new ProductDto(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock(),
                product.getCategory() != null ? product.getCategory().getId() : null,
                product.getTags() != null
                        ? product.getTags().stream().map(Tag::getId).collect(Collectors.toSet())
                        : Set.of()
        );
    }
}

