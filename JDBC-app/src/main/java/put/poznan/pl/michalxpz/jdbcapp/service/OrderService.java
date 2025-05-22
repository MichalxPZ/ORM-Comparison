package put.poznan.pl.michalxpz.jdbcapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import put.poznan.pl.michalxpz.jdbcapp.model.Order;
import put.poznan.pl.michalxpz.jdbcapp.model.OrderItem;
import put.poznan.pl.michalxpz.jdbcapp.model.Product;
import put.poznan.pl.michalxpz.jdbcapp.model.User;
import put.poznan.pl.michalxpz.jdbcapp.repository.OrderItemRepository;
import put.poznan.pl.michalxpz.jdbcapp.repository.OrderRepository;
import put.poznan.pl.michalxpz.jdbcapp.repository.ProductRepository;
import put.poznan.pl.michalxpz.jdbcapp.repository.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {
    @Autowired private OrderRepository orderRepo;
    @Autowired private OrderItemRepository orderItemRepo;
    @Autowired private UserRepository userRepo;
    @Autowired private ProductRepository productRepo;

    /** Scenariusz 1: Odczyt zamówienia wraz z pozycjami */
    public Order getOrderWithItems(Long orderId) {
        Order order = orderRepo.findById(orderId);
        if (order != null) {
            List<OrderItem> items = orderItemRepo.findByOrderId(orderId);
            order.setItems(items);
        }
        return order;
    }

    /** Scenariusz 3: Utworzenie nowego zamówienia z losowym użytkownikiem, nowym produktem
     *  oraz wsadowe dodanie wielu pozycji tego produktu do zamówienia */
    public Order createOrderWithItemsBatch(int itemCount) {
        // 1. Wybierz losowego istniejącego użytkownika
        User randomUser = userRepo.findRandomUser();
        // 2. Utwórz nowy produkt (przykładowe dane, kategoria stała 1L)
        Product newProd = new Product(null, 1L, "NewProduct", "Batch-insert product",
                new BigDecimal("100.00"), 100);
        productRepo.insert(newProd);
        // 3. Utwórz nowe zamówienie dla wylosowanego użytkownika
        Order order = new Order(null, randomUser.getId(), LocalDateTime.now());
        orderRepo.insert(order);
        // 4. Przygotuj listę pozycji zamówienia (wiele pozycji z tym samym produktem)
        List<OrderItem> items = new ArrayList<>();
        for (int i = 0; i < itemCount; i++) {
            items.add(new OrderItem(null, order.getId(), newProd.getId(), 1));
        }
        // 5. Wstaw pozycje zamówienia wsadowo
        orderItemRepo.insertBatch(items);
        order.setItems(items);
        return order;
    }

    /** Scenariusz 5: Złożona transakcja zakupu (odczyt + weryfikacja + modyfikacje wielu tabel) */
    @Transactional  // cała operacja wykonuje się w jednej transakcji
    public Order placeOrder(Long userId, List<Long> productIds) {
        // 1. Odczytaj użytkownika (sprawdzenie istnienia)
        User user = userRepo.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }
        // 2. Sprawdź dostępność produktów i zaktualizuj ich stany
        List<Product> products = new ArrayList<>();
        for (Long productId : productIds) {
            Product p = productRepo.findById(productId);
            if (p == null) {
                throw new IllegalArgumentException("Product " + productId + " not found");
            }
            if (p.getStock() <= 0) {
                throw new RuntimeException("Product " + productId + " is out of stock");
            }
            // zmniejsz stan magazynowy o 1
            productRepo.updateStock(p.getId(), p.getStock() - 1);
            products.add(p);
        }
        // 3. Utwórz nowe zamówienie powiązane z użytkownikiem
        Order order = new Order(null, userId, LocalDateTime.now());
        orderRepo.insert(order);
        // 4. Dodaj po jednej pozycji OrderItem dla każdego produktu
        List<OrderItem> orderItems = new ArrayList<>();
        for (Product p : products) {
            OrderItem item = new OrderItem(null, order.getId(), p.getId(), 1);
            orderItemRepo.insert(item);
            orderItems.add(item);
        }
        order.setItems(orderItems);
        return order;
    }
}
