package put.poznan.pl.michalxpz.hibernateapp.controller;

import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import put.poznan.pl.michalxpz.hibernateapp.model.*;
import put.poznan.pl.michalxpz.hibernateapp.model.Product;
import put.poznan.pl.michalxpz.hibernateapp.service.DataInitService;
import put.poznan.pl.michalxpz.hibernateapp.service.OrderService;
import put.poznan.pl.michalxpz.hibernateapp.service.ProductService;

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
    public ResponseEntity<OrderDto> api(@PathVariable Integer id) {
        logger.info("Fetching order with ID: " + id);
        AtomicReference<Order> orderRef = new AtomicReference<>();
        recordMetrics("get-by-id", null, () -> {
            Order order = orderService.getOrderWithItems(id);
            orderRef.set(order);
        });
        OrderDto dto = OrderMapper.toDto(orderRef.get());
        return ResponseEntity.ok(dto);
    }

    // Scenariusz 2: Filtrowanie i sortowanie listy produktów po kryteriach
    @Timed("getProducts.timer")
    @GetMapping("/products")
    public ResponseEntity<List<ProductDto>> getProducts(@RequestParam(required=false) Long categoryId,
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

        List<ProductDto> dtos = productsRef.get().stream()
                .map(ProductMapper::toDto)
                .toList();

        return ResponseEntity.ok(dtos);
    }

    // Scenariusz 3: Utworzenie zamówienia z batch-insert wielu pozycji (parametr count określa liczbę pozycji)
    @Timed("batch-insert.timer")
    @PostMapping("/orders/batchItems")
    public ResponseEntity<OrderDto> createOrderBatch(@RequestParam(name="count", defaultValue="10") int count) {
        logger.info("Creating order with " + count + " items using batch insert.");
        AtomicReference<Order> orderRef = new AtomicReference<>();
        recordMetrics("batch-insert", count, () -> {
            Order order = orderService.createOrderWithItemsBatch(count);
            orderRef.set(order);
        });
        OrderDto dto = OrderMapper.toDto(orderRef.get());
        return ResponseEntity.ok(dto);
    }

    // Scenariusz 4: Masowa aktualizacja cen produktów z filtracja po modulo
    @Timed("updatePrices.timer")
    @PutMapping("/products/prices")
    public String updatePrices(@RequestParam Integer mod) {
        logger.info("Updating product prices filtering by modulo " + mod);
        AtomicInteger updatedRef = new AtomicInteger();
        recordMetrics("updatePrices", mod, () -> {
            int updated = productService.updateProductPrices(mod);
            updatedRef.set(updated);
        });
        return "Updated prices for " + updatedRef.get() + " products.";
    }

    // Scenariusz 5: Złożona operacja transakcyjna - utworzenie zamówienia z listą produktów dla użytkownika
    @Timed("createOrderTransactional.timer")
    @PostMapping("/orders/complex")
    public ResponseEntity<OrderDto> createOrderTransactional(@RequestBody OrderRequest request) {
        logger.info("Creating order for user: " + request.getUserId() + " with products: " + request.getProductIds());
        AtomicReference<Order> orderRef = new AtomicReference<>();
        recordMetrics("transactionalOrder", null, () -> {
            Order order = orderService.placeOrder(request.getUserId(), request.getProductIds());
            orderRef.set(order);
        });
        OrderDto dto = OrderMapper.toDto(orderRef.get());
        return ResponseEntity.ok(dto);
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

        if (value != null) {
            RequestContextHolder.currentRequestAttributes()
                    .setAttribute("benchmarkParam", value, RequestAttributes.SCOPE_REQUEST);
        }

        try {
            action.run();
        } finally {
            if (value == null) {
                sample.stop(Timer.builder(String.format("%s-duration_seconds", scenario))
                        .description("Czas wykonania scenariusza testowego")
                        .tags("scenario", scenario, "orm", orm, "db", db)
                        .register(meterRegistry));
            } else {
                sample.stop(Timer.builder(String.format("%s-duration_seconds", scenario))
                        .description("Czas wykonania scenariusza testowego")
                        .tags("scenario", scenario, "orm", orm, "db", db, "param", String.valueOf(value))
                        .register(meterRegistry));
            }
        }
    }
}


