package com.ibatulanand.inventoryservice.repository;

import com.ibatulanand.inventoryservice.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    List<Inventory> findBySkuCodeIn(List<String> skuCode);
    Optional<Inventory> findBySkuCode(String skuCode);

    @Modifying
    @Query("UPDATE Inventory i SET i.quantity = i.quantity - :quantity WHERE i.skuCode = :skuCode")
    int deductInventory(@Param("skuCode") String skuCode, @Param("quantity") Integer quantity);
}
