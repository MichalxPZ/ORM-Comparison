package put.poznan.pl.michalxpz.mybatisapp.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Order {
    private Long id;
    private LocalDateTime orderDate;
    private User user;
    private Set<Product> products = new HashSet<>();
}

