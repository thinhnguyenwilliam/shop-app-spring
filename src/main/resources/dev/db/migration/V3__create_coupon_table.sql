CREATE TABLE IF NOT EXISTS coupons (
                                       id INT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
                                       code VARCHAR(50) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true
    );

ALTER TABLE orders
    ADD COLUMN coupon_id INT UNSIGNED,
ADD CONSTRAINT fk_orders_coupon
FOREIGN KEY (coupon_id) REFERENCES coupons(id);

ALTER TABLE order_details
    ADD COLUMN coupon_id INT UNSIGNED,
ADD CONSTRAINT fk_order_details_coupon
FOREIGN KEY (coupon_id) REFERENCES coupons(id);

CREATE TABLE IF NOT EXISTS coupon_conditions (
                                                 id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                                                 coupon_id INT UNSIGNED NOT NULL,
                                                 attribute VARCHAR(255) NOT NULL,
    operator VARCHAR(10) NOT NULL,
    value VARCHAR(255) NOT NULL,
    discount_amount DECIMAL(5, 2) NOT NULL,
    FOREIGN KEY (coupon_id) REFERENCES coupons(id)
    );
