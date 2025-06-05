package put.poznan.pl.michalxpz.jdbcapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import put.poznan.pl.michalxpz.jdbcapp.model.Category;
import put.poznan.pl.michalxpz.jdbcapp.model.Order;
import put.poznan.pl.michalxpz.jdbcapp.model.OrderItem;
import put.poznan.pl.michalxpz.jdbcapp.model.Product;
import put.poznan.pl.michalxpz.jdbcapp.model.User;
import put.poznan.pl.michalxpz.jdbcapp.repository.CategoryRepository;
import put.poznan.pl.michalxpz.jdbcapp.repository.OrderItemRepository;
import put.poznan.pl.michalxpz.jdbcapp.repository.OrderRepository;
import put.poznan.pl.michalxpz.jdbcapp.repository.ProductRepository;
import put.poznan.pl.michalxpz.jdbcapp.repository.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class DataInitService {
    @Autowired private CategoryRepository categoryRepo;
    @Autowired private UserRepository userRepo;
    @Autowired private ProductRepository productRepo;
    @Autowired private OrderRepository orderRepo;
    @Autowired private OrderItemRepository orderItemRepo;

    /**
     * Initialize the database with given numbers of entities (users, categories, products, orders, etc).
     * All operations run in one transaction for consistency.
     */
    @Transactional
    public void initializeDatabase(int userCount, int categoryCount, int productCount,
                                   int orderCount, int itemsPerOrder) {
        // 1. Insert categories
        List<Long> categoryIds = new ArrayList<>();
        for (int i = 1; i <= categoryCount; i++) {
            Category category = new Category(null, "Category " + i);
            categoryRepo.insert(category);
            if (category.getId() == null) {
                throw new IllegalStateException("Failed to get ID of new category");
            }
            categoryIds.add(category.getId());
        }

        // 2. Insert users
        List<Long> userIds = new ArrayList<>();
        for (int u = 1; u <= userCount; u++) {
            User user = new User(null, "User" + u, "user" + u + "@example.com");
            userRepo.insert(user);
            if (user.getId() == null) {
                throw new IllegalStateException("Failed to get ID of new user");
            }
            userIds.add(user.getId());
        }

        // 3. Insert products with random prices and initial stock
        List<Long> productIds = new ArrayList<>();
        Random rand = new Random();
        for (int p = 1; p <= productCount; p++) {
            Long categoryId = categoryIds.isEmpty() ? null
                    : categoryIds.get((p - 1) % categoryIds.size());
            // Random price between 5 and 500 (inclusive), with two decimal places
            int basePrice = 5 + rand.nextInt(496);  // 5 to 500
            BigDecimal price = BigDecimal.valueOf(basePrice).setScale(2);
            Product product = new Product(null, categoryId, "Product" + p,
                    "Sample product " + p, price, 100);
            productRepo.insert(product);
            if (product.getId() == null) {
                throw new IllegalStateException("Failed to get ID of new product");
            }
            productIds.add(product.getId());
        }

        // 4. Insert orders and order items
        //    For each new order, pick a random user and generate given number of items with random products.
        //    Track product usage to adjust stock levels accordingly.
        Map<Long, Integer> productUsageCount = new HashMap<>();
        for (int o = 1; o <= orderCount; o++) {
            // pick a random user for this order
            Long userId = userIds.isEmpty() ? null : userIds.get(rand.nextInt(userIds.size()));
            Order order = new Order(null, userId, LocalDateTime.now());
            orderRepo.insert(order);
            if (order.getId() == null) {
                throw new IllegalStateException("Failed to get ID of new order");
            }
            // create order items if requested
            if (itemsPerOrder > 0 && !productIds.isEmpty()) {
                List<OrderItem> items = new ArrayList<>();
                for (int i = 0; i < itemsPerOrder; i++) {
                    Long productId = productIds.get(rand.nextInt(productIds.size()));
                    items.add(new OrderItem(null, order.getId(), productId, 1));
                    // update usage count for this product
                    productUsageCount.put(productId,
                            productUsageCount.getOrDefault(productId, 0) + 1);
                }
                orderItemRepo.insertBatch(items);
            }
        }

        // 5. Update stock levels for products used in orders (decrease stock by number of times product was ordered)
        for (Map.Entry<Long, Integer> entry : productUsageCount.entrySet()) {
            Long prodId = entry.getKey();
            int timesOrdered = entry.getValue();
            // Decrease stock but not below 0
            int currentStock = 100;  // initial stock set above
            int newStock = currentStock - timesOrdered;
            if (newStock < 0) newStock = 0;
            productRepo.updateStock(prodId, newStock);
        }
    }
}
