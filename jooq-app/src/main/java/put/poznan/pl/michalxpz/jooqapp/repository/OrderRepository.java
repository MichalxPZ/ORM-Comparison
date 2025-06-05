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
                .where(ORDERS.ID.eq(orderId))
                .fetchOne();
        if (order == null) {
            throw new IllegalArgumentException("Order not found");
        }
        return order;
    }

    /** Retrieves all products belonging to a given order via the join table. */
    public List<ProductRecord> findProductsByOrderId(Long orderId) {
        return dsl.select(PRODUCT.fields())
                .from(PRODUCT)
                .join(ORDER_ITEM).on(PRODUCT.ID.eq(ORDER_ITEM.PRODUCT_ID))
                .where(ORDER_ITEM.ORDER_ID.eq(orderId))
                .fetchInto(ProductRecord.class);
    }

    /** Creates a new order with the given userId, date and productIds. */
    public OrdersRecord createOrder(Long userId, LocalDateTime orderDate, Set<Long> productIds) {
        // Insert into ORDERS table
        OrdersRecord orderRecord = dsl.newRecord(ORDERS);
        orderRecord.setUserId(userId);
        orderRecord.setOrderDate(orderDate);
        orderRecord.store(); // Inserts and populates the generated ID

        // Insert into the join table for each product
        for (Long productId : productIds) {
            dsl.insertInto(ORDER_ITEM)
                    .columns(ORDER_ITEM.ORDER_ID, ORDER_ITEM.PRODUCT_ID)
                    .values(orderRecord.getId().longValue(), productId)
                    .execute();
        }
        return orderRecord;
    }
}

