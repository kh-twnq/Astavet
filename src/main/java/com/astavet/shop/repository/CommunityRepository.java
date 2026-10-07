package com.astavet.shop.repository;
import com.astavet.shop.domain.Review;
import com.astavet.shop.domain.ReviewStatus;
import java.util.UUID;
import java.util.List;
import java.util.Optional;
public interface CommunityRepository {
    List<UUID> wishlist(UUID accountId);
    void setWishlist(UUID accountId, UUID productId, boolean saved);
    Optional<Review> findReview(UUID accountId, UUID productId);
    Review lockReview(UUID id);
    Review saveReview(Review review);
    List<Review> publicReviews(UUID productId, int page);
    List<Review> reviewsByStatus(ReviewStatus status, int page);
    List<Review> ownReviews(UUID accountId, int page);
}
