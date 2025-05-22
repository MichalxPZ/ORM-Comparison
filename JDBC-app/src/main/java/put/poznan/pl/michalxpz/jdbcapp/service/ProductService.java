package put.poznan.pl.michalxpz.jdbcapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import put.poznan.pl.michalxpz.jdbcapp.model.Product;
import put.poznan.pl.michalxpz.jdbcapp.repository.ProductRepository;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductService {
    @Autowired
    private ProductRepository productRepo;

    /** Scenariusz 2: Filtrowanie i sortowanie listy produktów */
    public List<Product> getFilteredProducts(Long categoryId, BigDecimal minPrice,
                                             BigDecimal maxPrice, String keyword) {
        return productRepo.findByCriteria(categoryId, minPrice, maxPrice, keyword);
    }

    /** Scenariusz 4: Aktualizacja cen produktów o zadany procent */
    public int updateProductPrices(BigDecimal percentIncrease) {
        return productRepo.updatePrices(percentIncrease);
    }
}
