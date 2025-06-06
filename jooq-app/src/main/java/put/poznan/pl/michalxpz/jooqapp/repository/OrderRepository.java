package put.poznan.pl.michalxpz.jooqapp.repository;


import org.jooq.DSLContext;
import put.poznan.pl.michalxpz.generated.tables.records.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static put.poznan.pl.michalxpz.generated.Tables.*;

@Repository
public class OrderRepository {
    @Autowired private DSLContext dsl;

    /** Retrieves the order record by ID, throwing if not found. */
    public OrdersRecord findById(Long orderId) {
        OrdersRecord order = dsl.selectFrom(ORDERS)
                .where(ORDERS.ID.eq(Math.toIntExact(orderId)))
                .fetchOne();
        if (order == null) {
            throw new IllegalArgumentException("Order not found");
        }
        return order;
    }

    /** Retrieves all products belonging to a given order via the join table. */
    public List<ProductsRecord> findProductsByOrderId(Long orderId) {
        return dsl.select(PRODUCTS.fields())
                .from(PRODUCTS)
                .join(ORDER_ITEMS).on(PRODUCTS.ID.eq(ORDER_ITEMS.PRODUCT_ID))
                .where(ORDER_ITEMS.ORDER_ID.eq(Math.toIntExact(orderId)))
                .fetchInto(ProductsRecord.class);
    }

    /** Creates a new order with the given userId, date and productIds. */
    public OrdersRecord createOrder(Long userId, LocalDateTime orderDate, Set<Long> productIds) {
        // Insert into ORDERS table
        OrdersRecord orderRecord = dsl.newRecord(ORDERS);
        orderRecord.setUserId(Math.toIntExact(userId));
        orderRecord.setOrderDate(orderDate);
        orderRecord.store(); // Inserts and populates the generated ID

        // Insert into the join table for each product
        for (Long productId : productIds) {
            dsl.insertInto(ORDER_ITEMS)
                    .columns(ORDER_ITEMS.ORDER_ID, ORDER_ITEMS.PRODUCT_ID)
                    .values(orderRecord.getId(), Math.toIntExact(productId))
                    .execute();
        }
        return orderRecord;
    }
}

