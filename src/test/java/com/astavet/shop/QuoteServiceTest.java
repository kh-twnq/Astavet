package com.astavet.shop;

import com.astavet.shop.domain.CartLine;
import com.astavet.shop.domain.Product;
import com.astavet.shop.domain.Quote;
import com.astavet.shop.service.QuoteService;
import com.astavet.shop.service.impl.QuoteServiceImpl;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class QuoteServiceTest {
    private final QuoteService quotes = new QuoteServiceImpl();
    private final UUID first = UUID.randomUUID();
    private final UUID second = UUID.randomUUID();
    @Test
    void calculatesExactMoneyAndFreeShippingAtThreshold() {
        Product product = product(first, "33.34");
        Quote below = quotes.calculate(List.of(new CartLine(first, 2)), List.of(product));
        assertThat(below.subtotal()).isEqualByComparingTo("66.68");
        assertThat(below.shipping()).isEqualByComparingTo("7.95");
        assertThat(below.total()).isEqualByComparingTo("74.63");
        Quote above = quotes.calculate(List.of(new CartLine(first, 3)), List.of(product));
        assertThat(above.total()).isEqualByComparingTo("100.02");
        assertThat(above.shipping()).isZero();
    }
    @Test
    void quoteIsStableForLineOrderingButChangesForPricesOrQuantity() {
        List<Product> products = List.of(product(first, "49.00"), product(second, "10.00"));
        Quote firstQuote = quotes.calculate(List.of(new CartLine(first, 1), new CartLine(second, 2)), products);
        Quote reordered = quotes.calculate(List.of(new CartLine(second, 2), new CartLine(first, 1)), products);
        Quote repriced = quotes.calculate(List.of(new CartLine(first, 1), new CartLine(second, 2)),
                List.of(product(first, "50.00"), product(second, "10.00")));
        assertThat(reordered.fingerprint()).isEqualTo(firstQuote.fingerprint());
        assertThat(repriced.fingerprint()).isNotEqualTo(firstQuote.fingerprint());
        assertThat(quotes.calculate(List.of(new CartLine(first, 2)), products).fingerprint()).isNotEqualTo(firstQuote.fingerprint());
    }
    @Test
    void emptyCartHasNoShipping() {
        Quote quote = quotes.calculate(List.of(), List.of());
        assertThat(quote.total()).isZero();
        assertThat(quote.lines()).isEmpty();
    }
    private Product product(UUID id, String price) {
        return new Product(id, "product-" + id, "Product", "Description", new BigDecimal(price), "AUD", 99, true);
    }
}
