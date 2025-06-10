CREATE DATABASE shopapp_java  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE shopapp_java;
DROP DATABASE shopapp_java;

DESC users;
DESC categories;

-- ✅ Corrected UPDATE to set thumbnail from first image
-- chạy sau khi update trong code java
SET SQL_SAFE_UPDATES = 0; -- Run this before your update:
SET SQL_SAFE_UPDATES = 1; -- Re-enable safe mode afterward:


UPDATE products p
JOIN (
    SELECT product_id, MIN(id) AS first_image_id
    FROM product_images
    GROUP BY product_id
) AS first_images ON p.id = first_images.product_id
JOIN product_images pi ON pi.id = first_images.first_image_id
SET p.thumbnail = pi.image_url;


CREATE TABLE users (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    fullname VARCHAR(50) DEFAULT '',
    phone_number VARCHAR(20) NOT NULL,
    address VARCHAR(50) DEFAULT '',
    password VARCHAR(255) NOT NULL DEFAULT '',
    created_at DATETIME,
    updated_at DATETIME,
    is_active TINYINT(1) DEFAULT 1,
    date_of_birth DATE,
    facebook_account_id INT DEFAULT 0,
    google_account_id INT DEFAULT 0,
	email VARCHAR(100) DEFAULT ''
);
ALTER TABLE users
ADD COLUMN retype_password VARCHAR(255) NOT NULL DEFAULT '';


CREATE TABLE tokens (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    token VARCHAR(512) UNIQUE NOT NULL,
    token_type VARCHAR(50) NOT NULL,
    expiration_date DATETIME,
    revoked TINYINT(1) NOT NULL,
    expired TINYINT(1) NOT NULL,
    user_id INT UNSIGNED,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id)
);
ALTER TABLE tokens
ADD COLUMN is_mobile TINYINT(1) DEFAULT 0;  -- TINYINT is a 1-byte integer data type.



CREATE TABLE social_accounts (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id INT UNSIGNED,
    provider VARCHAR(100) NOT NULL COMMENT 'Tên nhà social network',
    provider_id VARCHAR(100) NOT NULL, -- the unique ID from the social provider
    email VARCHAR(100) NOT NULL COMMENT 'Email tài khoản',
	name VARCHAR(100) NOT NULL COMMENT 'Tên người dùng',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE categories (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL DEFAULT '' COMMENT 'Tên danh mục: đồ điện tử,...',
    slug VARCHAR(100) DEFAULT '', -- for SEO-friendly URLs
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_category_name (name)
);


CREATE TABLE products (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150),
    slug VARCHAR(150) NOT NULL UNIQUE,         -- SEO-friendly URL
    description TEXT,
    price DECIMAL(10,2) NOT NULL CHECK (price >= 0),    -- supports prices up to 99,999,999.99
    category_id INT UNSIGNED NOT NULL,
    thumbnail VARCHAR(150) DEFAULT '',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (category_id) REFERENCES categories(id)
);


CREATE TABLE product_images (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    product_id INT UNSIGNED,
    image_url VARCHAR(300),
    
    
    FOREIGN KEY (product_id) REFERENCES products(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);


CREATE TABLE roles (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,           -- e.g., 'admin', 'user', 'staff'
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
INSERT INTO roles (name) VALUES
('ADMIN'),
('USER'),
('STAFF');


ALTER TABLE users
ADD COLUMN role_id INT UNSIGNED,
ADD FOREIGN KEY (role_id) REFERENCES roles(id);


INSERT INTO users (
    fullname, phone_number, address, password, date_of_birth, email
) VALUES
('Alice Smith', '1234567890', '123 Main St', 'pass123', '1995-05-15', 'alice@example.com'),
('Bob Johnson', '2345678901', '456 Elm St', 'secure456', '1990-03-22', 'bob@example.com'),
('Charlie Brown', '3456789012', '789 Oak Ave', 'charlie789', '1988-07-30', 'charlie@example.com'),
('Diana Prince', '4567890123', '1600 Hero Ln', 'wonderpass', '1992-10-25', 'diana@example.com'),
('Evan Wright', '5678901234', '321 Maple Rd', 'evansecure', '1999-12-01', 'evan@example.com');



CREATE TABLE orders (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id INT UNSIGNED,
    fullname VARCHAR(100) DEFAULT '',
    email VARCHAR(100) DEFAULT '',
    phone_number VARCHAR(100) NOT NULL,
    address VARCHAR(255) NOT NULL,                -- billing/ordering address
    shipping_address VARCHAR(255),                -- where the order is shipped
    shipping_method VARCHAR(100),
    shipping_date DATE,
    tracking_number VARCHAR(100),
    note VARCHAR(100) DEFAULT '',
    order_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(100),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    total_money DECIMAL(10,2) CHECK (total_money >= 0),
   
    FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_order_user (user_id),
    INDEX idx_order_status (status)
);
ALTER TABLE orders
ADD COLUMN active TINYINT(1);

ALTER TABLE orders
MODIFY COLUMN active TINYINT(1) DEFAULT 1;

ALTER TABLE orders
ADD COLUMN payment_method VARCHAR(100) DEFAULT '';

ALTER TABLE orders
MODIFY COLUMN status ENUM('pending', 'processing', 'shipped', 'delivered', 'cancelled', 'returned');

ALTER TABLE orders
MODIFY COLUMN total_money DECIMAL(10,2) DEFAULT 0;

DELETE FROM orders WHERE 1 = 1;

CREATE TABLE order_details (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    order_id INT UNSIGNED NOT NULL,
    product_id INT UNSIGNED NOT NULL,
    price DECIMAL(10,2) NOT NULL CHECK (price >= 0),
    number_of_products INT UNSIGNED,
    total_money DECIMAL(10,2) CHECK (total_money >= 0),
    color VARCHAR(20) DEFAULT '',

    FOREIGN KEY (order_id) REFERENCES orders(id),
    FOREIGN KEY (product_id) REFERENCES products(id)
);
DELETE FROM order_details WHERE 1 = 1;






