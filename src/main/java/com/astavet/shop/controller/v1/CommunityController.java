package com.astavet.shop.controller.v1;
import com.astavet.shop.domain.ReviewStatus;
import com.astavet.shop.dto.ModerateReviewRequest;
import com.astavet.shop.dto.ReviewResponse;
import com.astavet.shop.dto.ProductResponse;
import com.astavet.shop.dto.SetWishlistRequest;
import com.astavet.shop.dto.SaveReviewRequest;
import com.astavet.shop.service.CommunityService;
import jakarta.validation.Valid;
import java.util.UUID;
import java.util.List;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
@RestController @RequestMapping("/api/v1")
public class CommunityController {
    private final CommunityService community;
    public CommunityController(CommunityService community) { this.community = community; }
    @GetMapping("/account/wishlist") public List<ProductResponse> wishlist() { return community.wishlist().stream().map(ProductResponse::from).toList(); }
    @PutMapping("/account/wishlist/{id}") public List<ProductResponse> wishlist(@PathVariable UUID id, @Valid @RequestBody SetWishlistRequest request) {
        return community.setWishlist(id, request.saved()).stream().map(ProductResponse::from).toList();
    }
    @GetMapping("/products/{id}/reviews") public List<ReviewResponse> reviews(@PathVariable UUID id, @RequestParam(defaultValue = "0") int page) {
        return community.publicReviews(id, page).stream().map(ReviewResponse::from).toList();
    }
    @PutMapping("/account/reviews/{id}") public ReviewResponse submit(@PathVariable UUID id, @Valid @RequestBody SaveReviewRequest request) {
        return ReviewResponse.from(community.submit(id, request.rating(), request.body()));
    }
    @GetMapping("/account/reviews") public List<ReviewResponse> own(@RequestParam(defaultValue = "0") int page) {
        return community.ownReviews(page).stream().map(ReviewResponse::from).toList();
    }
    @GetMapping("/admin/reviews") public List<ReviewResponse> moderation(@RequestParam(defaultValue = "PENDING") ReviewStatus status, @RequestParam(defaultValue = "0") int page) {
        return community.moderation(status, page).stream().map(ReviewResponse::from).toList();
    }
    @PutMapping("/admin/reviews/{id}") public ReviewResponse moderate(@PathVariable UUID id, @Valid @RequestBody ModerateReviewRequest request) {
        return ReviewResponse.from(community.moderate(id, request.expectedStatus(), request.status(), request.expectedVersion()));
    }
}
