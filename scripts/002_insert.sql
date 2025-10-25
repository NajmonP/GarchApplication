INSERT INTO garch.users (username, email, password_hash, role, created_at)
VALUES
    ('admin', 'admin@example.com', '$2a$10$T3qVTX65fwhbpfrZXVpLAevFJuF7ltoNhRw7qHw8w44WYUp548DdS', 'ADMIN', now()),
    ('user',  'user@example.com',  '$2a$10$T3qVTX65fwhbpfrZXVpLAevFJuF7ltoNhRw7qHw8w44WYUp548DdS', 'USER',  now());
