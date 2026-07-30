CREATE TABLE t_orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_number VARCHAR(255) NOT NULL
);

CREATE TABLE t_order_line_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sku_code VARCHAR(255) NOT NULL,
    price DECIMAL(38,2) NOT NULL,
    quantity INT NOT NULL,
    order_id BIGINT,
    CONSTRAINT fk_order_line_items_order_id FOREIGN KEY (order_id) REFERENCES t_orders(id) ON DELETE CASCADE
);

CREATE INDEX idx_order_line_items_order_id ON t_order_line_items (order_id);

CREATE TABLE t_outbox (
    id VARCHAR(36) PRIMARY KEY,
    aggregate_type VARCHAR(255) NOT NULL,
    aggregate_id VARCHAR(255) NOT NULL,
    event_type VARCHAR(255) NOT NULL,
    payload TEXT NOT NULL,
    status VARCHAR(50) NOT NULL,
    retry_count INT DEFAULT 0,
    last_attempt_at TIMESTAMP NULL,
    error_message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE INDEX idx_outbox_status_created ON t_outbox (status, created_at);
