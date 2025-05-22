package put.poznan.pl.michalxpz.jdbcapp.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import put.poznan.pl.michalxpz.jdbcapp.model.Order;

import java.sql.PreparedStatement;
import java.sql.Timestamp;

@Repository
public class OrderRepository {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public Order findById(Long orderId) {
        String sql = "SELECT * FROM orders WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{orderId}, (rs, rowNum) -> {
            Order order = new Order();
            order.setId(rs.getLong("id"));
            order.setUserId(rs.getLong("user_id"));
            order.setOrderDate(rs.getTimestamp("order_date").toLocalDateTime());
            return order;
        });
    }

    public Long insert(Order order) {
        // Wstawienie zamówienia i uzyskanie wygenerowanego klucza (ID)
        // (Przyjmujemy, że baza generuje ID, np. SERIAL; używamy KeyHolder aby pobrać wygenerowane id)
        String sql = "INSERT INTO orders(user_id, order_date) VALUES(?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, new String[]{"id"});
            ps.setLong(1, order.getUserId());
            ps.setTimestamp(2, Timestamp.valueOf(order.getOrderDate()));
            return ps;
        }, keyHolder);
        Long newId = keyHolder.getKey().longValue();
        order.setId(newId);
        return newId;
    }
}
