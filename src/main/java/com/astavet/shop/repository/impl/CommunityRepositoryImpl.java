package com.astavet.shop.repository.impl;

import com.astavet.shop.domain.Review;
import com.astavet.shop.domain.ReviewStatus;
import com.astavet.shop.entity.ReviewEntity;
import com.astavet.shop.entity.WishlistEntity;
import com.astavet.shop.exception.ShopException;
import com.astavet.shop.repository.CommunityRepository;
import com.astavet.shop.repository.jpa.JpaReviewRepository;
import com.astavet.shop.repository.jpa.JpaWishlistRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

@Repository
public class CommunityRepositoryImpl implements CommunityRepository {
  private final JpaWishlistRepository wishes;
  private final JpaReviewRepository reviews;

  public CommunityRepositoryImpl(JpaWishlistRepository wishes, JpaReviewRepository reviews) {
    this.wishes = wishes;
    this.reviews = reviews;
  }

  @Override
  public List<UUID> wishlist(UUID accountId) {
    return wishes.findByAccountIdOrderByCreatedAtDesc(accountId).stream()
        .map(w -> w.productId)
        .toList();
  }

  @Override
  public void setWishlist(UUID accountId, UUID productId, boolean saved) {
    Optional<WishlistEntity> found = wishes.findByAccountIdAndProductId(accountId, productId);
    if (saved && found.isEmpty()) {
      WishlistEntity w = new WishlistEntity();
      w.id = UUID.randomUUID();
      w.accountId = accountId;
      w.productId = productId;
      w.createdAt = Instant.now();
      wishes.save(w);
    } else if (!saved && found.isPresent()) {
      wishes.delete(found.get());
    }
  }

  @Override
  public Optional<Review> findReview(UUID accountId, UUID productId) {
    return reviews.findByAccountIdAndProductId(accountId, productId).map(this::map);
  }

  @Override
  public Review lockReview(UUID id) {
    return map(reviews.lock(id).orElseThrow(() -> new ShopException(404, "Review not found.")));
  }

  @Override
  public Review saveReview(Review review) {
    ReviewEntity e = new ReviewEntity();
    e.id = review.id();
    e.accountId = review.accountId();
    e.productId = review.productId();
    e.authorName = review.authorName();
    e.version = review.version();
    e.rating = review.rating();
    e.body = review.body();
    e.status = review.status();
    e.createdAt = review.createdAt();
    e.updatedAt = review.updatedAt();
    return map(reviews.saveAndFlush(e));
  }

  @Override
  public List<Review> publicReviews(UUID productId, int page) {
    return reviews
        .findByProductIdAndStatusOrderByCreatedAtDescIdAsc(
            productId, ReviewStatus.APPROVED, PageRequest.of(page, 25))
        .stream()
        .map(this::map)
        .toList();
  }

  @Override
  public List<Review> reviewsByStatus(ReviewStatus status, int page) {
    return reviews.findByStatusOrderByCreatedAtDescIdAsc(status, PageRequest.of(page, 25)).stream()
        .map(this::map)
        .toList();
  }

  @Override
  public List<Review> ownReviews(UUID accountId, int page) {
    return reviews
        .findByAccountIdOrderByCreatedAtDescIdAsc(accountId, PageRequest.of(page, 25))
        .stream()
        .map(this::map)
        .toList();
  }

  private Review map(ReviewEntity entity) {
    return new Review(
        entity.id,
        entity.accountId,
        entity.productId,
        entity.authorName,
        entity.rating,
        entity.body,
        entity.status,
        entity.createdAt,
        entity.updatedAt,
        entity.version);
  }
}
