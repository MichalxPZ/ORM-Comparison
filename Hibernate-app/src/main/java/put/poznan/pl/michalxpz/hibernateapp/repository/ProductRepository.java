package put.poznan.pl.michalxpz.hibernateapp.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import put.poznan.pl.michalxpz.hibernateapp.model.Product;

import java.math.BigDecimal;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    // Metoda do masowej aktualizacji cen (batch update)
    @Modifying
    @Query("UPDATE Product p SET p.price = p.price * (1 + :percent/100)")
    int updatePrices(BigDecimal percent);
}

