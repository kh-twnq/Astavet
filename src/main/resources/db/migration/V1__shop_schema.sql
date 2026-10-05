CREATE TABLE products (
    id UUID PRIMARY KEY,
    slug VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(160) NOT NULL,
    description VARCHAR(2000) NOT NULL,
    price NUMERIC(12,2) NOT NULL CHECK (price > 0),
    currency VARCHAR(3) NOT NULL CHECK (currency = 'AUD'),
    stock INTEGER NOT NULL CHECK (stock >= 0),
    active BOOLEAN NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);
CREATE TABLE carts (id UUID PRIMARY KEY);
CREATE TABLE cart_lines (
    cart_id UUID NOT NULL REFERENCES carts(id),
    product_id UUID NOT NULL REFERENCES products(id),
    quantity INTEGER NOT NULL CHECK (quantity BETWEEN 1 AND 99),
    PRIMARY KEY (cart_id, product_id)
);
CREATE TABLE customer_orders (
    id UUID PRIMARY KEY,
    cart_id UUID NOT NULL REFERENCES carts(id),
    idempotency_key UUID NOT NULL,
    request_hash VARCHAR(64) NOT NULL,
    customer_name VARCHAR(100) NOT NULL,
    email VARCHAR(254) NOT NULL,
    phone VARCHAR(30) NOT NULL,
    address VARCHAR(300) NOT NULL,
    city VARCHAR(100) NOT NULL,
    postcode VARCHAR(10) NOT NULL,
    state VARCHAR(3) NOT NULL,
    subtotal NUMERIC(12,2) NOT NULL CHECK (subtotal > 0),
    shipping NUMERIC(12,2) NOT NULL CHECK (shipping >= 0),
    total NUMERIC(12,2) NOT NULL CHECK (total = subtotal + shipping),
    currency VARCHAR(3) NOT NULL CHECK (currency = 'AUD'),
    payment_method VARCHAR(3) NOT NULL CHECK (payment_method = 'COD'),
    status VARCHAR(20) NOT NULL CHECK (status IN ('PLACED','CONFIRMED','SHIPPED','DELIVERED','CANCELLED')),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    UNIQUE (cart_id, idempotency_key)
);
CREATE TABLE order_lines (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL REFERENCES customer_orders(id),
    product_id UUID NOT NULL REFERENCES products(id),
    name VARCHAR(160) NOT NULL,
    unit_price NUMERIC(12,2) NOT NULL CHECK (unit_price > 0),
    quantity INTEGER NOT NULL CHECK (quantity BETWEEN 1 AND 99),
    UNIQUE (order_id, product_id)
);
CREATE TABLE order_events (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL REFERENCES customer_orders(id),
    status VARCHAR(20) NOT NULL,
    actor VARCHAR(100) NOT NULL,
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE INDEX idx_orders_created ON customer_orders(created_at DESC, id);
CREATE INDEX idx_events_order ON order_events(order_id, occurred_at);
