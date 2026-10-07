package com.astavet.shop.service.impl;
import com.astavet.shop.domain.Review;
import com.astavet.shop.domain.Product;
import com.astavet.shop.domain.ReviewStatus;
import com.astavet.shop.exception.ShopException;
import com.astavet.shop.domain.Account;
import com.astavet.shop.repository.OrderRepository;
import com.astavet.shop.repository.CommunityRepository;
import com.astavet.shop.repository.AccountRepository;
import com.astavet.shop.service.AccountService;
import com.astavet.shop.service.ProductService;
import com.astavet.shop.service.CommunityService;
import java.time.Instant;
import java.util.UUID;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service @Transactional(readOnly = true)
public class CommunityServiceImpl implements CommunityService {
    private final CommunityRepository repository;
    private final AccountService accounts;
    private final AccountRepository accountRepository;
    private final ProductService products;
    private final OrderRepository orders;
    public CommunityServiceImpl(CommunityRepository repository, AccountService accounts, AccountRepository accountRepository, ProductService products, OrderRepository orders) {
        this.repository = repository; this.accounts = accounts; this.accountRepository = accountRepository; this.products = products; this.orders = orders;
    }
    @Override @PreAuthorize("hasRole('CUSTOMER')") public List<Product> wishlist() { return wishes(accounts.current().id()); }
    @Override @Transactional @PreAuthorize("hasRole('CUSTOMER')") public List<Product> setWishlist(UUID productId, boolean saved) {
        UUID accountId = accounts.current().id(); accountRepository.lock(accountId);
        Product product = products.find(productId);
        List<UUID> existing = repository.wishlist(accountId);
        if (saved && !product.active()) throw new ShopException(409, "This product is no longer available.");
        if (saved && !existing.contains(productId) && existing.size() >= 100) throw new ShopException(409, "Save at most 100 products.");
        repository.setWishlist(accountId, productId, saved);
        return wishes(accountId);
    }
    @Override public List<Review> publicReviews(UUID productId, int page) { requirePage(page); return repository.publicReviews(productId, page); }
    @Override @PreAuthorize("hasRole('CUSTOMER')") public List<Review> ownReviews(int page) { requirePage(page); return repository.ownReviews(accounts.current().id(), page); }
    @Override @Transactional @PreAuthorize("hasRole('CUSTOMER')") public Review submit(UUID productId, int rating, String body) {
        if (rating < 1 || rating > 5 || body.trim().length() < 10 || body.trim().length() > 2000) throw new ShopException(400, "Use a rating from 1 to 5 and a review of 10 to 2000 characters.");
        Account account = accounts.current(); accountRepository.lock(account.id());
        products.find(productId);
        if (!orders.hasDelivered(account.id(), productId)) throw new ShopException(409, "Reviews are available after an order containing this product has been delivered.");
        Review old = repository.findReview(account.id(), productId).map(r -> repository.lockReview(r.id())).orElse(null);
        Instant now = Instant.now();
        return repository.saveReview(new Review(old == null ? UUID.randomUUID() : old.id(), account.id(), productId, account.name(), rating,
                body.trim(), ReviewStatus.PENDING, old == null ? now : old.createdAt(), now, old == null ? 0 : old.version()));
    }
    @Override @PreAuthorize("hasRole('ADMIN')") public List<Review> moderation(ReviewStatus status, int page) { requirePage(page); return repository.reviewsByStatus(status, page); }
    @Override @Transactional @PreAuthorize("hasRole('ADMIN')") public Review moderate(UUID id, ReviewStatus expected, ReviewStatus status, long expectedVersion) {
        Review old = repository.lockReview(id);
        if (status == ReviewStatus.PENDING) throw new ShopException(400, "Choose approve or reject.");
        if (old.status() == status) return old;
        if (old.status() != expected || old.version() != expectedVersion) throw new ShopException(409, "Review changed. Refresh before moderation.");
        return repository.saveReview(new Review(old.id(), old.accountId(), old.productId(), old.authorName(), old.rating(), old.body(), status, old.createdAt(), Instant.now(), old.version()));
    }
    private List<Product> wishes(UUID accountId) { return products.findAll(repository.wishlist(accountId)); }
    private void requirePage(int page) { if (page < 0 || page > 100000) throw new ShopException(400, "Invalid page number."); }
}
