package put.poznan.pl.michalxpz.jdbcapp.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class Category {
    private Long id;
    private String name;
}