package put.poznan.pl.michalxpz.mybatisapp.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import put.poznan.pl.michalxpz.mybatisapp.model.Order;
import put.poznan.pl.michalxpz.mybatisapp.model.OrderRequest;
import put.poznan.pl.michalxpz.mybatisapp.model.Product;
import put.poznan.pl.michalxpz.mybatisapp.service.OrderService;
import put.poznan.pl.michalxpz.mybatisapp.service.ProductService;
import put.poznan.pl.michalxpz.mybatisapp.service.DataInitService;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api")
public class ScenarioController {

    @Autowired private OrderService orderService;
    @Autowired private ProductService productService;
    @Autowired private DataInitService dataInitService;

    // Scenariusz 1: Pobranie zamówienia z pozycjami
    @GetMapping("/orders/{id}")
    public Order getOrderWithItems(@PathVariable Long id) {
        log.info("Fetching order with ID: {}", id);
        return orderService.getOrderWithItems(id);
    }

    // Scenariusz 2: Filtrowanie i sortowanie produktów
    @GetMapping("/products")
    public List<Product> getProducts(@RequestParam(required=false) Long categoryId,
                                     @RequestParam(required=false) BigDecimal minPrice,
                                     @RequestParam(required=false) BigDecimal maxPrice,
                                     @RequestParam(required=false) String keyword) {
        log.info("Filtering products: categoryId={}, minPrice={}, maxPrice={}, keyword={}",
                categoryId, minPrice, maxPrice, keyword);
        return productService.getFilteredProducts(categoryId, minPrice, maxPrice, keyword);
    }

    // Scenariusz 3: Utworzenie zamówienia z batch-insert pozycji
    @PostMapping("/orders/batchItems")
    public Order createOrderBatch(@RequestParam(name="count", defaultValue="10") int count) {
        log.info("Creating order with {} items (batch)", count);
        return orderService.createOrderWithItemsBatch(count);
    }

    // Scenariusz 4: Masowa aktualizacja cen produktów
    @PutMapping("/products/prices")
    public String updatePrices(@RequestParam BigDecimal percent) {
        log.info("Updating product prices by {}%", percent);
        int updated = productService.updateProductPrices(percent);
        return "Updated prices for " + updated + " products.";
    }

    // Scenariusz 5: Złożone utworzenie zamówienia dla użytkownika
    @PostMapping("/orders/complex")
    public Order createOrderTransactional(@RequestBody OrderRequest request) {
        log.info("Creating order for user: {} with products: {}", request.getUserId(), request.getProductIds());
        return orderService.placeOrder(request.getUserId(), request.getProductIds());
    }

    // Endpoint inicjalizacji bazy danych
    @PostMapping("/init")
    public String initializeDatabase(@RequestParam(defaultValue="50") int users,
                                     @RequestParam(defaultValue="5") int categories,
                                     @RequestParam(defaultValue="100") int products,
                                     @RequestParam(defaultValue="20") int orders,
                                     @RequestParam(defaultValue="5") int itemsPerOrder) {
        log.info("Initializing DB with {} users, {} categories, {} products, {} orders ({} items each).",
                users, categories, products, orders, itemsPerOrder);
        dataInitService.initializeDatabase(users, categories, products, orders, itemsPerOrder);
        log.info("Database initialization completed successfully.");
        if (orders > 0) {
            return String.format("Initialized DB: %d users, %d categories, %d products, %d orders (%d items each).",
                    users, categories, products, orders, itemsPerOrder);
        } else {
            return String.format("Initialized DB: %d users, %d categories, %d products, no orders.",
                    users, categories, products);
        }
    }
}

