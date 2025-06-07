package put.poznan.pl.michalxpz.hibernateapp.model;

import java.util.Set;
import java.util.stream.Collectors;

public class OrderMapper {

    public static OrderDto toDto(Order order) {
        return new OrderDto(
                order.getId(),
                order.getOrderDate(),
                order.getUser() != null ? order.getUser().getId() : null,
                order.getProducts() != null
                        ? order.getProducts().stream().map(Product::getId).collect(Collectors.toSet())
                        : Set.of()
        );
    }
}
