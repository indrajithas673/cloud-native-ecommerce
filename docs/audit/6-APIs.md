# APIs

This document details every REST endpoint exposed by the system. All external requests are expected to route through the **API Gateway** (`/api/**`) with a valid Keycloak JWT Bearer token.

---

## Product Service APIs

### 1. Create Product
- **Service**: Product Service
- **HTTP Method**: `POST`
- **URL**: `/api/product`
- **Purpose**: Adds a new product to the catalog.
- **Authentication**: Required (Valid JWT Token)
- **Validation**: No strict payload validation (missing `@Valid`).
- **Request Body**:
  ```json
  {
    "name": "iPhone 15",
    "description": "Apple Smartphone",
    "price": 999.99
  }
  ```
- **Response**: `201 Created`
- **Important Classes**: `ProductController.java` (`createProduct`), `ProductRequest.java`

### 2. Get All Products
- **Service**: Product Service
- **HTTP Method**: `GET`
- **URL**: `/api/product`
- **Purpose**: Retrieves all products from the catalog.
- **Authentication**: Required (Valid JWT Token)
- **Response**: `200 OK`
  ```json
  [
    {
      "id": "uuid-string",
      "name": "iPhone 15",
      "description": "Apple Smartphone",
      "price": 999.99
    }
  ]
  ```
- **Important Classes**: `ProductController.java` (`getAllProducts`), `ProductResponse.java`

---

## Order Service APIs

### 3. Place Order
- **Service**: Order Service
- **HTTP Method**: `POST`
- **URL**: `/api/order`
- **Purpose**: Creates an order, validates stock, and emits an event.
- **Authentication**: Required (Valid JWT Token)
- **Validation**: Strict runtime validation of inventory availability. If unavailable, throws `IllegalArgumentException`.
- **Request Body**:
  ```json
  {
    "orderLineItemsDtoList": [
      {
        "skuCode": "iphone_15",
        "price": 999.99,
        "quantity": 1
      }
    ]
  }
  ```
- **Response**: `201 Created`
  ```text
  "Order Placed Successfully!"
  ```
  *(Note: If the Inventory Service times out, a string fallback is returned: "Oops! Something went wrong, please order after some time!")*
- **Important Classes**: `OrderController.java` (`placeOrder`), `OrderRequest.java`

### 4. Get All Orders
- **Service**: Order Service
- **HTTP Method**: `GET`
- **URL**: `/api/order`
- **Purpose**: Retrieves order history.
- **Authentication**: Required (Valid JWT Token)
- **Response**: `200 OK`
- **Important Classes**: `OrderController.java` (`getAllOrders`), `OrderResponse.java`

---

## Inventory Service APIs

*(Note: The Inventory API is primarily called internally by the Order Service, but is exposed and theoretically callable via gateway if routed, although the Gateway currently does not route to `/api/inventory` based on `application.properties` rules).*

### 5. Check Stock Status
- **Service**: Inventory Service
- **HTTP Method**: `GET`
- **URL**: `/api/inventory?skuCode={skuCode}&skuCode={skuCode}`
- **Purpose**: Determines if requested SKUs are in stock.
- **Authentication**: Required (Valid JWT Token if routed via Gateway, otherwise internal calls bypass it).
- **Request**: Query parameters mapping to a `List<String> skuCode`.
- **Response**: `200 OK`
  ```json
  [
    {
      "skuCode": "iphone_15",
      "isInStock": true
    }
  ]
  ```
- **Important Classes**: `InventoryController.java` (`isInStock`), `InventoryResponse.java`
