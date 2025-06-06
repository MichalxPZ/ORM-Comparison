package put.poznan.pl.michalxpz.jdbcapp.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import put.poznan.pl.michalxpz.jdbcapp.model.OrderItem;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

@Repository
public class OrderItemRepository {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<OrderItem> findByOrderId(Long orderId) {
        String sql = "SELECT * FROM order_items WHERE order_id = ?";
        return jdbcTemplate.query(sql, new Object[]{orderId}, (rs, rowNum) -> {
            OrderItem item = new OrderItem();
            item.setId(rs.getLong("id"));
            item.setOrderId(rs.getLong("order_id"));
            item.setProductId(rs.getLong("product_id"));
            item.setQuantity(rs.getInt("quantity"));
            return item;
        });
    }

    public void insertBatch(List<OrderItem> items) {
        String sql = "INSERT INTO order_items(order_id, product_id, quantity) VALUES(?,?,?)";
        jdbcTemplate.execute((ConnectionCallback<Void>) conn -> {
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                int batchSize = 500;
                int count = 0;

                for (OrderItem item : items) {
                    ps.setLong(1, item.getOrderId());
                    ps.setLong(2, item.getProductId());
                    ps.setInt(3, item.getQuantity());
                    ps.addBatch();

                    count++;
                    if (count % batchSize == 0) {
                        ps.executeBatch();  // wysyłka partii
                    }
                }
                ps.executeBatch();  // ostatnia partia (jeśli coś zostało)
            }
            return null;
        });
    }

    public Long insert(OrderItem item) {
        String sql = "INSERT INTO order_items(order_id, product_id, quantity) VALUES(?,?,?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, new String[]{"id"});
            ps.setLong(1, item.getOrderId());
            ps.setLong(2, item.getProductId());
            ps.setInt(3, item.getQuantity());
            return ps;
        }, keyHolder);
        Long newId = keyHolder.getKey().longValue();
        item.setId(newId);
        return newId;
    }
}