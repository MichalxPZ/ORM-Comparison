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
    public List<ProductRecord> findFiltered(Long categoryId, BigDecimal minPrice, BigDecimal maxPrice, String keyword) {
        // Start with base SELECT
        var query = dsl.selectFrom(PRODUCT);
        // Build dynamic WHERE conditions
        Condition condition = DSL.trueCondition();
        if (categoryId != null) {
            condition = condition.and(PRODUCT.CATEGORY_ID.eq(categoryId));
        }
        if (minPrice != null) {
            condition = condition.and(PRODUCT.PRICE.ge(minPrice));
        }
        if (maxPrice != null) {
            condition = condition.and(PRODUCT.PRICE.le(maxPrice));
        }
        if (keyword != null && !keyword.isEmpty()) {
            String pattern = "%" + keyword.toLowerCase() + "%";
            condition = condition.and(
                    DSL.lower(PRODUCT.NAME).like(pattern)
                            .or(DSL.lower(PRODUCT.DESCRIPTION).like(pattern))
            );
        }
        // Execute query with ORDER BY name
        return query.where(condition)
                .orderBy(PRODUCT.NAME.asc())
                .fetch();
    }

    /** Bulk-updates product prices by a percentage (e.g. percent=10 means +10%). */
    public int updatePrices(BigDecimal percent) {
        // Compute multiplier = (100 + percent) / 100, e.g. 1.10 for +10%
        BigDecimal multiplier = percent.add(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(100));
        // Execute SQL UPDATE
        return dsl.update(PRODUCT)
                .set(PRODUCT.PRICE, PRODUCT.PRICE.mul(multiplier))
                .execute();
    }

    /** Retrieves all products ordered by ID (used for batch order creation). */
    public List<ProductRecord> findAll() {
        return dsl.selectFrom(PRODUCT)
                .orderBy(PRODUCT.ID.asc())
                .fetch();
    }

    /** Finds products by a list of IDs. */
    public List<ProductRecord> findAllById(List<Long> ids) {
        return dsl.selectFrom(PRODUCT)
                .where(PRODUCT.ID.in(ids))
                .fetch();
    }
}

