package put.poznan.pl.michalxpz.hibernateapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import put.poznan.pl.michalxpz.hibernateapp.model.*;
import put.poznan.pl.michalxpz.hibernateapp.repository.CategoryRepository;
import put.poznan.pl.michalxpz.hibernateapp.repository.OrderRepository;
import put.poznan.pl.michalxpz.hibernateapp.repository.ProductRepository;
import put.poznan.pl.michalxpz.hibernateapp.repository.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    @Autowired private OrderRepository orderRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Value("${DB:postgresql}") private String db;

    /** Scenariusz 1: Odczyt zamówienia z produktami */
    @Transactional(readOnly = true)
    public Order getOrderWithItems(Integer orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
    }

    /** Scenariusz 3: Utworzenie zamówienia z batch-em produktów */
    @Transactional
    public Order createOrderWithItemsBatch(int itemCount) {
        User randomUser = db.equalsIgnoreCase("postgresql")
                ? userRepository.findRandomUserPostgres()
                : userRepository.findRandomUserMySQL();
        if (randomUser == null) {
            throw new IllegalStateException("Brak użytkowników w bazie!");
        }
        Product product = new Product();
        product.setName("NewProduct");
        product.setDescription("Batch-insert product");
        product.setPrice(new BigDecimal("100.00"));
        product.setStock(100);
        product.setCategory(categoryRepository.findById(1).orElseThrow());

        productRepository.save(product);

        Order order = new Order();
        order.setOrderDate(LocalDateTime.now());
        order.setUser(randomUser);

        List<Product> productList = new ArrayList<>(itemCount);
        for (int i = 0; i < itemCount; i++) {
            productList.add(product);
        }

        order.setProducts(productList);
        orderRepository.save(order);
        return orderRepository.save(order);
    }

    /** Scenariusz 5: Transakcja: zakup wielu produktów przez użytkownika */
    @Transactional
    public Order placeOrder(Integer userId, List<Integer> productIds) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<Product> products = productRepository.findAllById(productIds);
        if (products.size() != productIds.size()) {
            throw new IllegalArgumentException("Some products not found");
        }

        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setProducts(new ArrayList<>(products));

        return orderRepository.save(order);
    }
}


