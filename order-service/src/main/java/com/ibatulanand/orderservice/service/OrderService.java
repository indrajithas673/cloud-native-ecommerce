package com.ibatulanand.orderservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibatulanand.orderservice.dto.InventoryDeductRequest;
import com.ibatulanand.orderservice.dto.InventoryResponse;
import com.ibatulanand.orderservice.dto.OrderLineItemsDto;
import com.ibatulanand.orderservice.dto.OrderRequest;
import com.ibatulanand.orderservice.dto.OrderResponse;
import com.ibatulanand.orderservice.event.OrderPlacedEvent;
import com.ibatulanand.orderservice.model.Order;
import com.ibatulanand.orderservice.model.OrderLineItems;
import com.ibatulanand.orderservice.model.Outbox;
import com.ibatulanand.orderservice.model.OutboxStatus;
import com.ibatulanand.orderservice.repository.OrderRepository;
import com.ibatulanand.orderservice.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final OutboxRepository outboxRepository;
    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(this::mapToOrderResponse)
                .toList();
    }

    public String placeOrder(OrderRequest orderRequest) {
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());

        List<OrderLineItems> orderLineItems = orderRequest.getOrderLineItemsDtoList()
                .stream()
                .map(this::mapToDto)
                .toList();
        order.setOrderLineItemsList(orderLineItems);

        List<InventoryDeductRequest> deductRequests = order.getOrderLineItemsList().stream()
                .map(item -> new InventoryDeductRequest(item.getSkuCode(), item.getQuantity()))
                .toList();

        // Call Inventory service to deduct stock atomically
        try {
            webClientBuilder.build().post()
                    .uri("http://inventory-service/api/inventory/deduct")
                    .bodyValue(deductRequests)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            // If we reach here, deduction was successful, save the order
            orderRepository.save(order);
            
            // Save event to Outbox within the same transaction to guarantee atomicity (eliminates Dual-Write problem)
            try {
                OrderPlacedEvent event = new OrderPlacedEvent(order.getOrderNumber());
                String payload = objectMapper.writeValueAsString(event);
                
                Outbox outbox = Outbox.builder()
                        .aggregateType("Order")
                        .aggregateId(order.getOrderNumber())
                        .eventType("OrderPlacedEvent")
                        .payload(payload)
                        .status(OutboxStatus.PENDING)
                        .retryCount(0)
                        .build();
                        
                outboxRepository.save(outbox);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to serialize Outbox payload", e);
            }
            
            return "Order Placed Successfully!";
        } catch (WebClientResponseException e) {
            throw new IllegalArgumentException("Product is not in stock or insufficient quantity, please try again later");
        }
    }

    private OrderLineItems mapToDto(OrderLineItemsDto orderLineItemsDto) {
        OrderLineItems orderLineItems = new OrderLineItems();
        orderLineItems.setPrice(orderLineItemsDto.getPrice());
        orderLineItems.setQuantity(orderLineItemsDto.getQuantity());
        orderLineItems.setSkuCode(orderLineItemsDto.getSkuCode());
        return orderLineItems;
    }

    private OrderResponse mapToOrderResponse(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getOrderLineItemsList().stream()
                        .map(this::mapToOrderLineItemsDto)
                        .toList()
        );
    }

    private OrderLineItemsDto mapToOrderLineItemsDto(OrderLineItems orderLineItems) {
        return new OrderLineItemsDto(
                orderLineItems.getId(),
                orderLineItems.getSkuCode(),
                orderLineItems.getPrice(),
                orderLineItems.getQuantity()
        );
    }
}
