CREATE TABLE IF NOT EXISTS comments (
                                        id INT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
                                        product_id INT UNSIGNED,
                                        user_id INT UNSIGNED,
                                        content VARCHAR(255),
                                        created_at DATETIME,
                                        updated_at DATETIME,
                                        FOREIGN KEY (product_id) REFERENCES products(id),
                                        FOREIGN KEY (user_id) REFERENCES users(id)
);
