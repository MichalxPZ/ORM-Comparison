package put.poznan.pl.michalxpz.jdbcapp.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ProductTag {
    private Long productId;
    private Long tagId;
}