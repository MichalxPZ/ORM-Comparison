package put.poznan.pl.michalxpz.jdbcapp.controller;

import io.micrometer.core.annotation.Timed;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import put.poznan.pl.michalxpz.jdbcapp.model.Order;
import put.poznan.pl.michalxpz.jdbcapp.model.OrderRequest;
import put.poznan.pl.michalxpz.jdbcapp.model.Product;
import put.poznan.pl.michalxpz.jdbcapp.service.DataInitService;
import put.poznan.pl.michalxpz.jdbcapp.service.OrderService;
import put.poznan.pl.michalxpz.jdbcapp.service.ProductService;

import java.math.BigDecimal;
import java.util.List;
import java.util.logging.Logger;

@Slf4j
@RestController
@RequestMapping("/api")
public class ScenarioController {
    @Autowired private OrderService orderService;
    @Autowired private ProductService productService;
    @Autowired private DataInitService dataInitService;

    Logger logger = Logger.getLogger(ScenarioController.class.getName());

    // Scenariusz 1: Pobranie zamówienia z jego pozycjami (eager/lazy load)
    @Timed("getOrderWithItems.timer")
    @GetMapping("/orders/{id}")
    public Order getOrderWithItems(@PathVariable Long id) {
        logger.info("Fetching order with ID: " + id);
        return orderService.getOrderWithItems(id);
    }

    // Scenariusz 2: Filtrowanie i sortowanie listy produktów po kryteriach
    @Timed("getProducts.timer")
    @GetMapping("/products")
    public List<Product> getProducts(@RequestParam(required=false) Long categoryId,
                                     @RequestParam(required=false) BigDecimal minPrice,
                                     @RequestParam(required=false) BigDecimal maxPrice,
                                     @RequestParam(required=false) String keyword) {
        logger.info("Filtering products with criteria: categoryId=" + categoryId +
                ", minPrice=" + minPrice + ", maxPrice=" + maxPrice + ", keyword='" + keyword + "'");
        return productService.getFilteredProducts(categoryId, minPrice, maxPrice, keyword);
    }

    // Scenariusz 3: Utworzenie zamówienia z batch-insert wielu pozycji (parametr count określa liczbę pozycji)
    @Timed("batch-insert.timer")
    @PostMapping("/orders/batchItems")
    public Order createOrderBatch(@RequestParam(name="count", defaultValue="10") int count) {
        logger.info("Creating order with " + count + " items using batch insert.");
        return orderService.createOrderWithItemsBatch(count);
    }

    // Scenariusz 4: Masowa aktualizacja cen produktów o podany procent (np. ?percent=5.0)
    @Timed("updatePrices.timer")
    @PutMapping("/products/prices")
    public String updatePrices(@RequestParam BigDecimal percent) {
        logger.info("Updating product prices by " + percent + "%");
        int updated = productService.updateProductPrices(percent);
        return "Updated prices for " + updated + " products.";
    }

    // Scenariusz 5: Złożona operacja transakcyjna - utworzenie zamówienia z listą produktów dla użytkownika
    @Timed("createOrderTransactional.timer")
    @PostMapping("/orders/complex")
    public Order createOrderTransactional(@RequestBody OrderRequest request) {
        logger.info("Creating order for user: " + request.getUserId() + " with products: " + request.getProductIds());
        return orderService.placeOrder(request.getUserId(), request.getProductIds());
    }

    // Endpoint: Inicjalizacja bazy danych określoną liczbą encji (użytkowników, kategorii, produktów, zamówień)
    @Timed("init.timer")
    @PostMapping("/init")
    public String initializeDatabase(@RequestParam(defaultValue="50") int users,
                                     @RequestParam(defaultValue="5") int categories,
                                     @RequestParam(defaultValue="100") int products,
                                     @RequestParam(defaultValue="20") int orders,
                                     @RequestParam(defaultValue="5") int itemsPerOrder) {
        logger.info(String.format("Initializing database with %d users, %d categories, %d products, %d orders (%d items each).",
                users, categories, products, orders, itemsPerOrder));
        dataInitService.initializeDatabase(users, categories, products, orders, itemsPerOrder);
        logger.info("Database initialization completed successfully.");
        if (orders > 0) {
            return String.format("Initialized database with %d users, %d categories, %d products, and %d orders (%d items each).",
                    users, categories, products, orders, itemsPerOrder);
        } else {
            return String.format("Initialized database with %d users, %d categories, %d products, and 0 orders.",
                    users, categories, products);
        }
    }
}
