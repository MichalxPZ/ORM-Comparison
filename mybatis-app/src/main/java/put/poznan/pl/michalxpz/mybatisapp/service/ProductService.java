package put.poznan.pl.michalxpz.mybatisapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import put.poznan.pl.michalxpz.mybatisapp.model.Product;
import put.poznan.pl.michalxpz.mybatisapp.repository.ProductMapper;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductService {

    @Autowired private ProductMapper productMapper;

    // Scenariusz 2: filtrowanie listy produktów po kryteriach
    @Transactional(readOnly = true)
    public List<Product> getFilteredProducts(Long categoryId, BigDecimal minPrice,
                                             BigDecimal maxPrice, String keyword) {
        // Wywołujemy mapper z dynamicznym SQL-em w XML:
        return productMapper.findFiltered(categoryId, minPrice, maxPrice, keyword);
    }

    // Scenariusz 4: masowa aktualizacja cen produktów
    @Transactional
    public int updateProductPrices(BigDecimal percent) {
        return productMapper.updatePrices(percent);
    }
}
