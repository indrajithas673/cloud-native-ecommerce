package com.ibatulanand.productservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductRequest {
    @NotBlank(message = "Product name cannot be blank")
    private String name;
    
    private String description;
    
    @NotNull(message = "Price cannot be null")
    @Positive(message = "Price must be strictly positive")
    private BigDecimal price;
}
