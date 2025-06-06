package put.poznan.pl.michalxpz.mybatisapp.controller;

import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import put.poznan.pl.michalxpz.mybatisapp.model.Order;
import put.poznan.pl.michalxpz.mybatisapp.model.OrderRequest;
import put.poznan.pl.michalxpz.mybatisapp.model.Product;
import put.poznan.pl.michalxpz.mybatisapp.service.OrderService;
import put.poznan.pl.michalxpz.mybatisapp.service.ProductService;
import put.poznan.pl.michalxpz.mybatisapp.service.DataInitService;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

@Slf4j
@RestController
@RequestMapping("/api")
public class ScenarioController {

    @Autowired private OrderService orderService;
    @Autowired private ProductService productService;
    @Autowired private DataInitService dataInitService;
    @Autowired private MeterRegistry meterRegistry;

    @Value("${orm:unknown}") private String orm;
    @Value("${db:unknown}") private String db;

    Logger logger = Logger.getLogger(ScenarioController.class.getName());

    // Scenariusz 1: Pobranie zamówienia z jego pozycjami (eager/lazy load)
    @Timed("getOrderWithItems.timer")
    @GetMapping("/orders/{id}")
    public ResponseEntity<Order> getOrderWithItems(@PathVariable Long id) {
        logger.info("Fetching order with ID: " + id);
        AtomicReference<Order> orderRef = new AtomicReference<>();
        recordMetrics("get-by-id", null, () -> {
            Order order = orderService.getOrderWithItems(id);
            orderRef.set(order);
        });
        return ResponseEntity.ok(orderRef.get());
    }

    // Scenariusz 2: Filtrowanie i sortowanie listy produktów po kryteriach
    @Timed("getProducts.timer")
    @GetMapping("/products")
    public ResponseEntity<List<Product>> getProducts(@RequestParam(required=false) Long categoryId,
                                                     @RequestParam(required=false) BigDecimal minPrice,
                                                     @RequestParam(required=false) BigDecimal maxPrice,
                                                     @RequestParam(required=false) String keyword) {
        logger.info("Filtering products with criteria: categoryId=" + categoryId +
                ", minPrice=" + minPrice + ", maxPrice=" + maxPrice + ", keyword='" + keyword + "'");
        AtomicReference<List<Product>> productsRef = new AtomicReference<>();
        recordMetrics("get-products", null, () -> {
            List<Product> products = productService.getFilteredProducts(categoryId, minPrice, maxPrice, keyword);
            productsRef.set(products);
        });

        return ResponseEntity.ok(productsRef.get());
    }

    // Scenariusz 3: Utworzenie zamówienia z batch-insert wielu pozycji (parametr count określa liczbę pozycji)
    @Timed("batch-insert.timer")
    @PostMapping("/orders/batchItems")
    public ResponseEntity<Order> createOrderBatch(@RequestParam(name="count", defaultValue="10") int count) {
        logger.info("Creating order with " + count + " items using batch insert.");
        AtomicReference<Order> orderRef = new AtomicReference<>();
        recordMetrics("batch-insert", count, () -> {
            Order order = orderService.createOrderWithItemsBatch(count);
            orderRef.set(order);
        });
        return ResponseEntity.ok(orderRef.get());
    }

    // Scenariusz 4: Masowa aktualizacja cen produktów o podany procent (np. ?percent=5.0)
    @Timed("updatePrices.timer")
    @PutMapping("/products/prices")
    public String updatePrices(@RequestParam BigDecimal percent) {
        logger.info("Updating product prices by " + percent + "%");
        AtomicInteger updatedRef = new AtomicInteger();
        recordMetrics("updatePrices", percent.intValue(), () -> {
            int updated = productService.updateProductPrices(percent);
            updatedRef.set(updated);
        });
        return "Updated prices for " + updatedRef.get() + " products.";
    }

    // Scenariusz 5: Złożona operacja transakcyjna - utworzenie zamówienia z listą produktów dla użytkownika
    @Timed("createOrderTransactional.timer")
    @PostMapping("/orders/complex")
    public ResponseEntity<Order> createOrderTransactional(@RequestBody OrderRequest request) {
        logger.info("Creating order for user: " + request.getUserId() + " with products: " + request.getProductIds());
        AtomicReference<Order> orderRef = new AtomicReference<>();
        recordMetrics("transactionalOrder", null, () -> {
            Order order = orderService.placeOrder(request.getUserId(), request.getProductIds());
            orderRef.set(order);
        });
        return ResponseEntity.ok(orderRef.get());
    }

    // Endpoint: Inicjalizacja bazy danych określoną liczbą encji (użytkowników, kategorii, produktów, zamówień)
    @Timed("init.timer")
    @PostMapping("/init")
    public ResponseEntity<String> initializeDatabase(@RequestParam(defaultValue="50") int users,
                                                     @RequestParam(defaultValue="5") int categories,
                                                     @RequestParam(defaultValue="100") int products,
                                                     @RequestParam(defaultValue="20") int orders,
                                                     @RequestParam(defaultValue="5") int itemsPerOrder) {
        logger.info(String.format("Initializing database with %d users, %d categories, %d products, %d orders (%d items each).",
                users, categories, products, orders, itemsPerOrder));
        recordMetrics("init", null ,() -> dataInitService.initializeDatabase(users, categories, products, orders, itemsPerOrder));
        logger.info("Database initialization completed successfully.");

        return ResponseEntity.ok(
                String.format("Initialized DB: %d users, %d categories, %d products, %d orders (%d items each).",
                        users, categories, products, orders, itemsPerOrder));
    }

    private void recordMetrics(String scenario, Integer value, Runnable action) {
        Timer.Sample sample = Timer.start(meterRegistry);

        try {
            action.run();
        } finally {
            if (value == null) {
                sample.stop(Timer.builder(String.format("%s-%s-%s-duration_seconds", scenario, orm, db))
                        .description("Czas wykonania scenariusza testowego")
                        .tags("scenario", scenario, "orm", orm, "db", db)
                        .register(meterRegistry));
            } else {
                sample.stop(Timer.builder(String.format("%s-%s-%s-duration_seconds", scenario, orm, db))
                        .description("Czas wykonania scenariusza testowego")
                        .tags("scenario", scenario, "orm", orm, "db", db, "param", String.valueOf(value))
                        .register(meterRegistry));
            }
        }
    }
}

