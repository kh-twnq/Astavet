package com.astavet.shop.controller.v1;

import com.astavet.shop.dto.OrderResponse;
import com.astavet.shop.dto.UpdateOrderStatusRequest;
import com.astavet.shop.service.OrderService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/orders")
public class AdminOrderController {
    private final OrderService orders;
    public AdminOrderController(OrderService orders) { this.orders = orders; }
    @GetMapping
    public List<OrderResponse> list(@RequestParam(defaultValue = "0") int page) {
        return orders.list(page).stream().map(OrderResponse::from).toList();
    }
    @GetMapping("/{id}")
    public OrderResponse find(@PathVariable UUID id) { return OrderResponse.from(orders.findForAdmin(id)); }
    @PutMapping("/{id}/status")
    public OrderResponse transition(@PathVariable UUID id, @Valid @RequestBody UpdateOrderStatusRequest request, Principal principal) {
        return OrderResponse.from(orders.transition(id, request.expectedStatus(), request.status(), principal.getName()));
    }
}
