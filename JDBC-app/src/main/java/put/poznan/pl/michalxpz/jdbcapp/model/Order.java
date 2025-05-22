package put.poznan.pl.michalxpz.jdbcapp.model;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Order {
    private Long id;
    private Long userId;
    private LocalDateTime orderDate;
    private List<OrderItem> items;

    public Order(Long id, Long userId, LocalDateTime orderDate) {
        this.id = id;
        this.userId = userId;
        this.orderDate = orderDate;
    }
}
