package put.poznan.pl.michalxpz.mybatisapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import put.poznan.pl.michalxpz.mybatisapp.model.*;
import put.poznan.pl.michalxpz.mybatisapp.repository.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class DataInitService {

    @Autowired private UserMapper userMapper;
    @Autowired private CategoryMapper categoryMapper;
    @Autowired private TagMapper tagMapper;
    @Autowired private ProductMapper productMapper;
    @Autowired private OrderMapper orderMapper;

    @Transactional
    public void initializeDatabase(int userCount, int categoryCount, int productCount,
                                   int orderCount, int itemsPerOrder) {
        Random rand = new Random();

        // 1. Użytkownicy
        List<User> users = new ArrayList<>();
        for (int i = 1; i <= userCount; i++) {
            users.add(new User(null, "User" + i, "user" + i + "@example.com", null));
        }
        // Wstaw użytkowników (jako Batch w pętli lub w mapperze)
        for (User u : users) {
            userMapper.insert(u);
        }

        // 2. Kategorie
        List<Category> categories = new ArrayList<>();
        for (int i = 1; i <= categoryCount; i++) {
            categories.add(new Category(null, "Category" + i, null));
        }
        for (Category c : categories) {
            categoryMapper.insert(c);
        }

        // 3. Tagi
        List<Tag> tags = new ArrayList<>();
        for (int i = 1; i <= categoryCount * 2; i++) {
            tags.add(new Tag(null, "Tag" + i, null));
        }
        for (Tag t : tags) {
            tagMapper.insert(t);
        }

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
        // Wstaw produkty i powiąż z tagami:
        for (Product p : products) {
            productMapper.insert(p); // wstawia do tabeli product
            // Powiąż produkt z tagami:
            if (p.getTags() != null) {
                for (Tag t : p.getTags()) {
                    productMapper.insertProductTag(p.getId(), t.getId());
                }
            }
        }

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
        for (Order o : orders) {
            orderMapper.insertOrder(o);
            for (Product p : o.getProducts()) {
                orderMapper.insertOrderItem(o.getId(), p.getId());
            }
        }
    }
}

