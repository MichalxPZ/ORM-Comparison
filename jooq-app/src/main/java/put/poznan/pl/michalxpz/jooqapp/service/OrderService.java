package put.poznan.pl.michalxpz.jooqapp.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import put.poznan.pl.michalxpz.generated.tables.pojos.Orders;
import put.poznan.pl.michalxpz.generated.tables.pojos.Products;
import put.poznan.pl.michalxpz.generated.tables.pojos.Users;
import put.poznan.pl.michalxpz.jooqapp.model.OrderWithItems;
import put.poznan.pl.michalxpz.jooqapp.repository.OrderRepository;
import put.poznan.pl.michalxpz.jooqapp.repository.ProductRepository;
import put.poznan.pl.michalxpz.jooqapp.repository.UserRepository;
import put.poznan.pl.michalxpz.generated.tables.records.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class OrderService {
    @Autowired private OrderRepository orderRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ProductRepository productRepository;

    /** Scenario 1: Fetch order with its products. */
    @Transactional(readOnly = true)
    public OrderWithItems getOrderWithItems(Long orderId) {
        // Fetch order record and its products
        Orders orderRec = orderRepository.findById(orderId);
        List<Products> products = orderRepository.findProductsByOrderId(orderId);

        // Map to our DTO-like object
        OrderWithItems orderWithItems = new OrderWithItems();
        orderWithItems.setId(Long.valueOf(orderRec.id()));
        orderWithItems.setOrderDate(orderRec.orderDate());
        orderWithItems.setUserId(Long.valueOf(orderRec.userId()));
        orderWithItems.setProducts(products);
        return orderWithItems;
    }

    /** Scenario 3: Create an order with a batch of items. */
    @Transactional
    public Orders createOrderWithItemsBatch(int itemCount) {
        // Pick any user
        List<Users> users = userRepository.findAll();
        if (users.isEmpty()) throw new IllegalStateException("No users found");
        Integer userId = Math.toIntExact(users.get(0).id());

        // Get all products to cycle through
        List<Products> allProducts = productRepository.findAll();
        if (allProducts.isEmpty()) throw new IllegalStateException("No products found");

        // Select product IDs (wrapping around the list)
        Set<Long> orderProductIds = new HashSet<>();
        for (int i = 0; i < itemCount; i++) {
            orderProductIds.add(Long.valueOf(allProducts.get(i % allProducts.size()).id()));
        }

        // Create and return the order
        return orderRepository.createOrder(Long.valueOf(userId), LocalDateTime.now(), orderProductIds);
    }

    /** Scenario 5: Place order for user with given product IDs. */
    @Transactional
    public Orders placeOrder(Long userId, List<Long> productIds) {
        // Verify user exists
        userRepository.findById(userId);

        // Verify all products exist
        List<Products> products = productRepository.findAllById(productIds);
        if (products.size() != productIds.size()) {
            throw new IllegalArgumentException("Some products not found");
        }

        // Use a Set to avoid duplicates
        Set<Long> productIdSet = new HashSet<>(productIds);
        // Create and return the order
        return orderRepository.createOrder(userId, LocalDateTime.now(), productIdSet);
    }
}

