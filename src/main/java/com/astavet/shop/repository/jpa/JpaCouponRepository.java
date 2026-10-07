package com.astavet.shop.repository.jpa;
import com.astavet.shop.entity.CouponEntity;
import jakarta.persistence.LockModeType;
import java.util.UUID;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;
public interface JpaCouponRepository extends JpaRepository<CouponEntity, UUID> {
    Optional<CouponEntity> findByCode(String code);
    Page<CouponEntity> findAllByOrderByCodeAsc(Pageable pageable);
    @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select c from CouponEntity c where c.code = :code")
    Optional<CouponEntity> lock(@Param("code") String code);
}
