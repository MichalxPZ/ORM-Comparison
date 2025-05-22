package put.poznan.pl.michalxpz.jdbcapp.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import put.poznan.pl.michalxpz.jdbcapp.model.OrderItem;

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
        // Przygotowanie listy parametrów dla batch (każdy element listy to tablica param)
        List<Object[]> batchArgs = new ArrayList<>();
        for (OrderItem item : items) {
            batchArgs.add(new Object[]{
                    item.getOrderId(), item.getProductId(), item.getQuantity()
            });
        }
        jdbcTemplate.batchUpdate(sql, batchArgs);
    }

    public void insert(OrderItem item) {
        String sql = "INSERT INTO order_items(order_id, product_id, quantity) VALUES(?,?,?)";
        jdbcTemplate.update(sql, item.getOrderId(), item.getProductId(), item.getQuantity());
    }
}
