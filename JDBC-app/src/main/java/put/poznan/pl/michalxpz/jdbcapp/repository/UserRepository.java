package put.poznan.pl.michalxpz.jdbcapp.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import put.poznan.pl.michalxpz.jdbcapp.model.User;

import java.sql.PreparedStatement;

@Repository
public class UserRepository {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public User findById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        return jdbcTemplate.queryForObject(
                sql,
                (rs, rowNum) -> new User(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getString("email")
                ),
                id
        );
    }

    public User findRandomUser() {
        String sql = "SELECT * FROM users ORDER BY RANDOM() LIMIT 1";
        return jdbcTemplate.queryForObject(sql, (rs, rowNum) ->
                new User(rs.getLong("id"), rs.getString("name"), rs.getString("email"))
        );
    }

    public Long insert(User user) {
        String sql = "INSERT INTO users(name, email) VALUES(?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            return ps;
        }, keyHolder);
        Long newId = keyHolder.getKey().longValue();
        user.setId(newId);
        return newId;
    }
}