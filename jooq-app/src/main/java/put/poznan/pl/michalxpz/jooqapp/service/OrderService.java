package put.poznan.pl.michalxpz.jooqapp.service;


import org.jooq.DSLContext;
import org.jooq.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import put.poznan.pl.michalxpz.generated.tables.pojos.Orders;
import put.poznan.pl.michalxpz.generated.tables.pojos.Products;
import put.poznan.pl.michalxpz.generated.tables.pojos.Users;
import put.poznan.pl.michalxpz.generated.tables.records.OrdersRecord;
import put.poznan.pl.michalxpz.jooqapp.model.OrderWithItems;
import put.poznan.pl.michalxpz.jooqapp.repository.OrderRepository;
import put.poznan.pl.michalxpz.jooqapp.repository.ProductRepository;
import put.poznan.pl.michalxpz.jooqapp.repository.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static put.poznan.pl.michalxpz.generated.Tables.ORDER_ITEMS;

@Service
public class OrderService {
    @Autowired private OrderRepository orderRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private DSLContext dsl;

    @Value("${DB:postgresql}") private String db;

    /** Scenario 1: Fetch order with its products. */
    @Transactional(readOnly = true)
    public OrderWithItems getOrderWithItems(Long orderId) {
        // Fetch order record and its products
        Orders orderRec = orderRepository.findById(orderId);
        List<Products> products = orderRepository.findProductsByOrderId(orderId);

        // Map to our DTO-like object
        OrderWithItems orderWithItems = new OrderWithItems();
        orderWithItems.setId(Long.valueOf(orderRec.id()));
        orderWithItems.setOrderDate(orderRec.orderDate());
        orderWithItems.setUserId(Long.valueOf(orderRec.userId()));
        orderWithItems.setProducts(products);
        return orderWithItems;
    }

    /** Scenario 3: Create an order with a batch of items. */
    @Transactional
    public Orders createOrderWithItemsBatch(int itemCount) {
        Users user = userRepository.findRandomUser(db);
        if (user == null) throw new IllegalStateException("No users found");

        // Insert 1 produkt
        Products product = new Products(
                null,
                1,
                "NewProduct",
                "Batch-insert product",
                new BigDecimal("100.00"),
                100
        );
        product = productRepository.saveAndReturn(product);

        // Insert zamówienie
        OrdersRecord orderRecord = orderRepository.insertOrder(Long.valueOf(user.id()), LocalDateTime.now());

        // Batch insert pozycji zamówienia (w serwisie)
        final int BATCH_SIZE = 500;
        for (int i = 0; i < itemCount; i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, itemCount);
            List<Query> batch = new ArrayList<>(end - i);
            for (int j = i; j < end; j++) {
                batch.add(
                        dsl.insertInto(ORDER_ITEMS)
                                .columns(ORDER_ITEMS.ORDER_ID, ORDER_ITEMS.PRODUCT_ID)
                                .values(orderRecord.getId(), product.id())
                );
            }
            dsl.batch(batch).execute();
        }

        // Zwracamy POJO Orders
        return new Orders(orderRecord.getId(), user.id(), orderRecord.getOrderDate());
    }


    /** Scenario 5: Place order for user with given product IDs. */
    @Transactional
    public OrdersRecord placeOrder(Long userId, List<Long> productIds) {
        // Verify user exists
        userRepository.findById(userId);

        // Verify all products exist
        List<Products> products = productRepository.findAllById(productIds);
        if (products.size() != productIds.size()) {
            throw new IllegalArgumentException("Some products not found");
        }

        // Use a Set to avoid duplicates
        List<Integer> productIdSet = new ArrayList<>(productIds).stream().map(Long::intValue).toList();
        // Create and return the order
        return orderRepository.insertOrder(userId, LocalDateTime.now());
    }
}

