package put.poznan.pl.michalxpz.mybatisapp.service;

import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import put.poznan.pl.michalxpz.mybatisapp.model.*;
import put.poznan.pl.michalxpz.mybatisapp.repository.CategoryMapper;
import put.poznan.pl.michalxpz.mybatisapp.repository.OrderMapper;
import put.poznan.pl.michalxpz.mybatisapp.repository.ProductMapper;
import put.poznan.pl.michalxpz.mybatisapp.repository.UserMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Service
public class OrderService {

    @Autowired private OrderMapper orderMapper;
    @Autowired private UserMapper userMapper;
    @Autowired private ProductMapper productMapper;
    @Autowired private CategoryMapper categoryMapper;
    @Autowired private SqlSessionFactory sqlSessionFactory;
    @Value("${DB:postgresql}") private String db;

    /** Scenariusz 1: Odczyt zamówienia z produktami */
    @Transactional(readOnly = true)
    public Order getOrderWithItems(Long orderId) {
        Order order = orderMapper.findById(orderId);
        if (order == null) throw new IllegalArgumentException("Order not found");
        // Dołączenie pozycji (produkty) do zamówienia:
        List<Product> items = orderMapper.findItemsByOrderId(orderId);
        order.setProducts(new HashSet<>(items));
        return order;
    }

    /** Scenariusz 3: Utworzenie zamówienia z batch-em produktów */
    @Transactional
    public void createOrderWithItemsBatch(int itemCount) {
        try (SqlSession session = sqlSessionFactory.openSession(ExecutorType.BATCH, false)) {
            OrderMapper mapper = session.getMapper(OrderMapper.class);
        User user = userMapper.findRandomUser(db);
        if (user == null) throw new IllegalStateException("No users found");

        Product product = new  Product();
        product.setName("NewProduct");
        product.setDescription("Batch-insert product");
        product.setPrice(new BigDecimal("100.00"));
        product.setStock(100);
        product.setCategory(categoryMapper.findById(1L));

        productMapper.insert(product);
        Order order = new Order();
        order.setOrderDate(LocalDateTime.now());
        order.setUser(user);
        orderMapper.insertOrder(order);

        for (int i = 0; i < itemCount; i++) {
            List<Product> products = new ArrayList<>(itemCount);
            products.add(product);
            orderMapper.insertOrderItem(order.getId(), product.getId());
        }
        session.commit();
        }
    }

    /** Scenariusz 5: Transakcja: zakup wielu produktów przez użytkownika */
    @Transactional
    public Order placeOrder(Long userId, List<Long> productIds) {
        User user = userMapper.findById(userId);
        if (user == null) throw new IllegalArgumentException("User not found");

        List<Product> products = productMapper.findByIds(productIds);
        if (products.size() != productIds.size()) {
            throw new IllegalArgumentException("Some products not found");
        }

        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setProducts(new HashSet<>(products));

        orderMapper.insertOrder(order);
        for (Product p : products) {
            orderMapper.insertOrderItem(order.getId(), p.getId());
        }
        return order;
    }
}

