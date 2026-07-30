package com.ibatulanand.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InventoryDeductRequest {
    private String skuCode;
    private Integer quantity;
}
