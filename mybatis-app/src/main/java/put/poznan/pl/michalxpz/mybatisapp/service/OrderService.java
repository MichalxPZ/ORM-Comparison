package put.poznan.pl.michalxpz.mybatisapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import put.poznan.pl.michalxpz.mybatisapp.model.*;
import put.poznan.pl.michalxpz.mybatisapp.repository.OrderMapper;
import put.poznan.pl.michalxpz.mybatisapp.repository.ProductMapper;
import put.poznan.pl.michalxpz.mybatisapp.repository.UserMapper;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class OrderService {

    @Autowired private OrderMapper orderMapper;
    @Autowired private UserMapper userMapper;
    @Autowired private ProductMapper productMapper;

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
    public Order createOrderWithItemsBatch(int itemCount) {
        // Pobierz dowolnego użytkownika:
        List<User> users = userMapper.findAll();
        if (users.isEmpty()) throw new IllegalStateException("No users found");
        User user = users.get(0);

        List<Product> allProducts = productMapper.findAll();
        if (allProducts.isEmpty()) throw new IllegalStateException("No products found");

        Order order = new Order();
        order.setOrderDate(LocalDateTime.now());
        order.setUser(user);

        // Wybrane produkty (batch):
        Set<Product> orderProducts = new HashSet<>();
        for (int i = 0; i < itemCount; i++) {
            orderProducts.add(allProducts.get(i % allProducts.size()));
        }
        order.setProducts(orderProducts);

        // Zapis zamówienia:
        orderMapper.insertOrder(order); // po wywołaniu order.id zostanie ustawione (useGeneratedKeys)
        // Zapis pozycji do tabeli order_item:
        for (Product p : orderProducts) {
            orderMapper.insertOrderItem(order.getId(), p.getId());
        }
        return order;
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

