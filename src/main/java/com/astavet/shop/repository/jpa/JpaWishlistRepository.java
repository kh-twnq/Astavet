package com.astavet.shop.repository.jpa;
import com.astavet.shop.entity.WishlistEntity;
import java.util.UUID;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
public interface JpaWishlistRepository extends JpaRepository<WishlistEntity, UUID> {
    List<WishlistEntity> findByAccountIdOrderByCreatedAtDesc(UUID accountId);
    Optional<WishlistEntity> findByAccountIdAndProductId(UUID accountId, UUID productId);
}
