package put.poznan.pl.michalxpz.jdbcapp.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import put.poznan.pl.michalxpz.jdbcapp.model.User;

@Repository
public class UserRepository {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public User findById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        return jdbcTemplate.queryForObject(sql,
                (rs, rowNum) -> new User(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getString("email")));
    }

    public User findRandomUser() {
        String sql = "SELECT * FROM users ORDER BY RANDOM() LIMIT 1";
        return jdbcTemplate.queryForObject(sql, (rs, rowNum) ->
                new User(rs.getLong("id"), rs.getString("name"), rs.getString("email")));
    }
}
