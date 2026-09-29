create table admin_users (
    id uuid primary key,
    email varchar(255) not null unique,
    password_hash varchar(255) not null,
    role varchar(32) not null,
    active boolean not null default true,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null
);

create table products (
    id uuid primary key,
    slug varchar(180) not null unique,
    name varchar(255) not null,
    short_description varchar(500),
    description text,
    status varchar(32) not null,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null
);

create table product_images (
    id uuid primary key,
    product_id uuid not null references products(id),
    url varchar(1000) not null,
    alt_text varchar(255),
    sort_order integer not null default 0
);

create index idx_product_images_product on product_images(product_id, sort_order);

create table product_variants (
    id uuid primary key,
    product_id uuid not null references products(id),
    name varchar(255) not null,
    sku varchar(100) not null unique,
    price bigint not null check (price >= 0),
    stock_quantity integer not null check (stock_quantity >= 0),
    active boolean not null default true,
    version bigint not null default 0,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null
);

create index idx_product_variants_product on product_variants(product_id);

create table orders (
    id uuid primary key,
    order_code varchar(32) not null unique,
    customer_name varchar(150) not null,
    phone varchar(20) not null,
    address varchar(500) not null,
    note varchar(1000),
    subtotal bigint not null check (subtotal >= 0),
    shipping_fee bigint not null check (shipping_fee >= 0),
    total bigint not null check (total >= 0),
    payment_method varchar(32) not null,
    payment_status varchar(32) not null,
    status varchar(32) not null,
    idempotency_key varchar(100) not null unique,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null
);

create index idx_orders_phone on orders(phone);
create index idx_orders_status_created on orders(status, created_at desc);

create table order_items (
    id uuid primary key,
    order_id uuid not null references orders(id),
    product_id uuid not null,
    variant_id uuid not null,
    product_name varchar(255) not null,
    variant_name varchar(255) not null,
    sku varchar(100) not null,
    unit_price bigint not null check (unit_price >= 0),
    quantity integer not null check (quantity > 0),
    line_total bigint not null check (line_total >= 0)
);

create index idx_order_items_order on order_items(order_id);

create table order_status_history (
    id uuid primary key,
    order_id uuid not null references orders(id),
    previous_status varchar(32),
    new_status varchar(32) not null,
    changed_by varchar(255) not null,
    created_at timestamp with time zone not null
);

create index idx_order_status_history_order on order_status_history(order_id, created_at);

