# API Documentation

This document outlines the public REST API endpoints exposed by the Micro Marketplace platform via the API Gateway.

## Base URL
All external requests should be routed through the API Gateway, which operates on port `80` (or `443` via HTTPS) at the cluster ingress point (e.g., `http://api.<PUBLIC_IP>.nip.io` in AWS, or `http://api.127.0.0.1.nip.io` locally).

## Authentication
All endpoints require a valid OAuth2 Bearer token in the `Authorization` header, issued by Keycloak.

**Header Format:**
```
Authorization: Bearer <JWT_TOKEN>
```

---

## 1. Product Service

Manages the catalog of products.

### 1.1 Create Product
- **URL**: `/api/product`
- **Method**: `POST`
- **Auth Required**: Yes

**Request Body:**
```json
{
    "name": "Smartphone X",
    "description": "Latest generation smartphone",
    "price": 999.99
}
```

**Response (201 Created):**
```json
{
    "id": "prod_12345",
    "name": "Smartphone X",
    "description": "Latest generation smartphone",
    "price": 999.99
}
```

### 1.2 Get All Products
- **URL**: `/api/product`
- **Method**: `GET`
- **Auth Required**: Yes

**Response (200 OK):**
```json
[
    {
        "id": "prod_12345",
        "name": "Smartphone X",
        "description": "Latest generation smartphone",
        "price": 999.99
    }
]
```

---

## 2. Order Service

Manages customer orders and orchestrates inventory deduction.

### 2.1 Place Order
- **URL**: `/api/order`
- **Method**: `POST`
- **Auth Required**: Yes

**Request Body:**
```json
{
    "orderLineItemsDtoList": [
        {
            "skuCode": "smartphone_x_1",
            "price": 999.99,
            "quantity": 1
        }
    ]
}
```

**Response (201 Created):**
```text
Order Placed Successfully
```

**Fallback Response (200 OK):**
*(If Inventory Service is unreachable or times out)*
```text
Oops! Something went wrong, please order after some time!
```

---

## 3. Inventory Service

Internal service (not exposed directly to external clients via API Gateway by default) managing product stock.

### 3.1 Check Stock (GET)
- **URL**: `/api/inventory`
- **Method**: `GET`
- **Query Params**: `skuCode` (List of SKU strings)
- **Auth Required**: Yes

**Example Request:**
`/api/inventory?skuCode=smartphone_x_1&skuCode=laptop_y_2`

**Response (200 OK):**
```json
[
    {
        "skuCode": "smartphone_x_1",
        "isInStock": true
    },
    {
        "skuCode": "laptop_y_2",
        "isInStock": false
    }
]
```

### 3.2 Deduct Stock (POST)
*(Called internally by Order Service)*
- **URL**: `/api/inventory/deduct`
- **Method**: `POST`
- **Auth Required**: Yes

**Request Body:**
```json
[
    {
        "skuCode": "smartphone_x_1",
        "quantity": 1
    }
]
```

**Response (200 OK):**
```text
Inventory updated successfully.
```

**Error Response (400 Bad Request):**
*(If insufficient stock exists)*
```json
{
    "error": "InsufficientStockException",
    "message": "Product smartphone_x_1 is not in stock, please try again later"
}
```

---

## 4. Notification Service
*(Event-driven via Kafka)*

The Notification Service does not expose public REST APIs. It listens for `OrderPlacedEvent` messages on the `notificationTopic` Kafka topic and simulates sending emails or SMS messages by writing to the application logs.
