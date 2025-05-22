-- =====================================================
-- init.sql – inicjalizacja schematu i przykładowe dane
-- =====================================================

-- SCHEMA

CREATE TABLE categories (
                            id SERIAL PRIMARY KEY,
                            name VARCHAR(100) NOT NULL
);

CREATE TABLE tags (
                      id SERIAL PRIMARY KEY,
                      name VARCHAR(50) NOT NULL
);

CREATE TABLE products (
                          id SERIAL PRIMARY KEY,
                          category_id INT NOT NULL REFERENCES categories(id),
                          name VARCHAR(150) NOT NULL,
                          description TEXT,
                          price NUMERIC(10,2) NOT NULL,
                          stock INT NOT NULL DEFAULT 0
);

CREATE TABLE products_tags (
                               product_id INT NOT NULL REFERENCES products(id),
                               tag_id INT NOT NULL REFERENCES tags(id),
                               PRIMARY KEY (product_id, tag_id)
);

CREATE TABLE users (
                       id SERIAL PRIMARY KEY,
                       name VARCHAR(100) NOT NULL,
                       email VARCHAR(150) UNIQUE NOT NULL
);

CREATE TABLE orders (
                        id SERIAL PRIMARY KEY,
                        user_id INT NOT NULL REFERENCES users(id),
                        order_date TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE order_items (
                             id SERIAL PRIMARY KEY,
                             order_id INT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
                             product_id INT NOT NULL REFERENCES products(id),
                             quantity INT NOT NULL
);

-- SAMPLE DATA

-- Kategorie
INSERT INTO categories (name) VALUES
                                  ('Electronics'),
                                  ('Books'),
                                  ('Home & Kitchen');

-- Tag’i
INSERT INTO tags (name) VALUES
                            ('New'),
                            ('Popular'),
                            ('Discounted');

-- Kilka produktów
INSERT INTO products (category_id, name, description, price, stock) VALUES
                                                                        (1, 'Smartphone X', 'Flagship smartphone', 799.99, 50),
                                                                        (1, 'Wireless Headphones', 'Noise-cancelling', 199.99, 100),
                                                                        (2, 'Spring Boot in Action', 'Comprehensive guide', 39.90, 200),
                                                                        (3, 'Blender Pro', 'High-speed blender', 89.50, 75);

-- Powiązania produktów z tagami
INSERT INTO products_tags (product_id, tag_id) VALUES
                                                   (1, 1), -- Smartphone X: New
                                                   (1, 2), -- Smartphone X: Popular
                                                   (2, 2), -- Headphones: Popular
                                                   (4, 3); -- Blender: Discounted

-- Użytkownicy
INSERT INTO users (name, email) VALUES
                                    ('Alice Kowalska', 'alice@example.com'),
                                    ('Bob Nowak',     'bob@example.com');

-- Przykladowe zamówienie i pozycje
INSERT INTO orders (user_id, order_date) VALUES
                                             (1, NOW() - INTERVAL '2 days'),
                                             (2, NOW() - INTERVAL '1 day');

INSERT INTO order_items (order_id, product_id, quantity) VALUES
                                                             (1, 1, 1),
                                                             (1, 2, 2),
                                                             (2, 3, 1);