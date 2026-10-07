package com.astavet.shop.service;

import com.astavet.shop.domain.Checkout;
import com.astavet.shop.domain.Order;
import com.astavet.shop.domain.OrderStatus;
import java.util.List;
import java.util.UUID;

public interface OrderService {
    Order place(UUID cartId, Checkout checkout);
    Order findForCustomer(UUID cartId, UUID orderId);
    List<Order> list(int page);
    Order findForAdmin(UUID orderId);
    Order transition(UUID id, OrderStatus expected, OrderStatus next, String actor);
    List<Order> listMine(int page);
}
