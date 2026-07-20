package com.ibatulanand.inventoryservice.service;

import com.ibatulanand.inventoryservice.dto.InventoryDeductRequest;
import com.ibatulanand.inventoryservice.dto.InventoryResponse;
import com.ibatulanand.inventoryservice.exception.InsufficientStockException;
import com.ibatulanand.inventoryservice.model.Inventory;
import com.ibatulanand.inventoryservice.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    @Transactional(readOnly = true)
    public List<InventoryResponse> isInStock(List<String> skuCode) {
        return inventoryRepository.findBySkuCodeIn(skuCode).stream()
                .map(inventory ->
                        InventoryResponse.builder()
                                .skuCode(inventory.getSkuCode())
                                .isInStock(inventory.getQuantity() > 0)
                                .build()
                ).toList();
    }

    @Transactional
    public String deductInventory(List<InventoryDeductRequest> deductRequests) {
        // Aggregate duplicate SKUs and sort them to prevent deadlocks
        Map<String, Integer> aggregatedRequests = deductRequests.stream()
                .collect(Collectors.toMap(
                        InventoryDeductRequest::getSkuCode,
                        InventoryDeductRequest::getQuantity,
                        Integer::sum,
                        TreeMap::new // TreeMap automatically sorts by SKU code lexicographically
                ));

        for (Map.Entry<String, Integer> entry : aggregatedRequests.entrySet()) {
            int rowsUpdated = inventoryRepository.deductInventory(entry.getKey(), entry.getValue());
            if (rowsUpdated == 0) {
                throw new InsufficientStockException("Insufficient stock for SKU: " + entry.getKey());
            }
        }
        return "Inventory successfully deducted.";
    }
}
