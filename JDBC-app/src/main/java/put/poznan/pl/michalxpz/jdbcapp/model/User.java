package put.poznan.pl.michalxpz.jdbcapp.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class User {
    private Long id;
    private String name;
    private String email;

    public User(long id, String name, String email) {
    }
}
