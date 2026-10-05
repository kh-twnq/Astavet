package com.astavet.shop.domain;

import java.math.BigDecimal;
import java.util.List;

public record Quote(List<OrderLine> lines, BigDecimal subtotal, BigDecimal shipping,
                    BigDecimal total, String currency, String fingerprint) {}
