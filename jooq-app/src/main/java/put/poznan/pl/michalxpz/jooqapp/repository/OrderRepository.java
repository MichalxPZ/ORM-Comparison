package put.poznan.pl.michalxpz.jooqapp.repository;


import org.jooq.DSLContext;
import org.jooq.Query;
import org.jooq.impl.QOM;
import put.poznan.pl.michalxpz.generated.tables.pojos.Orders;
import put.poznan.pl.michalxpz.generated.tables.pojos.Products;
import put.poznan.pl.michalxpz.generated.tables.records.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static put.poznan.pl.michalxpz.generated.Tables.*;

@Repository
public class OrderRepository {
    @Autowired private DSLContext dsl;

    /** Retrieves the order record by ID, throwing if not found. */
    public Orders findById(Long orderId) {
        Orders order = dsl.selectFrom(ORDERS)
                .where(ORDERS.ID.eq(Math.toIntExact(orderId)))
                .fetchOneInto(Orders.class);
        if (order == null) {
            throw new IllegalArgumentException("Order not found");
        }
        return order;
    }

    /** Retrieves all products belonging to a given order via the join table. */
    public List<Products> findProductsByOrderId(Long orderId) {
        return dsl.select(PRODUCTS.fields())
                .from(PRODUCTS)
                .join(ORDER_ITEMS).on(PRODUCTS.ID.eq(ORDER_ITEMS.PRODUCT_ID))
                .where(ORDER_ITEMS.ORDER_ID.eq(Math.toIntExact(orderId)))
                .fetchInto(Products.class);
    }

    /**
     * Creates a new order with the given userId, date and productIds.
     */
    public OrdersRecord insertOrder(Long userId, LocalDateTime orderDate) {
        OrdersRecord orderRecord = dsl.newRecord(ORDERS);
        orderRecord.setUserId(Math.toIntExact(userId));
        orderRecord.setOrderDate(orderDate);
        orderRecord.store();
        return orderRecord;
    }
}

