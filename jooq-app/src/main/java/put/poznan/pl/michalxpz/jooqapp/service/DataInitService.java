package put.poznan.pl.michalxpz.jooqapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.jooq.DSLContext;
import put.poznan.pl.michalxpz.generated.tables.records.*;
import put.poznan.pl.michalxpz.jooqapp.repository.CategoryRepository;
import put.poznan.pl.michalxpz.jooqapp.repository.TagRepository;
import put.poznan.pl.michalxpz.jooqapp.repository.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static put.poznan.pl.michalxpz.generated.Tables.*;

@Service
public class DataInitService {
    @Autowired private DSLContext dsl;
    @Autowired private UserRepository userRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private TagRepository tagRepository;

    /** Initializes the database with sample users, categories, tags, products, and orders. */
    @Transactional
    public void initializeDatabase(int userCount, int categoryCount, int productCount,
                                   int orderCount, int itemsPerOrder) {
        Random rand = new Random();

        // 1. Users
        List<UsersRecord> users = new ArrayList<>();
        for (int i = 1; i <= userCount; i++) {
            UsersRecord user = dsl.newRecord(USERS);
            user.setName("User" + i);
            user.setEmail("user" + i + "@example.com");
            users.add(user);
        }
        userRepository.saveAll(users);

        // 2. Categories
        List<CategoriesRecord> categories = new ArrayList<>();
        for (int i = 1; i <= categoryCount; i++) {
            CategoriesRecord category = dsl.newRecord(CATEGORIES);
            category.setName("Category" + i);
            categories.add(category);
        }
        categoryRepository.saveAll(categories);

        // 3. Tags
        List<TagsRecord> tags = new ArrayList<>();
        for (int i = 1; i <= categoryCount * 2; i++) {
            TagsRecord tag = dsl.newRecord(TAGS);
            tag.setName("Tag" + i);
            tags.add(tag);
        }
        tagRepository.saveAll(tags);

        // 4. Products
        List<ProductsRecord> products = new ArrayList<>();
        for (int i = 1; i <= productCount; i++) {
            // Pick random category
            CategoriesRecord category = categories.get(rand.nextInt(categories.size()));
            ProductsRecord product = dsl.newRecord(PRODUCTS);
            product.setName("Product" + i);
            product.setDescription("Description of product " + i);
            product.setPrice(BigDecimal.valueOf(10 + rand.nextInt(90)));
            product.setStock(100);
            product.setCategoryId(category.getId());
            product.store(); // Insert and get ID

            // Assign random tags (1 to 3 unique tags)
            int tagCount = 1 + rand.nextInt(3);
            Set<Long> productTagIds = new HashSet<>();
            for (int j = 0; j < tagCount; j++) {
                Integer tagId = tags.get(rand.nextInt(tags.size())).getId();
                productTagIds.add(Long.valueOf(tagId));
            }
            // Insert join table entries for product-tags
            for (Long tagId : productTagIds) {
                dsl.insertInto(PRODUCT_TAG)
                        .columns(PRODUCT_TAG.PRODUCT_ID, PRODUCT_TAG.TAG_ID)
                        .values(product.getId(), Math.toIntExact(tagId))
                        .execute();
            }
            products.add(product);
        }

        // 5. Orders
        List<Integer> productIds = products.stream().map(ProductsRecord::getId).toList();
        for (int i = 0; i < orderCount; i++) {
            // Pick random user
            UsersRecord user = users.get(rand.nextInt(users.size()));
            // Create order
            OrdersRecord order = dsl.newRecord(ORDERS);
            order.setOrderDate(LocalDateTime.now());
            order.setUserId(user.getId());
            order.store(); // Insert order

            // Add random products to this order
            Set<Long> orderProductIds = new HashSet<>();
            for (int j = 0; j < itemsPerOrder; j++) {
                orderProductIds.add(Long.valueOf(productIds.get(rand.nextInt(productIds.size()))));
            }
            // Insert join table entries for order-products
            for (Long pid : orderProductIds) {
                dsl.insertInto(ORDER_ITEMS)
                        .columns(ORDER_ITEMS.ORDER_ID, ORDER_ITEMS.PRODUCT_ID)
                        .values(order.getId(), Math.toIntExact(pid))
                        .execute();
            }
        }
        // Note: Orders already inserted individually above.
    }
}
