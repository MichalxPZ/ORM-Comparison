package put.poznan.pl.michalxpz.mybatisapp.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import put.poznan.pl.michalxpz.mybatisapp.model.Product;
import java.math.BigDecimal;
import java.util.List;

public interface ProductMapper {
    List<Product> findAll();
    Product findById(Long id);
    List<Product> findByIds(@Param("ids") List<Long> ids);

    // Metoda filtrująca produkty (kategoria, ceny, słowo kluczowe)
    List<Product> findFiltered(@Param("categoryId") Long categoryId,
                               @Param("minPrice") BigDecimal minPrice,
                               @Param("maxPrice") BigDecimal maxPrice,
                               @Param("keyword") String keyword);

    // Aktualizacja cen (zwrotny int = liczba zaktualizowanych rekordów)
    int updatePrices(BigDecimal percent);

    // Wstawianie produktu
    void insert(Product product);

    // Wstawianie powiązania produkt–tag
    void insertProductTag(@Param("productId") Long productId, @Param("tagId") Long tagId);
}

