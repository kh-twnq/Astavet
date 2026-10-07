ALTER TABLE products ADD COLUMN image_path VARCHAR(300);
UPDATE products SET image_path = CASE WHEN slug = 'astaxanthin-200g' THEN '/assets/astavet-130g.png' ELSE '/assets/product-placeholder.svg' END;
ALTER TABLE products ALTER COLUMN image_path SET NOT NULL;
CREATE TABLE accounts (
    id UUID PRIMARY KEY,
    email VARCHAR(254) NOT NULL UNIQUE CHECK (email = LOWER(TRIM(email))),
    name VARCHAR(100) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE TABLE stock_adjustments (
    id UUID PRIMARY KEY,
    product_id UUID NOT NULL REFERENCES products(id),
    operation_id UUID NOT NULL,
    delta INTEGER NOT NULL CHECK (delta <> 0),
    reason VARCHAR(300) NOT NULL,
    actor VARCHAR(254) NOT NULL,
    resulting_stock INTEGER NOT NULL CHECK (resulting_stock >= 0),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    UNIQUE (product_id, operation_id)
);
CREATE INDEX idx_stock_product_created ON stock_adjustments(product_id, created_at DESC);
CREATE TABLE coupons (
    id UUID PRIMARY KEY,
    code VARCHAR(30) NOT NULL UNIQUE,
    amount NUMERIC(12,2) NOT NULL CHECK (amount > 0),
    minimum_subtotal NUMERIC(12,2) NOT NULL CHECK (minimum_subtotal >= 0),
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    max_uses INTEGER NOT NULL CHECK (max_uses > 0),
    uses INTEGER NOT NULL DEFAULT 0 CHECK (uses >= 0 AND uses <= max_uses),
    active BOOLEAN NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);
ALTER TABLE carts ADD COLUMN coupon_code VARCHAR(30) REFERENCES coupons(code);
ALTER TABLE customer_orders ADD COLUMN account_id UUID REFERENCES accounts(id);
ALTER TABLE customer_orders ADD COLUMN coupon_code VARCHAR(30);
ALTER TABLE customer_orders ADD COLUMN discount NUMERIC(12,2) NOT NULL DEFAULT 0 CHECK (discount >= 0 AND discount <= subtotal);
ALTER TABLE customer_orders RENAME COLUMN total TO gross_total;
ALTER TABLE customer_orders ADD COLUMN total NUMERIC(12,2);
UPDATE customer_orders SET total = gross_total;
ALTER TABLE customer_orders ALTER COLUMN total SET NOT NULL;
ALTER TABLE customer_orders ADD CONSTRAINT ck_payable_total CHECK (total = gross_total - discount AND total >= 0);
CREATE INDEX idx_orders_account_created ON customer_orders(account_id, created_at DESC, id);
CREATE TABLE wishlists (
    id UUID PRIMARY KEY,
    account_id UUID NOT NULL REFERENCES accounts(id),
    product_id UUID NOT NULL REFERENCES products(id),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    UNIQUE (account_id, product_id)
);
CREATE INDEX idx_wishlist_account_created ON wishlists(account_id, created_at DESC);
CREATE TABLE reviews (
    id UUID PRIMARY KEY,
    account_id UUID NOT NULL REFERENCES accounts(id),
    product_id UUID NOT NULL REFERENCES products(id),
    author_name VARCHAR(100) NOT NULL,
    rating INTEGER NOT NULL CHECK (rating BETWEEN 1 AND 5),
    body VARCHAR(2000) NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('PENDING','APPROVED','REJECTED')),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    UNIQUE (account_id, product_id)
);
CREATE INDEX idx_reviews_product_status ON reviews(product_id, status, created_at DESC);
CREATE INDEX idx_reviews_status_created ON reviews(status, created_at DESC);
