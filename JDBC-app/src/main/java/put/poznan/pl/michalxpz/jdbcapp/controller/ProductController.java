package put.poznan.pl.michalxpz.jdbcapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import put.poznan.pl.michalxpz.jdbcapp.model.Product;
import put.poznan.pl.michalxpz.jdbcapp.service.ProductService;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ProductController {
    @Autowired
    private ProductService productService;

    // Scenariusz 2: Filtrowanie listy produktów po kryteriach (query params)
    @GetMapping("/products")
    public List<Product> getProducts(
            @RequestParam(required=false) Long categoryId,
            @RequestParam(required=false) BigDecimal minPrice,
            @RequestParam(required=false) BigDecimal maxPrice,
            @RequestParam(required=false) String keyword) {
        return productService.getFilteredProducts(categoryId, minPrice, maxPrice, keyword);
    }

    // Scenariusz 4: Masowa aktualizacja cen produktów o podany % (np. ?percent=5.0)
    @PutMapping("/products/prices")
    public String updatePrices(@RequestParam BigDecimal percent) {
        int updated = productService.updateProductPrices(percent);
        return "Updated prices for " + updated + " products.";
    }
}