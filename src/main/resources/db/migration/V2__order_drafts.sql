CREATE TABLE order_drafts (
 id UUID PRIMARY KEY, business_id UUID NOT NULL, version BIGINT NOT NULL,
 status VARCHAR(20) NOT NULL, source_message VARCHAR(10000),
 customer_name VARCHAR(200), customer_phone VARCHAR(40), customer_email VARCHAR(254),
 delivery_method VARCHAR(20), delivery_area VARCHAR(200), delivery_address VARCHAR(1000),
 delivery_requested_date DATE, delivery_original_date_text VARCHAR(500), delivery_notes VARCHAR(1000),
 currency VARCHAR(3) NOT NULL, notes VARCHAR(2000), confirmed_order_id UUID UNIQUE,
 created_at TIMESTAMP WITH TIME ZONE NOT NULL, updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE INDEX idx_drafts_business ON order_drafts(business_id);
CREATE TABLE order_draft_items (
 id UUID PRIMARY KEY, draft_id UUID NOT NULL REFERENCES order_drafts(id), item_position INTEGER,
 product_id UUID, description VARCHAR(300), quantity INTEGER CHECK (quantity > 0),
 unit_price NUMERIC(15,3) CHECK (unit_price >= 0), notes VARCHAR(1000)
);
CREATE INDEX idx_draft_items_draft ON order_draft_items(draft_id);
CREATE TABLE confirmed_orders (
 id UUID PRIMARY KEY, business_id UUID NOT NULL,
 draft_id UUID NOT NULL UNIQUE REFERENCES order_drafts(id), created_at TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE INDEX idx_orders_business_created ON confirmed_orders(business_id, created_at, id);
ALTER TABLE order_drafts ADD CONSTRAINT fk_confirmed_order FOREIGN KEY (confirmed_order_id) REFERENCES confirmed_orders(id);
