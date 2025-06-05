package put.poznan.pl.michalxpz.jdbcapp.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import put.poznan.pl.michalxpz.jdbcapp.model.Product;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ProductRepository {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public Long insert(Product p) {
        String sql = """
            INSERT INTO products(category_id, name, description, price, stock)
            VALUES (?, ?, ?, ?, ?)
            """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, new String[]{"id"});
            ps.setLong(1, p.getCategoryId());
            ps.setString(2, p.getName());
            ps.setString(3, p.getDescription());
            ps.setBigDecimal(4, p.getPrice());
            ps.setInt(5, p.getStock());
            return ps;
        }, keyHolder);
        Long newId = keyHolder.getKey().longValue();
        p.setId(newId);
        return newId;
    }

    public Product findById(Long id) {
        String sql = "SELECT * FROM products WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
            Product product = new Product();
            product.setId(rs.getLong("id"));
            product.setCategoryId(rs.getLong("category_id"));
            product.setName(rs.getString("name"));
            product.setDescription(rs.getString("description"));
            product.setPrice(rs.getBigDecimal("price"));
            product.setStock(rs.getInt("stock"));
            return product;
        }, id);
    }

    public int updateStock(Long id, int newStock) {
        String sql = "UPDATE products SET stock = ? WHERE id = ?";
        return jdbcTemplate.update(sql, newStock, id);
    }

    public List<Product> findByCriteria(Long categoryId, BigDecimal minPrice,
                                        BigDecimal maxPrice, String keyword) {
        StringBuilder sql = new StringBuilder("SELECT * FROM products WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if (categoryId != null) {
            sql.append(" AND category_id = ?");
            params.add(categoryId);
        }
        if (minPrice != null) {
            sql.append(" AND price >= ?");
            params.add(minPrice);
        }
        if (maxPrice != null) {
            sql.append(" AND price <= ?");
            params.add(maxPrice);
        }
        if (keyword != null && !keyword.isEmpty()) {
            sql.append(" AND description ILIKE ?");
            params.add("%" + keyword + "%");
        }
        sql.append(" ORDER BY price ASC");
        return jdbcTemplate.query(sql.toString(), params.toArray(), (rs, rowNum) -> {
            Product product = new Product();
            product.setId(rs.getLong("id"));
            product.setCategoryId(rs.getLong("category_id"));
            product.setName(rs.getString("name"));
            product.setDescription(rs.getString("description"));
            product.setPrice(rs.getBigDecimal("price"));
            product.setStock(rs.getInt("stock"));
            return product;
        });
    }

    public int updatePrices(BigDecimal percent) {
        // Example: percent = 10 means increase prices by +10%
        BigDecimal factor = BigDecimal.ONE.add(percent.divide(new BigDecimal("100")));
        String sql = "UPDATE products SET price = price * ?";
        return jdbcTemplate.update(sql, factor);
    }
}