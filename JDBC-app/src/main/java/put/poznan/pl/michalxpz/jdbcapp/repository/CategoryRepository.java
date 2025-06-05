package put.poznan.pl.michalxpz.jdbcapp.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import put.poznan.pl.michalxpz.jdbcapp.model.Category;

import java.sql.PreparedStatement;

@Repository
public class CategoryRepository {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public Long insert(Category category) {
        String sql = "INSERT INTO categories(name) VALUES(?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, category.getName());
            return ps;
        }, keyHolder);
        Long newId = keyHolder.getKey().longValue();
        category.setId(newId);
        return newId;
    }

    // (Additional CRUD methods like findById, findAll can be added if needed)
}
