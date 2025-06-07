package put.poznan.pl.michalxpz.jooqapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import put.poznan.pl.michalxpz.generated.tables.pojos.Products;
import put.poznan.pl.michalxpz.generated.tables.records.*;
import put.poznan.pl.michalxpz.jooqapp.repository.ProductRepository;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductService {
    @Autowired private ProductRepository productRepository;

    /** Scenario 2: Filter and sort products by criteria. */
    @Transactional(readOnly = true)
    public List<Products> getFilteredProducts(Long categoryId, BigDecimal minPrice, BigDecimal maxPrice, String keyword) {
        return productRepository.findFiltered(categoryId, minPrice, maxPrice, keyword);
    }

    /** Scenario 4: Bulk update of product prices. */
    @Transactional
    public int updateProductPrices(Integer mod) {
        return productRepository.updatePrices(mod);
    }
}

