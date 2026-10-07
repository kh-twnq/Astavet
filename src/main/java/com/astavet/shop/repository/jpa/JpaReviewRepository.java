package com.astavet.shop.repository.jpa;
import com.astavet.shop.domain.ReviewStatus;
import com.astavet.shop.entity.ReviewEntity;
import jakarta.persistence.LockModeType;
import java.util.UUID;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;
public interface JpaReviewRepository extends JpaRepository<ReviewEntity, UUID> {
    Optional<ReviewEntity> findByAccountIdAndProductId(UUID accountId, UUID productId);
    Page<ReviewEntity> findByProductIdAndStatusOrderByCreatedAtDescIdAsc(UUID productId, ReviewStatus status, Pageable page);
    Page<ReviewEntity> findByStatusOrderByCreatedAtDescIdAsc(ReviewStatus status, Pageable page);
    Page<ReviewEntity> findByAccountIdOrderByCreatedAtDescIdAsc(UUID accountId, Pageable page);
    @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select r from ReviewEntity r where r.id = :id")
    Optional<ReviewEntity> lock(@Param("id") UUID id);
}
