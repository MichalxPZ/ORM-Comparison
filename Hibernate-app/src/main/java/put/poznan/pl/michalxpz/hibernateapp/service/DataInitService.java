package put.poznan.pl.michalxpz.hibernateapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import put.poznan.pl.michalxpz.hibernateapp.model.Product;
import put.poznan.pl.michalxpz.hibernateapp.model.*;
import put.poznan.pl.michalxpz.hibernateapp.repository.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class DataInitService {

    @Autowired
    private UserRepository userRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private TagRepository tagRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private OrderRepository orderRepository;

    @Transactional
    public void initializeDatabase(int userCount, int categoryCount, int productCount,
                                   int orderCount, int itemsPerOrder) {

        Random rand = new Random();

        // 1. Użytkownicy
        List<User> users = new ArrayList<>();
        for (int i = 1; i <= userCount; i++) {
            users.add(new User(null, "User" + i, "user" + i + "@example.com", new ArrayList<>()));
        }
        userRepository.saveAll(users);

        // 2. Kategorie
        List<Category> categories = new ArrayList<>();
        for (int i = 1; i <= categoryCount; i++) {
            categories.add(new Category(null, "Category" + i, new ArrayList<>()));
        }
        categoryRepository.saveAll(categories);

        // 3. Tagi
        List<Tag> tags = new ArrayList<>();
        for (int i = 1; i <= categoryCount * 2; i++) {
            tags.add(new Tag(null, "Tag" + i, new HashSet<>()));
        }
        tagRepository.saveAll(tags);

        // 4. Produkty
        List<Product> products = new ArrayList<>();
        for (int i = 1; i <= productCount; i++) {
            Category category = categories.get(rand.nextInt(categories.size()));

            Product product = new Product();
            product.setName("Product" + i);
            product.setDescription("Description of product " + i);
            product.setPrice(BigDecimal.valueOf(10 + rand.nextInt(90)));
            product.setStock(100);
            product.setCategory(category);

            // Losowe tagi
            Set<Tag> productTags = new HashSet<>();
            for (int j = 0; j < 1 + rand.nextInt(3); j++) {
                productTags.add(tags.get(rand.nextInt(tags.size())));
            }
            product.setTags(productTags);

            products.add(product);
        }
        productRepository.saveAll(products);

        // 5. Zamówienia
        List<Order> orders = new ArrayList<>();
        for (int i = 0; i < orderCount; i++) {
            User user = users.get(rand.nextInt(users.size()));
            Order order = new Order();
            order.setOrderDate(LocalDateTime.now());
            order.setUser(user);

            // Dodaj losowe produkty
            Set<Product> orderProducts = new HashSet<>();
            for (int j = 0; j < itemsPerOrder; j++) {
                orderProducts.add(products.get(rand.nextInt(products.size())));
            }
            order.setProducts(orderProducts);

            orders.add(order);
        }
        orderRepository.saveAll(orders);
    }
}
