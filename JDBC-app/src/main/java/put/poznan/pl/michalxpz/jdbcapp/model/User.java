package put.poznan.pl.michalxpz.jdbcapp.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class User {
    private Long id;
    private String name;
    private String email;
}