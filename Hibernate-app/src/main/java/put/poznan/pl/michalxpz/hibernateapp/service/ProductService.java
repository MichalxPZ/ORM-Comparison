package put.poznan.pl.michalxpz.hibernateapp.service;

import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import put.poznan.pl.michalxpz.hibernateapp.model.Product;
import put.poznan.pl.michalxpz.hibernateapp.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProductService {
    @Autowired private ProductRepository productRepository;

    // Scenariusz 2: filtrowanie listy produktów po kryteriach
    @Transactional(readOnly = true)
    public List<Product> getFilteredProducts(Long categoryId, BigDecimal minPrice,
                                             BigDecimal maxPrice, String keyword) {
        Specification<Product> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }
            if (minPrice != null) {
                predicates.add(cb.ge(root.get("price"), minPrice));
            }
            if (maxPrice != null) {
                predicates.add(cb.le(root.get("price"), maxPrice));
            }
            if (keyword != null && !keyword.isEmpty()) {
                String pattern = "%" + keyword.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), pattern),
                        cb.like(cb.lower(root.get("description")), pattern)
                ));
            }
            query.orderBy(cb.asc(root.get("name")));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return productRepository.findAll(spec);
    }

    // Scenariusz 4: masowa aktualizacja cen produktów
    @Transactional
    public int updateProductPrices(BigDecimal percent) {
        return productRepository.updatePrices(percent);
    }
}

