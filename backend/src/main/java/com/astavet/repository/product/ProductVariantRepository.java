package com.astavet.repository.product;

import com.astavet.entity.product.ProductVariant;
import jakarta.persistence.LockModeType;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select variant from ProductVariant variant join fetch variant.product "
            + "where variant.id in :ids order by variant.id")
    List<ProductVariant> findAllByIdForUpdate(@Param("ids") Collection<UUID> ids);
}
