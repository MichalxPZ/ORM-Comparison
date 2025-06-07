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

    /** Scenariusz 1: Odczyt zamówienia wraz z powiązanymi pozycjami */
    public Order getOrderWithItems(Long orderId) {
        Order order = orderRepo.findById(orderId);
        if (order != null) {
            List<OrderItem> items = orderItemRepo.findByOrderId(orderId);
            order.setItems(items);
        }
        return order;
    }

    /** Scenariusz 3: Utworzenie zamówienia z wsadowym dodaniem pozycji (batch insert) */
    @Transactional
    public Order createOrderWithItemsBatch(int itemCount) {
        // 1. Wybierz losowego istniejącego użytkownika
        User randomUser = userRepo.findRandomUser();
        if (randomUser == null) {
            throw new IllegalStateException("Brak użytkowników w bazie!");
        }
        // 2. Utwórz nowy produkt, który będzie dodany do wszystkich pozycji zamówienia
        Product newProd = new Product(null, 1L, "NewProduct", "Batch-insert product",
                new BigDecimal("100.00"), 100);
        productRepo.insert(newProd);
        if (newProd.getId() == null) {
            throw new IllegalStateException("Nie udało się pobrać ID nowego produktu");
        }
        // 3. Utwórz nowe zamówienie powiązane z wybranym użytkownikiem
        Order order = new Order(null, randomUser.getId(), LocalDateTime.now());
        orderRepo.insert(order);
        if (order.getId() == null) {
            throw new IllegalStateException("Nie udało się pobrać ID nowego zamówienia");
        }
        // 4. Przygotuj listę pozycji zamówienia (każda z nowym produktem)
        List<OrderItem> items = new ArrayList<>(itemCount);
        for (int i = 0; i < itemCount; i++) {
            items.add(new OrderItem(null, order.getId(), newProd.getId(), 1));
        }
        // 5. Wstaw pozycje zamówienia w trybie batch
        orderItemRepo.insertBatch(items);
        order.setItems(items);
        return order;
    }

    /** Scenariusz 5: Złożona transakcja zakupu (odczyt + weryfikacje + modyfikacje wielu tabel) */
    @Transactional
    public Order placeOrder(Long userId, List<Long> productIds) {
        // 1. Odczytaj użytkownika (weryfikacja istnienia)
        User user = userRepo.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }
        // 2. Sprawdź dostępność produktów i zmniejsz ich stan magazynowy
        List<Product> products = new ArrayList<>();
        for (Long productId : productIds) {
            Product p = productRepo.findById(productId);
            if (p == null) {
                throw new IllegalArgumentException("Product " + productId + " not found");
            }
            // zmniejsz stan magazynowy produktu o 1
            productRepo.updateStock(p.getId(), p.getStock() - 1);
            products.add(p);
        }
        // 3. Utwórz nowe zamówienie powiązane z użytkownikiem
        Order order = new Order(null, userId, LocalDateTime.now());
        orderRepo.insert(order);
        // 4. Dodaj po jednej pozycji OrderItem dla każdego produktu z listy
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