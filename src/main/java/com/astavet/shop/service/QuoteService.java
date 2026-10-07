package com.astavet.shop.service;

import com.astavet.shop.domain.CartLine;
import com.astavet.shop.domain.Product;
import com.astavet.shop.domain.Quote;
import java.util.List;

public interface QuoteService {
  Quote calculate(List<CartLine> lines, List<Product> products);
}
