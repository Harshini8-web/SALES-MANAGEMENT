-- ==================================================
-- ORDER PROCESSING RUNTIME SCHEMA (SUBMIT-READY)
-- ==================================================
-- Purpose:
--   Exact schema needed by current runtime SQL usage in:
--   - com.designx.erp.ui.OrderProcessingService
--   - com.designx.erp.external.db.OrderDataFetcher
--
-- Scope:
--   Includes only tables/columns referenced by runtime queries.
--   This file is intentionally separate from subsystem-specific legacy schemas.
-- ==================================================

CREATE DATABASE IF NOT EXISTS polymorphs;
USE polymorphs;

-- --------------------------------------------------
-- READ-ONLY SALES TABLES (read by OrderDataFetcher)
-- --------------------------------------------------

CREATE TABLE IF NOT EXISTS customers (
    customer_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(20),
    region VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS quotes (
    quote_id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT,
    deal_id INT,
    total_amount DOUBLE,
    discount DOUBLE,
    final_amount DOUBLE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_quotes_customer_id (customer_id),
    INDEX idx_quotes_deal_id (deal_id)
);

CREATE TABLE IF NOT EXISTS quote_items (
    item_id INT AUTO_INCREMENT PRIMARY KEY,
    quote_id INT NOT NULL,
    product_name VARCHAR(150),
    quantity INT,
    price DOUBLE,
    INDEX idx_quote_items_quote_id (quote_id),
    CONSTRAINT fk_quote_items_quote
        FOREIGN KEY (quote_id) REFERENCES quotes(quote_id)
);

CREATE TABLE IF NOT EXISTS deals (
    deal_id INT AUTO_INCREMENT PRIMARY KEY,
    quote_id INT,
    stage VARCHAR(50),
    probability DOUBLE,
    expected_close_date DATETIME,
    INDEX idx_deals_quote_id (quote_id),
    CONSTRAINT fk_deals_quote
        FOREIGN KEY (quote_id) REFERENCES quotes(quote_id)
);

-- --------------------------------------------------
-- ORDER PROCESSING TABLES (read/write by UI service)
-- --------------------------------------------------

CREATE TABLE IF NOT EXISTS orders (
    order_id INT AUTO_INCREMENT PRIMARY KEY,
    quote_id INT NULL,
    customer_id INT NULL,
    customer_name VARCHAR(100) NOT NULL,
    contact_details VARCHAR(100),
    vehicle_model VARCHAR(100),
    vehicle_variant VARCHAR(100),
    vehicle_color VARCHAR(50),
    custom_features VARCHAR(255),
    order_value DOUBLE,
    order_date DATETIME,
    order_details VARCHAR(255),
    current_status VARCHAR(50),
    INDEX idx_orders_status (current_status),
    INDEX idx_orders_order_date (order_date),
    INDEX idx_orders_customer_id (customer_id),
    CONSTRAINT fk_orders_quote
        FOREIGN KEY (quote_id) REFERENCES quotes(quote_id),
    CONSTRAINT fk_orders_customer
        FOREIGN KEY (customer_id) REFERENCES customers(customer_id)
);

CREATE TABLE IF NOT EXISTS payments (
    payment_id VARCHAR(20) PRIMARY KEY,
    order_id INT NOT NULL,
    amount DOUBLE NOT NULL,
    payment_date DATETIME,
    status VARCHAR(30),
    INDEX idx_payments_order_id (order_id),
    INDEX idx_payments_status (status),
    INDEX idx_payments_date (payment_date),
    CONSTRAINT fk_payments_order
        FOREIGN KEY (order_id) REFERENCES orders(order_id)
);

CREATE TABLE IF NOT EXISTS shipments (
    tracking_number VARCHAR(30) PRIMARY KEY,
    order_id INT NOT NULL,
    status VARCHAR(30),
    created_date DATETIME,
    delivered_date DATETIME NULL,
    INDEX idx_shipments_order_id (order_id),
    INDEX idx_shipments_status (status),
    CONSTRAINT fk_shipments_order
        FOREIGN KEY (order_id) REFERENCES orders(order_id)
);

CREATE TABLE IF NOT EXISTS refunds (
    refund_id VARCHAR(20) PRIMARY KEY,
    order_id INT NOT NULL,
    amount DOUBLE NOT NULL,
    created_date DATETIME,
    status VARCHAR(30),
    INDEX idx_refunds_order_id (order_id),
    INDEX idx_refunds_status (status),
    CONSTRAINT fk_refunds_order
        FOREIGN KEY (order_id) REFERENCES orders(order_id)
);
