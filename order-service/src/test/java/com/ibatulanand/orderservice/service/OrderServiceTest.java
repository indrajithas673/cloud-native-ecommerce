package com.ibatulanand.orderservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibatulanand.orderservice.dto.OrderLineItemsDto;
import com.ibatulanand.orderservice.dto.OrderRequest;
import com.ibatulanand.orderservice.event.OrderPlacedEvent;
import com.ibatulanand.orderservice.model.Order;
import com.ibatulanand.orderservice.model.Outbox;
import com.ibatulanand.orderservice.repository.OrderRepository;
import com.ibatulanand.orderservice.repository.OutboxRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OutboxRepository outboxRepository;

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private OrderService selfMock;

    @InjectMocks
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        // Inject the mocked self reference to bypass the actual Resilience4j / WebClient call
        ReflectionTestUtils.setField(orderService, "self", selfMock);
    }

    @Test
    void placeOrder_Success_SavesOrderAndOutboxEvent() throws JsonProcessingException {
        // Arrange
        OrderRequest request = new OrderRequest(
                List.of(new OrderLineItemsDto(null, "SKU-1", BigDecimal.TEN, 1))
        );

        when(selfMock.callInventory(anyList())).thenReturn(CompletableFuture.completedFuture("SUCCESS"));
        when(objectMapper.writeValueAsString(any(OrderPlacedEvent.class))).thenReturn("{\"orderNumber\":\"1234\"}");

        // Act
        String response = orderService.placeOrder(request);

        // Assert
        assertEquals("Order Placed Successfully!", response);
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(outboxRepository, times(1)).save(any(Outbox.class));
    }

    @Test
    void placeOrder_InventoryFallback_ReturnsErrorMessage() {
        // Arrange
        OrderRequest request = new OrderRequest(
                List.of(new OrderLineItemsDto(null, "SKU-1", BigDecimal.TEN, 1))
        );

        when(selfMock.callInventory(anyList())).thenReturn(CompletableFuture.completedFuture("FALLBACK"));

        // Act
        String response = orderService.placeOrder(request);

        // Assert
        assertEquals("Oops! Something went wrong, please try again later. Inventory service is currently unavailable.", response);
        verify(orderRepository, never()).save(any(Order.class));
        verify(outboxRepository, never()).save(any(Outbox.class));
    }
}
