package com.astavet.entity.order;

import com.astavet.entity.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
public class CustomerOrder extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "order_code", nullable = false, unique = true, length = 32)
    private String orderCode;

    @Column(name = "customer_name", nullable = false, length = 150)
    private String customerName;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(nullable = false, length = 500)
    private String address;

    @Column(length = 1000)
    private String note;

    @Column(nullable = false)
    private long subtotal;

    @Column(name = "shipping_fee", nullable = false)
    private long shippingFee;

    @Column(nullable = false)
    private long total;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 32)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 32)
    private PaymentStatus paymentStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private OrderStatus status;

    @Column(name = "idempotency_key", nullable = false, unique = true, length = 100)
    private String idempotencyKey;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OrderItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("createdAt asc")
    private List<OrderStatusHistory> history = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("createdAt asc")
    private List<PaymentStatusHistory> paymentHistory = new ArrayList<>();

    protected CustomerOrder() {
    }

    public CustomerOrder(String orderCode, String customerName, String phone, String address, String note,
            long subtotal, long shippingFee, String idempotencyKey) {
        this.orderCode = orderCode;
        this.customerName = customerName;
        this.phone = phone;
        this.address = address;
        this.note = note;
        this.subtotal = subtotal;
        this.shippingFee = shippingFee;
        this.total = Math.addExact(subtotal, shippingFee);
        this.paymentMethod = PaymentMethod.COD;
        this.paymentStatus = PaymentStatus.UNPAID;
        this.status = OrderStatus.NEW;
        this.idempotencyKey = idempotencyKey;
        history.add(new OrderStatusHistory(this, null, OrderStatus.NEW, "customer"));
    }

    public void addItem(OrderItem item) {
        items.add(item);
    }

    public void transitionTo(OrderStatus nextStatus, String changedBy) {
        if (!status.canTransitionTo(nextStatus)) {
            throw new IllegalStateException("Invalid order status transition");
        }
        OrderStatus previous = status;
        status = nextStatus;
        history.add(new OrderStatusHistory(this, previous, nextStatus, changedBy));
    }

    public void updatePaymentStatus(PaymentStatus nextPaymentStatus, String changedBy) {
        if (paymentStatus == PaymentStatus.UNPAID
                && nextPaymentStatus == PaymentStatus.PAID
                && status == OrderStatus.DELIVERED) {
            PaymentStatus previous = paymentStatus;
            paymentStatus = nextPaymentStatus;
            paymentHistory.add(new PaymentStatusHistory(this, previous, nextPaymentStatus, changedBy));
            return;
        }
        if (paymentStatus == PaymentStatus.PAID
                && nextPaymentStatus == PaymentStatus.REFUNDED
                && status == OrderStatus.RETURNED) {
            PaymentStatus previous = paymentStatus;
            paymentStatus = nextPaymentStatus;
            paymentHistory.add(new PaymentStatusHistory(this, previous, nextPaymentStatus, changedBy));
            return;
        }
        throw new IllegalStateException("Invalid payment status transition");
    }

    public UUID getId() {
        return id;
    }

    public String getOrderCode() {
        return orderCode;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }

    public String getNote() {
        return note;
    }

    public long getSubtotal() {
        return subtotal;
    }

    public long getShippingFee() {
        return shippingFee;
    }

    public long getTotal() {
        return total;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public List<OrderStatusHistory> getHistory() {
        return history;
    }

    public List<PaymentStatusHistory> getPaymentHistory() {
        return paymentHistory;
    }
}
