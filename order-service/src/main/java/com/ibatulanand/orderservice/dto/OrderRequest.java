package com.ibatulanand.orderservice.dto;

import com.ibatulanand.orderservice.model.OrderLineItems;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequest {
    @NotEmpty(message = "Order line items cannot be empty")
    @Valid
    private List<OrderLineItemsDto> orderLineItemsDtoList;
}
