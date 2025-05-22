package put.poznan.pl.michalxpz.jdbcapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import put.poznan.pl.michalxpz.jdbcapp.model.Order;
import put.poznan.pl.michalxpz.jdbcapp.model.OrderRequest;
import put.poznan.pl.michalxpz.jdbcapp.service.OrderService;

@RestController
@RequestMapping("/api")
public class OrderController {
    @Autowired
    private OrderService orderService;

    // Scenariusz 1: Pobranie zamówienia z jego pozycjami
    @GetMapping("/orders/{id}")
    public Order getOrderWithItems(@PathVariable Long id) {
        return orderService.getOrderWithItems(id);
    }

    // Scenariusz 3: Utworzenie zamówienia z batch-insert pozycji (parametr count określa ile pozycji dodać)
    @PostMapping("/orders/batchItems")
    public Order createOrderBatch(@RequestParam(name="count", defaultValue="10") int count) {
        return orderService.createOrderWithItemsBatch(count);
    }

    // Scenariusz 5: Złożona operacja transakcyjna (tworzenie zamówienia na podstawie żądania z listą produktów)
    @PostMapping("/orders/complex")
    public Order createOrderTransactional(@RequestBody OrderRequest request) {
        return orderService.placeOrder(request.getUserId(), request.getProductIds());
    }
}
