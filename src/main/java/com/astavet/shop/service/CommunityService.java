package com.astavet.shop.service;

import com.astavet.shop.domain.Product;
import com.astavet.shop.domain.Review;
import com.astavet.shop.domain.ReviewStatus;
import java.util.List;
import java.util.UUID;

public interface CommunityService {
  List<Product> wishlist();

  List<Product> setWishlist(UUID productId, boolean saved);

  List<Review> publicReviews(UUID productId, int page);

  List<Review> ownReviews(int page);

  Review submit(UUID productId, int rating, String body);

  List<Review> moderation(ReviewStatus status, int page);

  Review moderate(UUID id, ReviewStatus expected, ReviewStatus status, long expectedVersion);
}
