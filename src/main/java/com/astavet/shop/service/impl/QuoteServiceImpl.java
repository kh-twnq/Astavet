package com.astavet.shop.service.impl;

import com.astavet.shop.domain.CartLine;
import com.astavet.shop.domain.Digests;
import com.astavet.shop.domain.OrderLine;
import com.astavet.shop.domain.Product;
import com.astavet.shop.domain.Quote;
import com.astavet.shop.exception.ShopException;
import com.astavet.shop.service.QuoteService;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class QuoteServiceImpl implements QuoteService {
  private static final BigDecimal SHIPPING_FEE = new BigDecimal("7.95");
  private static final BigDecimal FREE_SHIPPING_THRESHOLD = new BigDecimal("100.00");
  private static final BigDecimal MAX_TOTAL = new BigDecimal("1000000.00");

  @Override
  public Quote calculate(List<CartLine> lines, List<Product> products) {
    List<OrderLine> items =
        lines.stream()
            .sorted(Comparator.comparing(CartLine::productId))
            .map(line -> toOrderLine(line, products))
            .toList();
    BigDecimal subtotal =
        items.stream().map(OrderLine::total).reduce(new BigDecimal("0.00"), BigDecimal::add);
    BigDecimal shipping =
        items.isEmpty() || subtotal.compareTo(FREE_SHIPPING_THRESHOLD) >= 0
            ? new BigDecimal("0.00")
            : SHIPPING_FEE;
    BigDecimal total = subtotal.add(shipping);
    if (total.compareTo(MAX_TOTAL) > 0) {
      throw new ShopException(409, "Order total exceeds the allowed limit.");
    }
    String values =
        items.stream()
                .map(
                    line ->
                        line.productId()
                            + ":"
                            + line.quantity()
                            + ":"
                            + line.unitPrice().toPlainString()
                            + ":"
                            + Digests.field(line.name()))
                .reduce("AUD:", String::concat)
            + ":"
            + shipping.toPlainString();
    return new Quote(items, subtotal, shipping, total, "AUD", Digests.sha256(values));
  }

  private OrderLine toOrderLine(CartLine line, List<Product> products) {
    Product product =
        products.stream()
            .filter(item -> item.id().equals(line.productId()))
            .findFirst()
            .orElseThrow();
    return new OrderLine(product.id(), product.name(), product.price(), line.quantity());
  }
}
