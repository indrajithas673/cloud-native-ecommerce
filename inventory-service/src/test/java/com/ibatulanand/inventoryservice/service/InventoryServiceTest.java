package com.ibatulanand.inventoryservice.service;

import com.ibatulanand.inventoryservice.dto.InventoryDeductRequest;
import com.ibatulanand.inventoryservice.exception.InsufficientStockException;
import com.ibatulanand.inventoryservice.repository.InventoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private InventoryService inventoryService;

    @Test
    void deductInventory_Success() {
        // Arrange
        List<InventoryDeductRequest> requests = List.of(
                new InventoryDeductRequest("SKU-1", 2),
                new InventoryDeductRequest("SKU-2", 3)
        );

        when(inventoryRepository.deductInventory(anyString(), anyInt())).thenReturn(1);

        // Act
        String result = inventoryService.deductInventory(requests);

        // Assert
        assertEquals("Inventory successfully deducted.", result);
        verify(inventoryRepository, times(1)).deductInventory("SKU-1", 2);
        verify(inventoryRepository, times(1)).deductInventory("SKU-2", 3);
    }

    @Test
    void deductInventory_InsufficientStock_ThrowsException() {
        // Arrange
        List<InventoryDeductRequest> requests = List.of(
                new InventoryDeductRequest("SKU-1", 10)
        );

        when(inventoryRepository.deductInventory("SKU-1", 10)).thenReturn(0);

        // Act & Assert
        assertThrows(InsufficientStockException.class, () -> inventoryService.deductInventory(requests));
    }

    @Test
    void deductInventory_AggregatesDuplicates() {
        // Arrange
        List<InventoryDeductRequest> requests = List.of(
                new InventoryDeductRequest("SKU-1", 2),
                new InventoryDeductRequest("SKU-1", 3)
        );

        when(inventoryRepository.deductInventory(anyString(), anyInt())).thenReturn(1);

        // Act
        inventoryService.deductInventory(requests);

        // Assert
        verify(inventoryRepository, times(1)).deductInventory("SKU-1", 5);
        verifyNoMoreInteractions(inventoryRepository);
    }
}
