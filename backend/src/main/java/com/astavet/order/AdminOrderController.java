package com.astavet.order;

import com.astavet.order.OrderDtos.OrderResponse;
import com.astavet.order.OrderDtos.OrderPageResponse;
import com.astavet.order.OrderDtos.UpdateOrderStatusRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/orders")
public class AdminOrderController {

    private final OrderService orderService;

    public AdminOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public OrderPageResponse findAll(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return orderService.findForAdmin(status, page, size);
    }

    @GetMapping("/{id}")
    public OrderResponse findOne(@PathVariable UUID id) {
        return orderService.findOneForAdmin(id);
    }

    @PatchMapping("/{id}/status")
    public OrderResponse updateStatus(@PathVariable UUID id, @Valid @RequestBody UpdateOrderStatusRequest request,
            Authentication authentication) {
        return orderService.updateStatus(id, request.status(), authentication);
    }
}
