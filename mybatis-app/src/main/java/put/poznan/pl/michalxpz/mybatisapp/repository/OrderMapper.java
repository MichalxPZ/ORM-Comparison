package put.poznan.pl.michalxpz.mybatisapp.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import put.poznan.pl.michalxpz.mybatisapp.model.Order;
import put.poznan.pl.michalxpz.mybatisapp.model.Product;
import java.util.List;

public interface OrderMapper {
    Order findById(Long id);
    List<Product> findItemsByOrderId(Long orderId);

    void insertOrder(Order order);
    void insertOrderItem(@Param("orderId") Long orderId, @Param("productId") Long productId);
}

