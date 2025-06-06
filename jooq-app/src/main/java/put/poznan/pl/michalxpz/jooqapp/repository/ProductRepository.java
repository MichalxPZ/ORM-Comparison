package put.poznan.pl.michalxpz.jooqapp.repository;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import put.poznan.pl.michalxpz.generated.tables.records.*;

import java.math.BigDecimal;
import java.util.List;

import static put.poznan.pl.michalxpz.generated.Tables.*;


@Repository
public class ProductRepository {
    @Autowired private DSLContext dsl;

    /** Finds products matching optional filters and orders them by name. */
    public List<ProductsRecord> findFiltered(Long categoryId, BigDecimal minPrice, BigDecimal maxPrice, String keyword) {
        // Start with base SELECT
        var query = dsl.selectFrom(PRODUCTS);
        // Build dynamic WHERE conditions
        Condition condition = DSL.trueCondition();
        if (categoryId != null) {
            condition = condition.and(PRODUCTS.CATEGORY_ID.eq(Math.toIntExact(categoryId)));
        }
        if (minPrice != null) {
            condition = condition.and(PRODUCTS.PRICE.ge(minPrice));
        }
        if (maxPrice != null) {
            condition = condition.and(PRODUCTS.PRICE.le(maxPrice));
        }
        if (keyword != null && !keyword.isEmpty()) {
            String pattern = "%" + keyword.toLowerCase() + "%";
            condition = condition.and(
                    DSL.lower(PRODUCTS.NAME).like(pattern)
                            .or(DSL.lower(PRODUCTS.DESCRIPTION).like(pattern))
            );
        }
        // Execute query with ORDER BY name
        return query.where(condition)
                .orderBy(PRODUCTS.NAME.asc())
                .fetch();
    }

    /** Bulk-updates product prices by a percentage (e.g. percent=10 means +10%). */
    public int updatePrices(BigDecimal percent) {
        // Compute multiplier = (100 + percent) / 100, e.g. 1.10 for +10%
        BigDecimal multiplier = percent.add(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(100));
        // Execute SQL UPDATE
        return dsl.update(PRODUCTS)
                .set(PRODUCTS.PRICE, PRODUCTS.PRICE.mul(multiplier))
                .execute();
    }

    /** Retrieves all products ordered by ID (used for batch order creation). */
    public List<ProductsRecord> findAll() {
        return dsl.selectFrom(PRODUCTS)
                .orderBy(PRODUCTS.ID.asc())
                .fetch();
    }

    /** Finds products by a list of IDs. */
    public List<ProductsRecord> findAllById(List<Long> ids) {
        return dsl.selectFrom(PRODUCTS)
                .where(PRODUCTS.ID.in(ids))
                .fetch();
    }
}

