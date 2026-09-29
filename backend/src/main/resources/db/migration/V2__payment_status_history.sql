create table payment_status_history (
    id uuid primary key,
    order_id uuid not null references orders(id),
    previous_status varchar(32) not null,
    new_status varchar(32) not null,
    changed_by varchar(255) not null,
    created_at timestamp with time zone not null
);

create index idx_payment_status_history_order
    on payment_status_history(order_id, created_at);
