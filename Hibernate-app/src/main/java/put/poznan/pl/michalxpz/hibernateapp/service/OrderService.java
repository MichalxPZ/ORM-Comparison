package put.poznan.pl.michalxpz.hibernateapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import put.poznan.pl.michalxpz.hibernateapp.model.*;
import put.poznan.pl.michalxpz.hibernateapp.repository.OrderRepository;
import put.poznan.pl.michalxpz.hibernateapp.repository.ProductRepository;
import put.poznan.pl.michalxpz.hibernateapp.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ProductRepository productRepository;

    /** Scenariusz 1: Odczyt zamówienia z produktami */
    @Transactional(readOnly = true)
    public Order getOrderWithItems(Integer orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
    }

    /** Scenariusz 3: Utworzenie zamówienia z batch-em produktów */
    @Transactional
    public Order createOrderWithItemsBatch(int itemCount) {
        User user = userRepository.findAll().stream()
                .findAny()
                .orElseThrow(() -> new IllegalStateException("No users found"));

        List<Product> allProducts = productRepository.findAll();
        if (allProducts.isEmpty()) throw new IllegalStateException("No products found");

        Order order = new Order();
        order.setOrderDate(LocalDateTime.now());
        order.setUser(user);

        Set<Product> orderProducts = new HashSet<>();
        for (int i = 0; i < itemCount; i++) {
            orderProducts.add(allProducts.get(i % allProducts.size()));
        }

        order.setProducts(orderProducts);
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
        order.setProducts(new HashSet<>(products));

        return orderRepository.save(order);
    }
}


