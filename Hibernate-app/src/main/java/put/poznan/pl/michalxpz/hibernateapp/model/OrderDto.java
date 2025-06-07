package put.poznan.pl.michalxpz.hibernateapp.model;

import java.time.LocalDateTime;
import java.util.Set;

public record OrderDto(
        Integer id,
        LocalDateTime orderDate,
        Integer userId,
        Set<Integer> productIds
) {}
