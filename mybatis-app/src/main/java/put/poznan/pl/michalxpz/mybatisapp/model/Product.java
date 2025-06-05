package put.poznan.pl.michalxpz.mybatisapp.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.ibatis.mapping.FetchType;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Product {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private int stock;
    private Category category;
    private Set<Tag> tags = new HashSet<>();
    private Set<Order> orders = new HashSet<>();

}
