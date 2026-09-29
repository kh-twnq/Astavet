package com.astavet.service.order;

import com.astavet.config.StoreProperties;
import com.astavet.dto.request.order.CreateOrderItemRequest;
import com.astavet.dto.request.order.CreateOrderRequest;
import com.astavet.dto.response.order.OrderPageResponse;
import com.astavet.dto.response.order.OrderResponse;
import com.astavet.entity.order.CustomerOrder;
import com.astavet.entity.order.OrderItem;
import com.astavet.entity.order.OrderStatus;
import com.astavet.entity.product.ProductStatus;
import com.astavet.entity.product.ProductVariant;
import com.astavet.exception.ApiException;
import com.astavet.mapper.order.OrderMapper;
import com.astavet.repository.order.OrderRepository;
import com.astavet.repository.product.ProductVariantRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductVariantRepository variantRepository;
    private final StoreProperties storeProperties;
    private final Clock clock;

    @Autowired
    public OrderService(OrderRepository orderRepository, ProductVariantRepository variantRepository,
            StoreProperties storeProperties) {
        this(orderRepository, variantRepository, storeProperties, Clock.systemUTC());
    }

    OrderService(OrderRepository orderRepository, ProductVariantRepository variantRepository,
            StoreProperties storeProperties, Clock clock) {
        this.orderRepository = orderRepository;
        this.variantRepository = variantRepository;
        this.storeProperties = storeProperties;
        this.clock = clock;
    }

    @Transactional
    public OrderResponse create(CreateOrderRequest request) {
        String idempotencyKey = request.idempotencyKey().trim();
        orderRepository.lockIdempotencyKey(idempotencyKey);
        return orderRepository.findByIdempotencyKey(idempotencyKey)
                .map(existing -> existingOrderResponse(existing, request))
                .orElseGet(() -> createNewOrder(request, idempotencyKey));
    }

    @Transactional(readOnly = true)
    public OrderPageResponse findForAdmin(OrderStatus status, int page, int size) {
        PageRequest pageable = PageRequest.of(Math.max(0, page), Math.min(Math.max(1, size), 100));
        Page<CustomerOrder> orders = status == null
                ? orderRepository.findAllByOrderByCreatedAtDesc(pageable)
                : orderRepository.findAllByStatusOrderByCreatedAtDesc(status, pageable);
        List<OrderResponse> content = orders.getContent().stream().map(OrderMapper::toResponse).toList();
        return new OrderPageResponse(content, orders.getTotalElements(), orders.getTotalPages(),
                orders.getNumber(), orders.getSize());
    }

    @Transactional(readOnly = true)
    public OrderResponse findOneForAdmin(UUID id) {
        return OrderMapper.toResponse(findOrder(id));
    }

    @Transactional
    public OrderResponse updateStatus(UUID id, OrderStatus status, Authentication authentication) {
        CustomerOrder order = findOrder(id);
        if (!order.getStatus().canTransitionTo(status)) {
            throw new ApiException(HttpStatus.CONFLICT, "INVALID_STATUS_TRANSITION",
                    "Không thể chuyển đơn từ " + order.getStatus() + " sang " + status + ".");
        }
        if (status == OrderStatus.CANCELLED) {
            releaseReservedStock(order);
        }
        order.transitionTo(status, authentication.getName());
        orderRepository.flush();
        return OrderMapper.toResponse(order);
    }

    private OrderResponse createNewOrder(CreateOrderRequest request, String idempotencyKey) {
        Map<UUID, Integer> requestedQuantities = new LinkedHashMap<>();
        for (CreateOrderItemRequest item : request.items()) {
            requestedQuantities.merge(item.variantId(), item.quantity(), Math::addExact);
        }

        Set<UUID> variantIds = requestedQuantities.keySet();
        Map<UUID, ProductVariant> variants = variantRepository.findAllByIdForUpdate(variantIds).stream()
                .collect(Collectors.toMap(ProductVariant::getId, Function.identity()));
        if (variants.size() != variantIds.size()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_CART", "Giỏ hàng chứa sản phẩm không tồn tại.");
        }

        long subtotal = 0;
        for (Map.Entry<UUID, Integer> entry : requestedQuantities.entrySet()) {
            ProductVariant variant = variants.get(entry.getKey());
            if (!variant.isActive() || variant.getProduct().getStatus() != ProductStatus.ACTIVE) {
                throw new ApiException(HttpStatus.CONFLICT, "PRODUCT_UNAVAILABLE", "Một sản phẩm hiện không còn được bán.");
            }
            if (variant.getStockQuantity() < entry.getValue()) {
                throw new ApiException(HttpStatus.CONFLICT, "INSUFFICIENT_STOCK",
                        "Sản phẩm " + variant.getProduct().getName() + " không đủ tồn kho.");
            }
            subtotal = Math.addExact(subtotal, Math.multiplyExact(variant.getPrice(), entry.getValue()));
        }

        CustomerOrder order = new CustomerOrder(generateOrderCode(), request.customerName().trim(),
                normalizePhone(request.phone()), request.address().trim(), trim(request.note()), subtotal,
                storeProperties.shippingFee(), idempotencyKey);
        for (Map.Entry<UUID, Integer> entry : requestedQuantities.entrySet()) {
            ProductVariant variant = variants.get(entry.getKey());
            variant.reserve(entry.getValue());
            order.addItem(new OrderItem(order, variant, entry.getValue()));
        }
        return OrderMapper.toResponse(orderRepository.save(order));
    }

    private OrderResponse existingOrderResponse(CustomerOrder existing, CreateOrderRequest request) {
        Map<UUID, Integer> existingQuantities = existing.getItems().stream()
                .collect(Collectors.toMap(OrderItem::getVariantId, OrderItem::getQuantity, Math::addExact));
        Map<UUID, Integer> requestedQuantities = request.items().stream()
                .collect(Collectors.toMap(CreateOrderItemRequest::variantId, CreateOrderItemRequest::quantity, Math::addExact));
        boolean sameRequest = existing.getCustomerName().equals(request.customerName().trim())
                && existing.getPhone().equals(normalizePhone(request.phone()))
                && existing.getAddress().equals(request.address().trim())
                && Objects.equals(existing.getNote(), trim(request.note()))
                && existingQuantities.equals(requestedQuantities);
        if (!sameRequest) {
            throw new ApiException(HttpStatus.CONFLICT, "IDEMPOTENCY_CONFLICT",
                    "Khóa gửi đơn đã được sử dụng cho một yêu cầu khác.");
        }
        return OrderMapper.toResponse(existing);
    }

    private void releaseReservedStock(CustomerOrder order) {
        Map<UUID, Integer> quantities = order.getItems().stream()
                .collect(Collectors.toMap(OrderItem::getVariantId, OrderItem::getQuantity, Math::addExact));
        Map<UUID, ProductVariant> variants = variantRepository.findAllByIdForUpdate(quantities.keySet()).stream()
                .collect(Collectors.toMap(ProductVariant::getId, Function.identity()));
        if (variants.size() != quantities.size()) {
            throw new ApiException(HttpStatus.CONFLICT, "VARIANT_MISSING",
                    "Không thể hoàn tồn kho vì một biến thể không còn tồn tại.");
        }
        quantities.forEach((variantId, quantity) -> variants.get(variantId).release(quantity));
    }

    private CustomerOrder findOrder(UUID id) {
        return orderRepository.findOneById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "ORDER_NOT_FOUND", "Không tìm thấy đơn hàng."));
    }

    private String generateOrderCode() {
        String date = LocalDate.now(clock).format(DateTimeFormatter.BASIC_ISO_DATE);
        String suffix = UUID.randomUUID().toString().substring(0, 6).toUpperCase(Locale.ROOT);
        return "AST-" + date + "-" + suffix;
    }

    private String normalizePhone(String value) {
        String normalized = value.replaceAll("\\s+", "");
        return normalized.startsWith("+84") ? "0" + normalized.substring(3) : normalized;
    }

    private String trim(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
