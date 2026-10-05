package com.astavet.shop.service.impl;

import com.astavet.shop.domain.Cart;
import com.astavet.shop.domain.CartLine;
import com.astavet.shop.domain.Product;
import com.astavet.shop.domain.Quote;
import com.astavet.shop.domain.ShopException;
import com.astavet.shop.repository.CartRepository;
import com.astavet.shop.service.CartService;
import com.astavet.shop.service.ProductService;
import com.astavet.shop.service.QuoteService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CartServiceImpl implements CartService {
    private final CartRepository repository;
    private final ProductService products;
    private final QuoteService quotes;
    public CartServiceImpl(CartRepository repository, ProductService products, QuoteService quotes) {
        this.repository = repository; this.products = products; this.quotes = quotes;
    }
    @Override
    public Quote view(UUID id) { return quote(repository.lock(id)); }
    @Override
    public Quote setQuantity(UUID id, UUID productId, int quantity) {
        if (quantity < 0 || quantity > 99) { throw new ShopException(400, "Quantity must be between 0 and 99."); }
        Cart cart = repository.lock(id);
        List<CartLine> lines = new ArrayList<>(cart.lines());
        lines.removeIf(line -> line.productId().equals(productId));
        if (quantity > 0) {
            products.find(productId).requireAvailable(quantity);
            lines.add(new CartLine(productId, quantity));
        }
        if (lines.size() > 50) { throw new ShopException(400, "The cart can contain at most 50 products."); }
        repository.replaceLines(id, lines);
        return quote(new Cart(id, lines));
    }
    @Override
    public Cart lock(UUID id) { return repository.lock(id); }
    @Override
    public void clear(UUID id) { repository.replaceLines(id, List.of()); }
    private Quote quote(Cart cart) {
        List<Product> catalog = cart.lines().stream().map(line -> products.find(line.productId())).toList();
        return quotes.calculate(cart.lines(), catalog);
    }
}
