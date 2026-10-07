package com.astavet.shop.repository.jpa;
import com.astavet.shop.entity.StockAdjustmentEntity;
import java.util.UUID;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
public interface JpaStockAdjustmentRepository extends JpaRepository<StockAdjustmentEntity, UUID> {
    Optional<StockAdjustmentEntity> findByProductIdAndOperationId(UUID productId, UUID operationId);
    List<StockAdjustmentEntity> findTop25ByProductIdOrderByCreatedAtDesc(UUID productId);
}
